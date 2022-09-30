package com.linxonline.mallet.script ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.entity.Entity ;

public final class Script
{
	private final static Object FALLBACK = new Object() ;
	private final static IListener FALLBACK_LISTENER = new IListener()
	{
		@Override
		public void added( final Object _functions ) {}

		@Override
		public void removed() {}
	} ;

	private final String name ;
	private final String scriptPath ;

	private final List<Entity> entities = MalletList.<Entity>newList() ;

	private IListener listener ;
	private Class<?> scriptFunctions ;

	public Script( final String _path )
	{
		this( _path, _path ) ;
	}

	public Script( final String _name, final String _path )
	{
		name = _name ;
		scriptPath = _path ;
	}

	public boolean addAll( final List<Entity> _entities )
	{
		return entities.addAll( _entities ) ;
	}

	public boolean add( final Entity _entity )
	{
		return entities.add( _entity ) ;
	}

	public boolean remove( final Entity _entity )
	{
		return entities.remove( _entity ) ;
	}

	/**
		The passed in interface is used to allow the Java
		side to access script defined functions.

		When the script is processed by the Scripting Engine
		a function object will be returned from IListener.added().

		You should cast the Object back to the original interface
		that was passed into this function.
	*/
	public boolean setScriptFunctions( final Class<?> _class )
	{
		if( _class.isInterface() == false )
		{
			return false ;
		}

		scriptFunctions = _class ;
		return true ;
	}

	/**
		Return the class interface, if specified,
		that is expected to be mapped to the script functions.
	*/
	public Class<?> getScriptFunctions()
	{
		return scriptFunctions ;
	}

	public void setListener( final IListener _listener )
	{
		listener = ( _listener != null ) ? _listener : FALLBACK_LISTENER ;
	}

	public IListener getListener()
	{
		return listener ;
	}

	public String getName()
	{
		return name ;
	}

	public String getPath()
	{
		return scriptPath ;
	}

	public List<Entity> getEntities()
	{
		return entities ;
	}

	public interface IListener
	{
		/**
			Notify the user that the script has been
			added the the script-engine and can now be
			processed.

			Return an object that implements the passed
			in interface from setScriptFunctions() - this
			allows a user to call into the script.
		*/
		public void added( final Object _functions ) ;

		/**
			Notify the user that the script has been removed
			from the script-engine and is no longer being processed.
		*/
		public void removed() ;
	}
}
