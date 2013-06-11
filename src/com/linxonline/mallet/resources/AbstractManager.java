package com.linxonline.mallet.resources ;

import java.util.HashMap ;
import java.util.Set ;
import java.util.Collection ;

public abstract class AbstractManager implements ManagerInterface
{
	protected final HashMap<String, Resource> resources = new HashMap<String, Resource>() ;

	public AbstractManager() {}
	
	public boolean add( final String _key, final Resource _value )
	{
		if( exists( _key ) == true )
		{
			//System.out.println( "Key: " + _key + " Already Exists." ) ;
			return false ;
		}

		resources.put( _key, _value ) ;
		return true ;
	}

	public Resource get( final String _file )
	{
		if( exists( _file ) == true )
		{
			return resources.get( _file ) ;
		}

		Resource resource = createResource( _file ) ;
		if( resource != null )
		{
			add( _file, resource ) ;
			resource.register() ;
		}

		return resource ;
	}

	/**
		Should be overriden by all classes that extend this class.
		Called when a resource needs to be created.
	**/
	protected Resource createResource( final String _file )
	{
		return null ;
	}

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
	public void clean()
	{
		ManageResources.removeUnwantedResources( resources ) ;
	}

	/**
		Iterates over all Resource and destroying them.
		Then clearing the HashMap
	**/
	public void clear()
	{
		Collection<Resource> res = resources.values() ;
		for( final Resource resource : res )
		{
			resource.destroy() ;
		}
		resources.clear() ;
	}

	public void shutdown() {}

	protected boolean exists( final String _file )
	{
		return resources.containsKey( _file ) ;
	}
	
	public void printContents()
	{
		final Set<String> keys = resources.keySet() ;
		System.out.println( "Amount: " + resources.size() ) ;
		for( String name : keys )
		{
			System.out.println( "Key: " + name ) ;
		}
	}
}