package com.linxonline.mallet.util.factory ;

import java.util.HashMap ;
import com.linxonline.mallet.util.settings.Settings ;

/**
	Factory is a container class for CreatorInterface objects.
**/
public class Factory<T, U> implements FactoryInterface<T, U>
{
	protected final HashMap<String, CreatorInterface<T, U>> creators = new HashMap<String, CreatorInterface<T, U>>() ;

	@Override
	public void addCreator( final CreatorInterface<T, U> _creator )
	{
		final String type = _creator.getType() ;
		if( exists( type ) == true )
		{
			System.out.println( "Creator: " + type + ", already exists" ) ;
			return ;
		}

		creators.put( type, _creator ) ;
	}

	@Override
	public boolean removeCreator( final CreatorInterface<T, U> _creator )
	{
		return removeCreator( _creator.getType() ) ;
	}

	@Override
	public boolean removeCreator( final String _type )
	{
		if( exists( _type ) == true )
		{
			creators.remove( _type ) ;
			return true ;
		}

		return false ;
	}

	@Override
	public T create( final String _type, final U _data )
	{
		if( exists( _type ) == true )
		{
			return ( T )creators.get( _type ).create( _data ) ;
		}

		System.out.println( "Failed to create object of type: " + _type ) ;
		return null ;
	}

	protected boolean exists( final String _type )
	{
		assert _type != null ;
		return creators.containsKey( _type ) ;
	}
}