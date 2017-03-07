package com.linxonline.mallet.resources ;

import java.util.Set ;

/**
	Provide a consistant interface for loading resources.
	All resource loading managers should implement this 
	interface, or use AbstractManager for a general 
	implementation that will cover most cases.
*/
public interface ManagerInterface<T extends Resource>
{
	public T put( final String _key, final T _value  ) ;
	public T get( final String _file ) ;								// Retrieve T, using _file as key or load with _file

	public ResourceLoader<T> getResourceLoader() ;
	public long getMemoryConsumption() ;

	public void clean( final Set<String> _activeKeys ) ;		// Cleanup unused Resources
	public void clear() ;										// Ceanup all Resources

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

		public T load( final String _file ) ;
	}

	/**
		Implement to handle the loading of a specific file type.
		A Resource Delegate can be used to load/decode 
		multiple files types. If you have a range of different 
		files types, implement them in their own Resource Delegate.
	*/
	public static interface ResourceDelegate<T>
	{
		public boolean isLoadable( final String _file ) ;			// Can the Resource Delegate load the file
		public T load( final String _file ) ;						// Load the Resource
	}
}
