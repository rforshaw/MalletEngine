package com.linxonline.mallet.io ;

import java.util.List ;
import java.util.Map ;
import java.util.Set ;
import java.util.Collection ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.MalletMap ;

public abstract class AbstractManager<T extends Resource> implements ILoader<T>
{
	protected final AbstractLoader<T> abstractLoader = new AbstractLoader<T>() ;
	protected final Map<String, T> resources = MalletMap.<String, T>newMap() ;

	public AbstractManager() {}

	@Override
	public T put( final String _key, final T _value  )
	{
		resources.put( _key, _value ) ;
		return _value ;
	}

	/**
		Load the resource specified by file path and use 
		file path as unique key for future reference.
	*/
	@Override
	public T get( final String _file )
	{
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

	@Override
	public ResourceLoader<T> getResourceLoader()
	{
		return abstractLoader ;
	}

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

	protected T createResource( final String _file )
	{
		return abstractLoader.load( _file ) ;
	}

	/**
		Removes resources that are not used.
	**/
	@Override
	public void clean( final Set<String> _activeKeys )
	{
		final List<String> toRemove = MalletList.<String>newList() ;

		final Set<String> available = resources.keySet() ;
		for( final String key : available )
		{
			if( _activeKeys.contains( key ) == false )
			{
				toRemove.add( key ) ;
			}
		}

		for( final String key : toRemove )
		{
			final T resource = resources.get( key ) ;
			if( resource != null )
			{
				resource.destroy() ;
				resources.remove( key ) ;
			}
		}
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

		@Override
		public void add( final ResourceDelegate<T> _delegate )
		{
			if( loaders.contains( _delegate ) == false )
			{
				loaders.add( _delegate ) ;
			}
		}

		@Override
		public void remove( final ResourceDelegate<T> _delegate )
		{
			if( loaders.contains( _delegate ) == true )
			{
				loaders.remove( _delegate ) ;
			}
		}

		@Override
		public T load( final String _file )
		{
			assert _file != null ;			// _file must not be null

			final int length = loaders.size() ;
			for( int i = 0; i < length; ++i )
			{
				final ResourceDelegate<T> delegate = loaders.get( i ) ;
				if( delegate.isLoadable( _file ) == true )
				{
					return delegate.load( _file ) ;
				}
			}

			return null ;
		}
	}
}
