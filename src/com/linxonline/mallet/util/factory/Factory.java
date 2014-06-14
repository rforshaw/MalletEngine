package com.linxonline.mallet.util.factory ;

import java.util.HashMap ;
import com.linxonline.mallet.util.settings.Settings ;

/**
	Factory is a container class for CreatorInterface objects.
	
	Used by passing a Settings object to create(), which must 
	contain a Key = TYPE with the value as a String.
**/
public class Factory<T> implements FactoryInterface<T>
{
	protected static final String TYPE = "TYPE" ;
	protected final HashMap<String, CreatorInterface<T>> creators = new HashMap<String, CreatorInterface<T>>() ;

	@Override
	public void addCreator( final CreatorInterface<T> _creator )
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
	public boolean removeCreator( final CreatorInterface<T> _creator )
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
	public T create( final Settings _setting )
	{
		final String type = _setting.getString( TYPE, null ) ;
		if( type != null )
		{
			if( exists( type ) == true )
			{
				return ( T )creators.get( type ).create( _setting ) ;
			}
		}

		System.out.println( "Failed to create object of type: " + type ) ;
		return null ;
	}

	protected boolean exists( final String _type )
	{
		assert _type != null ;
		return creators.containsKey( _type ) ;
	}
}