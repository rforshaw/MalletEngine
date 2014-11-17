package com.linxonline.mallet.resources ;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.Set ;
import java.util.Collection ;

import com.linxonline.mallet.util.settings.Settings ;

public abstract class AbstractManager<T extends Resource> implements ManagerInterface<T>
{
	protected final AbstractLoader<T> abstractLoader = new AbstractLoader<T>() ;
	protected final HashMap<String, T> resources = new HashMap<String, T>() ;

	public AbstractManager() {}

	@Override
	public boolean add( final String _key, final T _value )
	{
		resources.put( _key, _value ) ;
		return true ;
	}

	@Override
	public T get( final String _key, final Settings _settings )
	{
		return get( _key, _settings.getString( "FILE", null ) ) ;
	}

	@Override
	public T get( final String _key, final String _file )
	{
		if( exists( _key ) == true )
		{
			return resources.get( _key ) ;
		}

		final T resource = createResource( _file, null ) ;
		if( resource != null )
		{
			add( _key, resource ) ;
			resource.register() ;			// Increment usage count
		}

		return resource ;
	}

	@Override
	public T get( final String _file )
	{
		if( exists( _file ) == true )
		{
			return resources.get( _file ) ;
		}

		final T resource = createResource( _file, null ) ;
		if( resource != null )
		{
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
		Iterates over all Resource and destroying them.
		Then clearing the HashMap
	**/
	@Override
	public void clear()
	{
		final Collection<T> res = resources.values() ;
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

	public class AbstractLoader<T extends Resource> implements ResourceLoader<T>
	{
		private final ArrayList<ResourceDelegate<T>> loaders = new ArrayList<ResourceDelegate<T>>() ;

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