package com.linxonline.mallet.io ;

import java.util.Set ;

/**
	Provide a consistant interface for loading resources.
	All resource loading managers should implement this 
	interface, or use AbstractManager for a general 
	implementation that will cover most cases.
*/
public interface ILoader<K, T extends Resource>
{
	public T put( final K _key, final T _value  ) ;
	public T get( final K _key ) ;

	public ResourceLoader<K, T> getResourceLoader() ;
	public long getMemoryConsumption() ;

	public void clean( final Set<K> _activeKeys ) ;		// Cleanup unused Resources
	public void clear() ;										// Ceanup all Resources

	public void shutdown() ;	// Shutdown connections to systems if required

	/**
		Manage multiple Resource Delegates.
		Provides a central access point to load 
		a file-type without specifically having to know 
		what Resource Delegate to use.
	*/
	public static interface ResourceLoader<K, T>
	{
		public void add( final ResourceDelegate<K, T> _delegate ) ;
		public void remove( final ResourceDelegate<K, T> _delegate ) ;

		public T load( final K _key ) ;
	}

	/**
		Implement to handle the loading of a specific file type.
		A Resource Delegate can be used to load/decode 
		multiple files types. If you have a range of different 
		files types, implement them in their own Resource Delegate.
	*/
	public static interface ResourceDelegate<K, T>
	{
		public boolean isLoadable( final K _key ) ;			// Can the Resource Delegate load the file
		public T load( final K _key ) ;						// Load the Resource
	}
}
