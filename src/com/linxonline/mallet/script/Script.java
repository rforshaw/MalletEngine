package com.linxonline.mallet.script ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

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
	private final Class<?> scriptFunctions ;	// The interface used to call script functions.

	private final List<Register> objects = MalletList.<Register>newList() ;
	private IListener listener ;

	private Script( final String _name, final String _path, final Class<?> _scriptFuncs )
	{
		name = _name ;
		scriptPath = _path ;
		scriptFunctions = _scriptFuncs ;
	}

	public static Script create( final String _path )
	{
		return create( _path, _path, null ) ;
	}

	public static Script create( final String _path, final Class<?> _scriptFuncs )
	{
		return create( _path, _path, _scriptFuncs ) ;
	}
	
	public static Script create( final String _name, final String _path, final Class<?> _scriptFuncs )
	{
		if( _scriptFuncs != null && _scriptFuncs.isInterface() == false )
		{
			return null ;
		}

		return new Script( _name, _path, _scriptFuncs ) ;
	}

	public <T> T register( final String _name, final T _obj )
	{
		objects.add( new Register( _name, _obj ) ) ;
		return _obj ;
	}

	public boolean unregister( final String _name )
	{
		final int size = objects.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Register register = objects.get( i ) ;
			if( register.isName( _name ) )
			{
				objects.remove( i ) ;
				return true ;
			}
		}

		return false ;
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

	public List<Register> getRegisteredObjects()
	{
		return objects ;
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

	public static final class Register
	{
		private String name ;
		private Object object ;

		public Register( final String _name, final Object _object )
		{
			name = _name ;
			object = _object ;
		}

		public boolean isName( final String _name )
		{
			return name.equals( _name ) ;
		}

		public String getName()
		{
			return name ;
		}

		public Object getObject()
		{
			return object ;
		}
	}
}
