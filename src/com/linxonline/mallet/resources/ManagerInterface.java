package com.linxonline.mallet.resources ;

import com.linxonline.mallet.util.settings.Settings ;

public interface ManagerInterface<T>
{
	public boolean add( final String _key, final T _value ) ;

	public T get( final String _key, final Settings _settings ) ;
	public T get( final String _key, final String _file ) ;
	public T get( final String _file ) ;

	public ResourceLoader<T> getResourceLoader() ;

	public void clean() ;		// Cleanup unused Resources
	public void clear() ;		// Ceanup all Resources

	public void shutdown() ;	// Shutdown connections to systems if required

	public static interface ResourceLoader<T>
	{
		public void add( final ResourceDelegate<T> _delegate ) ;
		public void remove( final ResourceDelegate<T> _delegate ) ;

		public T load( final String _file, final Settings _settings ) ;
	}

	public static interface ResourceDelegate<T>
	{
		public boolean isLoadable( final String _file ) ;		// Can the Resource Delegate load the file
		public T load( final String _file, final Settings _settings ) ;				// Load the Resource based on the passed in settings
	}
}