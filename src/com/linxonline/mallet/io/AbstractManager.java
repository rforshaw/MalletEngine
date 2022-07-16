package com.linxonline.mallet.io ;

import java.util.List ;
import java.util.Map ;
import java.util.Set ;
import java.util.Collection ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.MalletMap ;

public abstract class AbstractManager<K, T extends Resource> implements ILoader<K, T>
{
	protected final AbstractLoader<K, T> abstractLoader = new AbstractLoader<K, T>() ;
	protected final Map<K, T> resources = MalletMap.<K, T>newMap() ;
	private final List<K> toRemove = MalletList.<K>newList() ;

	public AbstractManager() {}

	@Override
	public T put( final K _key, final T _value  )
	{
		resources.put( _key, _value ) ;
		return _value ;
	}

	/**
		Load the resource specified by file path and use 
		file path as unique key for future reference.
	*/
	@Override
	public T get( final K _file )
	{
		clean() ;

		if( exists( _file ) == true )
		{
			// May still return a null resource but
			// the key has been assigned.
			return resources.get( _file ) ;
		}

		// If the resource doesn't exist create the resource 
		// using the appropriate resource loader.
		return createResource( _file ) ;
	}

	public void remove( final K _file )
	{
		if( toRemove.contains( _file ) == false )
		{
			toRemove.add( _file ) ;
		}
	}
	
	@Override
	public final ResourceLoader<K, T> getResourceLoader()
	{
		return abstractLoader ;
	}

	/**
		Returns the amount of memory in bytes used 
		by the loaded resources.
	*/
	@Override
	public long getMemoryConsumption()
	{
		long consumption = 0L ;

		final Collection<T> res = resources.values() ;
		for( final Resource resource : res )
		{
			consumption += resource.getMemoryConsumption() ;
		}

		return consumption ;
	}

	/**
		Create the resource and add it to the cache.
		Some loaders may return null and load the resource
		asynchronously.
	*/
	protected T createResource( final K _file )
	{
		final T resource = abstractLoader.load( _file ) ;
		put( _file, resource ) ;
		return resource ;
	}

	/**
		Removes resources that are not used.
		_activeKeys are resources considered as currently 
		in use.
		_activeKeys do not need to actually be in use, but 
		is potentially a resource that is about to be used, 
		or is considered an intensive resource to load and 
		therefore should not be cleaned up - at least not yet.
	**/
	@Override
	public void clean( final Set<K> _activeKeys )
	{
		final Set<K> available = resources.keySet() ;
		for( final K key : available )
		{
			if( _activeKeys.contains( key ) == false )
			{
				remove( key ) ;
			}
		}

		clean() ;
	}

	/**
		Remove unwanted resources that have been flagged 
		for destruction - a resource is flagged if remove 
		has been called on it.
	*/
	protected void clean()
	{
		for( final K key : toRemove )
		{
			final T resource = resources.get( key ) ;
			if( resource != null )
			{
				resource.destroy() ;
				resources.remove( key ) ;
			}
		}
		toRemove.clear() ;
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

	/**
		Clear currently loaded resources and shutdown 
		anything else that the manager may be using.

		For example the manager may have a connection 
		to a database that needs to be closed.
	*/
	@Override
	public void shutdown()
	{
		clear() ;
	}

	public boolean isKeyNull( final K _key )
	{
		if( exists( _key ) == true )
		{
			// We want to see if an actual object has 
			// been assigned to the key.
			return resources.get( _key ) == null ;
		}

		return false ;
	}
	
	protected boolean exists( final K _key )
	{
		return resources.containsKey( _key ) ;
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
	public class AbstractLoader<K, T extends Resource> implements ResourceLoader<K, T>
	{
		private final List<ResourceDelegate<K, T>> loaders = MalletList.<ResourceDelegate<K, T>>newList() ;

		public AbstractLoader() {}

		@Override
		public void add( final ResourceDelegate<K, T> _delegate )
		{
			if( loaders.contains( _delegate ) == false )
			{
				loaders.add( _delegate ) ;
			}
		}

		@Override
		public void remove( final ResourceDelegate<K, T> _delegate )
		{
			if( loaders.contains( _delegate ) == true )
			{
				loaders.remove( _delegate ) ;
			}
		}

		@Override
		public T load( final K _key )
		{
			assert _key != null ;			// _file must not be null

			final int length = loaders.size() ;
			for( int i = 0; i < length; ++i )
			{
				final ResourceDelegate<K, T> delegate = loaders.get( i ) ;
				if( delegate.isLoadable( _key ) == true )
				{
					return delegate.load( _key ) ;
				}
			}

			return null ;
		}
	}
}
