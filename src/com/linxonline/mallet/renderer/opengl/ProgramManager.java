package com.linxonline.mallet.renderer.opengl ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.io.Resource ;
import com.linxonline.mallet.io.AbstractManager ;
import com.linxonline.mallet.renderer.opengl.JSONProgram ;

public class ProgramManager<T extends ProgramManager.Program> extends AbstractManager<T>
{
	/**
		When loading a program the ProgramManager will load the 
		content a-synchronously.
		To ensure the programs are added safely to resources we 
		temporarily store the program in a queue.
	*/
	private final JSONBind<T> binder ;

	public ProgramManager( final JSONBuilder<T> _builder )
	{
		binder = new JSONBind<T>( _builder ) ;

		final ResourceLoader<T> loader = getResourceLoader() ;
		loader.add( new ResourceDelegate<T>()
		{
			public boolean isLoadable( final String _file )
			{
				return JSONProgram.isLoadable( _file ) ;
			}

			public T load( final String _file )
			{
				JSONProgram.load( _file, binder ) ;
				return null ;
			}
		} ) ;
	}

	/**
		Load the specified shader and map it to the 
		passed in key.
		Use the key with get() to retrieve the GLProgram.
	*/
	public void load( final String _key, final String _file )
	{
		put( _key, null ) ;
		createResource( _file ) ;
	}

	@Override
	public T get( final String _key )
	{
		// GLRenderer will continuosly call get() until it 
		// recieves a GLProgram, so we need to compile Programs
		// that are waiting for the OpenGL context 
		// when the render requests it.
		binder.buildPrograms( this ) ;
		return super.get( _key ) ;
	}

	public static abstract class Program extends Resource
	{
		public abstract String getName() ;
	}

	public static interface JSONBuilder<T extends ProgramManager.Program>
	{
		public T build( final JSONProgram _program ) ;
	}

	private static class JSONBind<T extends ProgramManager.Program> implements JSONProgram.Delegate
	{
		private final JSONBuilder<T> builder ;
		private final List<JSONProgram> toBind = MalletList.<JSONProgram>newList() ;

		public JSONBind( JSONBuilder<T> _builder )
		{
			builder = _builder ;
		}

		public void buildPrograms( ProgramManager<T> _manager )
		{
			synchronized( toBind )
			{
				if( toBind.isEmpty() )
				{
					return ;
				}

				final int size = toBind.size() ;
				for( int i = 0; i < size; i++ )
				{
					final T program = builder.build( toBind.get( i ) ) ;
					if( program != null )
					{
						final String id = program.getName() ;
						if( _manager.isKeyNull( id ) == false )
						{
							Logger.println( String.format( "Attempting to override existing resource: %s", id ), Logger.Verbosity.MAJOR ) ;
						}
						_manager.put( id, program ) ;
					}
				}
				toBind.clear() ;
			}
		}

		@Override
		public void loaded( final JSONProgram _program )
		{
			synchronized( toBind )
			{
				// We don't want to compile the Shaders now
				// as that will take control of the OpenGL context.
				toBind.add( _program ) ;
			}
		}

		@Override
		public void failed() {}
	}
}
