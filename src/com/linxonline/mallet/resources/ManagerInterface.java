package com.linxonline.mallet.resources ;

import com.linxonline.mallet.util.settings.Settings ;

public interface ManagerInterface<T>
{
	public boolean add( final String _key, final T _value ) ;			// Map T to _key

	public T get( final String _key, final Settings _settings ) ;		// Retrieve T with _key or load with _settings
	public T get( final String _key, final String _file ) ;				// Retrieve T with _key or load with _file
	public T get( final String _file ) ;								// Retrieve T, using _file as key or load with _file

	public ResourceLoader<T> getResourceLoader() ;

	public void clean() ;		// Cleanup unused Resources
	public void clear() ;		// Ceanup all Resources

	public void shutdown() ;	// Shutdown connections to systems if required

	/**
		Manage multiple Resource Delegates.
		Provides a central access point to load 
		a file-type without specifically having to know 
		what Resource Delegate to use.
	*/
	public static interface ResourceLoader<T>
	{
		public void add( final ResourceDelegate<T> _delegate ) ;
		public void remove( final ResourceDelegate<T> _delegate ) ;

		public T load( final String _file, final Settings _settings ) ;
	}

	/**
		Implement to handle the loading of a specific file type.
		A Resource Delegate can be used to load/decode 
		multiple files types. If you have a range of different 
		files types, implement them in their own Resource Delegate.
	*/
	public static interface ResourceDelegate<T>
	{
		public boolean isLoadable( final String _file ) ;							// Can the Resource Delegate load the file
		public T load( final String _file, final Settings _settings ) ;				// Load the Resource based on the passed in settings
	}
}