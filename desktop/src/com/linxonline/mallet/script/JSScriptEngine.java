package com.linxonline.mallet.script.javascript ;

import java.util.List ;
import java.util.Set ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.HashSet ;

import org.mozilla.javascript.EvaluatorException ;
import org.mozilla.javascript.ErrorReporter ;
import org.mozilla.javascript.ContextFactory ;
import org.mozilla.javascript.Context ;
import org.mozilla.javascript.Function ;
import org.mozilla.javascript.Scriptable ;
import org.mozilla.javascript.ScriptableObject ;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;
import org.mozilla.javascript.annotations.JSGetter;

import java.lang.reflect.Method ;
import java.lang.reflect.Proxy ;

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
	private final static ContextFactory FACTORY = new ContextFactory() ;

	private final static String FALLBACK_SCRIPT = "function create() { return { start: () => { }, end: () => { } } } ;" ;
	private final static Object[] FALLBACK_ARGUMENTS = new Object[0] ;

	private final BufferedList<Runnable> executions = new BufferedList<Runnable>() ;

	private final Context context ;
	private final Scriptable scope ;
	private final Scriptable state ;

	private HashMap<Script, Meta> lookups = new HashMap<Script, Meta>() ;
	private final List<Meta> updates = MalletList.<Meta>newList() ;

	private final JSLogger logger = new JSLogger() ;

	private final Object[] updateArguments = new Object[1] ;

	public JSScriptEngine()
	{
		context = FACTORY.enterContext() ;
		context.setLanguageVersion( Context.VERSION_ES6 ) ;
		context.setErrorReporter( new ErrorReporter()
		{
			@Override
			public void error( final String _message, final String _sourceName, final int _line, final String _lineSource, final int _lineOffset )
			{
				Logger.println( "Error in: " + _sourceName, Logger.Verbosity.MAJOR ) ;
				Logger.println( _message + " at line: " + _line, Logger.Verbosity.MAJOR ) ;
				Logger.println( _lineSource, Logger.Verbosity.MAJOR ) ;
			}

			@Override
			public EvaluatorException runtimeError( final String _message, final String _sourceName, final int _line, String _lineSource, final int _lineOffset )
			{
				return new EvaluatorException( _message ) ;
			}

			@Override
			public void warning( final String _message, final String _sourceName, int _line, final String _lineSource, final int _lineOffset )
			{
				Logger.println( "Warning in: " + _sourceName, Logger.Verbosity.NORMAL ) ;
				Logger.println( _message + " at line: " + _line, Logger.Verbosity.NORMAL ) ;
			}
		} ) ;

		scope = context.initStandardObjects() ;
		state = context.newObject( scope ) ;

		scope.put( "state", scope, state ) ;
		scope.put( "logger", scope, Context.javaToJS( logger, scope ) ) ;
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

			final List<Script.Register> toRegister = _script.getRegisteredObjects() ;
			final List<JSObject> objects = MalletList.<JSObject>newList( toRegister.size() ) ;
			for( final Script.Register register : toRegister )
			{
				objects.add( JSObject.create( register ) ) ;
			}

			final Meta meta = new Meta( _script, obj, objects ) ;
			lookups.put( _script, meta ) ;

			final Class<?> scriptClass = _script.getScriptFunctions() ;
			Object proxy = null ;
			if( scriptClass != null )
			{
				final ClassLoader loader = scriptClass.getClassLoader() ;
				proxy = Proxy.newProxyInstance( loader, new Class<?>[] { scriptClass }, ( final Object _proxy, final Method _method, final Object[] _args ) ->
				{
					final String methodName = _method.getName() ;
					if( ScriptableObject.hasProperty( obj, methodName ) == false )
					{
						Logger.println( "Javascript function: " + methodName + " does not exist.", Logger.Verbosity.MAJOR ) ;
						return null ;
					}

					final Object response = callMethod( obj, meta, methodName, _args ) ;
					return null ;
				} ) ;
			}

			if( ScriptableObject.hasProperty( obj, "update" ) )
			{
				// Only add the script to updates if it has an
				// update(dt) function.
				updates.add( meta ) ;
			}

			callMethod( obj, meta, "start", FALLBACK_ARGUMENTS ) ;

			final Script.IListener listener = _script.getListener() ;
			listener.added( proxy ) ;
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
				callMethod( jsObject, meta, "end", FALLBACK_ARGUMENTS ) ;

				final Script.IListener listener = _script.getListener() ;
				listener.removed() ;
			}
		} ) ;
	}

	@Override
	public void update( final double _dt )
	{
		updateExecutions() ;
		if( updates.isEmpty() == true )
		{
			return ;
		}

		updateArguments[0] = Double.valueOf( _dt ) ;

		final int size = updates.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Meta meta = updates.get( i ) ;
			final Scriptable jsObject = meta.getJSObject() ;
			callMethod( jsObject, meta, "update", updateArguments ) ;
		}
	}

	private Object callMethod( final Scriptable _jsObj, final Meta _meta, final String _name, final Object[] _arguments )
	{
		scope.put( "script", scope, ( IAccessibleMeta )_meta ) ;

		for( final JSObject obj : _meta.getState() )
		{
			state.put( obj.getName(), state, obj.getProxy() ) ;
		}

		return ScriptableObject.callMethod( _jsObj, _name, _arguments ) ;
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

			final int capacity = ( int )file.getSize() ;

			final StringBuilder builder = new StringBuilder( capacity ) ;
			String line = null ;
			while( ( line = stream.readLine() ) != null )
			{
				builder.append( line ) ;
				builder.append( '\n' ) ;
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
		try
		{
			context.exit() ;
		}
		catch( final Exception ex )
		{
			ex.printStackTrace() ;
		}
	}

	public interface IAccessibleMeta
	{
		public void removeScript() ;
	}

	private final class Meta implements IAccessibleMeta
	{
		private final Script script ;
		private final Scriptable jsObject ;

		private final List<JSObject> state ;

		public Meta( final Script _script, final Scriptable _jsObject, final List<JSObject> _state )
		{
			script = _script ;
			jsObject = _jsObject ;
			state = _state ;
		}

		@Override
		public void removeScript()
		{
			JSScriptEngine.this.remove( script ) ;
		}

		public Script getScript()
		{
			return script ;
		}

		public Scriptable getJSObject()
		{
			return jsObject ;
		}

		public List<JSObject> getState()
		{
			return state ;
		}
	}
}
