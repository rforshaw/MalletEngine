package com.linxonline.mallet.resources ;

public interface ManagerInterface<T>
{
	public boolean add( final String _key, final T _value ) ;
	public T get( final String _file ) ;

	public void clean() ;		// Cleanup unused Resources
	public void clear() ;		// Ceanup all Resources
	
	public void shutdown() ;	// Shutdown connections to systems if required
}