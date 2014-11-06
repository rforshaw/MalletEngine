package com.linxonline.mallet.resources ;

import java.util.HashMap ;
import java.util.Set ;
import java.util.Collection ;

public abstract class AbstractManager<T extends Resource> implements ManagerInterface<T>
{
	protected final HashMap<String, T> resources = new HashMap<String, T>() ;

	public AbstractManager() {}

	@Override
	public boolean add( final String _key, final T _value )
	{
		resources.put( _key, _value ) ;
		return true ;
	}

	@Override
	public T get( final String _file )
	{
		if( exists( _file ) == true )
		{
			return resources.get( _file ) ;
		}

		T resource = createResource( _file ) ;
		if( resource != null )
		{
			add( _file, resource ) ;
			resource.register() ;			// Increment usage count
		}

		return resource ;
	}

	/**
		Should be overriden by all classes that extend this class.
		Called when a resource needs to be created.
	**/
	protected abstract T createResource( final String _file ) ;

	/**
		This allow the developer to redirect what resource should be loaded up.

		For example this would allow the request to be redirected to a lower quality 
		or higher quality version of the requested resource. Override to make use of.
	**/
	protected String redirectResourceLocation( final String _file )
	{
		return _file ;
	}
	
	/**
		Removes resources that are not used.
	**/
	@Override
	public void clean()
	{
		ManageResources.removeUnwantedResources( resources ) ;
	}

	/**
		Iterates over all Resource and destroying them.
		Then clearing the HashMap
	**/
	@Override
	public void clear()
	{
		Collection<T> res = resources.values() ;
		for( final T resource : res )
		{
			resource.destroy() ;
		}
		resources.clear() ;
	}

	@Override
	public void shutdown() {}

	protected boolean exists( final String _file )
	{
		return resources.containsKey( _file ) ;
	}

	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer() ;
		buffer.append( "Resources: " + resources ) ;
		return buffer.toString() ;
	}
}