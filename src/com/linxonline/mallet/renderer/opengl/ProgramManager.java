package com.linxonline.mallet.renderer.opengl ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.io.Resource ;
import com.linxonline.mallet.io.AbstractManager ;
import com.linxonline.mallet.renderer.opengl.JSONProgram ;

public final class ProgramManager<T extends ProgramManager.Program> extends AbstractManager<String, T>
{
	/**
		When loading a program the ProgramManager will load the 
		content a-synchronously.
		To ensure the programs are added safely to resources we 
		temporarily store the program in a queue.
	*/
	private final JSONBind binder ;

	public ProgramManager( final JSONBuilder _builder )
	{
		binder = new JSONBind( _builder ) ;

		final ResourceLoader<String, T> loader = getResourceLoader() ;
		loader.add( new ResourceDelegate<String, T>()
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

	public static abstract class Program extends Resource
	{
		public abstract String getName() ;
	}

	public static interface JSONBuilder
	{
		public void build( final JSONProgram _program ) ;
	}

	private static class JSONBind implements JSONProgram.Delegate
	{
		private final JSONBuilder builder ;

		public JSONBind( JSONBuilder _builder )
		{
			builder = _builder ;
		}

		@Override
		public void loaded( final JSONProgram _program )
		{
			builder.build( _program ) ;
		}

		@Override
		public void failed() {}
	}
}
