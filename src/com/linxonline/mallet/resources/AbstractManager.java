package com.linxonline.mallet.resources ;

import java.util.List ;
import java.util.Map ;
import java.util.Set ;
import java.util.Collection ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.MalletMap ;

public abstract class AbstractManager<T extends Resource> implements ManagerInterface<T>
{
	protected final AbstractLoader<T> abstractLoader = new AbstractLoader<T>() ;
	protected final Map<String, T> resources = MalletMap.<String, T>newMap() ;

	public AbstractManager() {}

	@Override
	public boolean add( final String _key, final T _value )
	{
		resources.put( _key, _value ) ;
		return true ;
	}

	/**
		Load the resource specified by file path and associate 
		it with _key for future reference.
	*/
	@Override
	public T get( final String _key, final String _file )
	{
		if( exists( _key ) == true )
		{
			final T resource = resources.get( _key ) ;
			if( resource != null )
			{
				// If the resource has already been loaded 
				// increment resource count and return resource
				resource.register() ;
				return resource ;
			}

			// The key has been assigned to a resource 
			// that is currently being loaded async.
			// We don't want to load a resource that is 
			// currently being loaded.
			return null ;
		}

		// If the resource doesn't exist create the resource 
		// using the appropriate resource loader.
		final T resource = createResource( _file, null ) ;
		if( resource != null )
		{
			// If the resource was successfully created, 
			// add it to the manager, increment the resource 
			// count and return, if no resource was created 
			// simply return null
			add( _key, resource ) ;
			resource.register() ;			// Increment usage count
		}

		return resource ;
	}

	/**
		Load the resource specified by file path and use 
		file path as unique key for future reference.
	*/
	@Override
	public T get( final String _file )
	{
		T resource = resources.get( _file ) ;
		if( resource != null )
		{
			// If the resource has already been loaded 
			// increment resource count and return resource
			resource.register() ;
			return resource ;
		}

		// If the resource doesn't exist create the resource 
		// using the appropriate resource loader.
		resource = createResource( _file, null ) ;
		if( resource != null )
		{
			// If the resource was successfully created, 
			// add it to the manager, increment the resource 
			// count and return, if no resource was created 
			// simply return null
			add( _file, resource ) ;
			resource.register() ;			// Increment usage count
		}

		return resource ;
	}

	@Override
	public ResourceLoader<T> getResourceLoader()
	{
		return abstractLoader ;
	}

	protected T createResource( final String _file, final Settings _settings )
	{
		return abstractLoader.load( _file, _settings ) ;
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
		Iterates over all Resource and destroy them.
		Then clearing the Map
	**/
	@Override
	public void clear()
	{
		final Collection<T> res = resources.values() ;
		for( final T resource : res )
		{
			if( resource != null )
			{
				// While loading a resource async 
				// the key is registered and the value 
				// is set to null. It's possible to clear 
				// the manager before a resource is mapped to the key.
				resource.destroy() ;
			}
		}
		resources.clear() ;
	}

	@Override
	public void shutdown()
	{
		clear() ;
	}

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

	/**
		AbstractLoader handles ResourceDelegate's available by this 
		AbstractManager. When extending AbstractManager, call getResourceLoader()
		and add the appropriate ResourceDelegate's to it.
		Checkout GLTextureManager as an example.
	*/
	public class AbstractLoader<T extends Resource> implements ResourceLoader<T>
	{
		private final List<ResourceDelegate<T>> loaders = MalletList.<ResourceDelegate<T>>newList() ;

		public AbstractLoader() {}

		public void add( final ResourceDelegate<T> _delegate )
		{
			if( loaders.contains( _delegate ) == false )
			{
				loaders.add( _delegate ) ;
			}
		}

		public void remove( final ResourceDelegate<T> _delegate )
		{
			if( loaders.contains( _delegate ) == true )
			{
				loaders.remove( _delegate ) ;
			}
		}

		public T load( final String _file, final Settings _settings )
		{
			assert _file != null ;			// _file must not be null, _settings can be null

			final int length = loaders.size() ;
			for( int i = 0; i < length; ++i )
			{
				final ResourceDelegate<T> delegate = loaders.get( i ) ;
				if( delegate.isLoadable( _file ) == true )
				{
					return delegate.load( _file, _settings ) ;
				}
			}

			return null ;
		}
	}
}
