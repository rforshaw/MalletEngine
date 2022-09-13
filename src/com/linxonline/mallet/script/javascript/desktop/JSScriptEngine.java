package com.linxonline.mallet.script.javascript ;

import java.util.List ;
import java.util.ArrayList ;
import java.util.HashMap ;

import org.mozilla.javascript.Context ;
import org.mozilla.javascript.Function ;
import org.mozilla.javascript.Scriptable ;
import org.mozilla.javascript.ScriptableObject ;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;
import org.mozilla.javascript.annotations.JSGetter;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.filesystem.StringInStream ;

import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.core.GameState ;
import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.script.Script ;
import com.linxonline.mallet.script.IScriptEngine ;

public final class JSScriptEngine implements IScriptEngine
{
	private final static String FALLBACK_SCRIPT = "function create() { return { start: () => { }, update: ( _dt ) => { }, end: () => { } } } ;" ;
	private final static Object[] FALLBACK_ARGUMENTS = new Object[0] ; 

	private final BufferedList<Runnable> executions = new BufferedList<Runnable>() ;

	private final Context context ;
	private final Scriptable scope ;

	private HashMap<Script, Meta> lookups = new HashMap<Script, Meta>() ;
	private final List<Meta> updates = MalletList.<Meta>newList() ;

	private final JSLogger logger = new JSLogger() ;
	private final JSGameState game ;

	private final Object[] updateArguments = new Object[1] ;

	public JSScriptEngine( final GameState _game )
	{
		context = Context.enter() ;
		context.setLanguageVersion( Context.VERSION_ES6 ) ;

		scope = context.initStandardObjects() ;

		final Object wrappedLogger = Context.javaToJS( logger, scope ) ;
		ScriptableObject.putProperty( scope, "logger", wrappedLogger ) ;

		game = new JSGameState( _game ) ;
		final Object wrappedGame = Context.javaToJS( game, scope ) ;
		ScriptableObject.putProperty( scope, "game", wrappedGame ) ;
	}

	@Override
	public boolean init()
	{
		try
		{
			//ScriptableObject.defineClass( scope, JSUpdatePacket.class ) ;
			//ScriptableObject.defineClass( scope, JSEntity.class ) ;

			// Provide more wrappers for future objects.
			return true ;
		}
		catch( final Exception ex )
		{
			ex.printStackTrace() ;
			return false ;
		}
	}

	@Override
	public void add( final Script _script )
	{
		invokeLater( () ->
		{
			final String name = _script.getName() ;
			final String source = JSScriptEngine.getSource( _script ) ;
		
			final Function func = context.compileFunction( scope, source, name, 1, null ) ;
			final Scriptable obj = ( Scriptable )func.call( context, scope, func, FALLBACK_ARGUMENTS ) ;

			final List<Entity> entities = _script.getEntities() ;
			final List<JSEntity> fill = MalletList.<JSEntity>newList( entities.size() ) ;

			for( final Entity entity : entities )
			{
				fill.add( new JSEntity( entity ) ) ;
			}

			final Meta meta = new Meta( _script, obj, Context.javaToJS( fill, scope )  ) ;
			lookups.put( _script, meta ) ;
			updates.add( meta ) ;

			ScriptableObject.putProperty( scope, "entities", meta.getEntities() ) ;
			ScriptableObject.callMethod( obj, "start", FALLBACK_ARGUMENTS ) ;
		} ) ;
	}

	@Override
	public void remove( final Script _script )
	{
		invokeLater( () ->
		{
			final Meta meta = lookups.remove( _script ) ;
			if( meta != null )
			{
				updates.remove( meta ) ;

				final Scriptable jsObject = meta.getJSObject() ;

				ScriptableObject.putProperty( scope, "entities", meta.getEntities() ) ;
				ScriptableObject.callMethod( jsObject, "end", FALLBACK_ARGUMENTS ) ;
			}
		} ) ;
	}

	@Override
	public void update( final float _dt )
	{
		updateExecutions() ;
		if( updates.isEmpty() == true )
		{
			return ;
		}

		updateArguments[0] = Float.valueOf( _dt ) ;

		for( final Meta meta : updates )
		{
			final Scriptable jsObject = meta.getJSObject() ;

			ScriptableObject.putProperty( scope, "entities", meta.getEntities() ) ;
			ScriptableObject.callMethod( jsObject, "update", updateArguments ) ;
		}
	}

	private void invokeLater( final Runnable _run )
	{
		if( _run != null )
		{
			executions.add( _run ) ;
		}
	}

	private void updateExecutions()
	{
		executions.update() ;
		final List<Runnable> runnables = executions.getCurrentData() ;
		if( runnables.isEmpty() )
		{
			return ;
		}

		final int size = runnables.size() ;
		for( int i = 0; i < size; i++ )
		{
			runnables.get( i ).run() ;
		}
		runnables.clear() ;
	}

	private static String getSource( final Script _script )
	{
		final String path = _script.getPath() ;

		final FileStream file = GlobalFileSystem.getFile( path ) ;
		if( file.exists() == false )
		{
			Logger.println( "Javascript source: " + path + " doesn't exist.", Logger.Verbosity.MAJOR ) ;
			return FALLBACK_SCRIPT ;
		}
	
		try( final StringInStream stream = file.getStringInStream() )
		{
			if( stream == null )
			{
				return FALLBACK_SCRIPT ;
			}

			final StringBuilder builder = new StringBuilder() ;
			String line = null ;
			while( ( line = stream.readLine() ) != null )
			{
				builder.append( line ) ;
			}

			return builder.toString() ;
		}
		catch( Exception ex )
		{
			ex.printStackTrace() ;
			return FALLBACK_SCRIPT ;
		}
	}

	@Override
	public void close()
	{
		context.exit() ;
	}

	private static class Meta
	{
		private final Script script ;
		private final Scriptable jsObject ;
		private final Object entities ;

		public Meta( final Script _script, final Scriptable _jsObject, final Object _entities )
		{
			script = _script ;
			jsObject = _jsObject ;
			entities = _entities ;
		}

		public Script getScript()
		{
			return script ;
		}

		public Scriptable getJSObject()
		{
			return jsObject ;
		}

		public Object getEntities()
		{
			return entities ;
		}
	}
}
