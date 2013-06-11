package com.linxonline.mallet.resources ;

public interface ManagerInterface
{
	public boolean add( final String _key, final Resource _value ) ;
	public Resource get( final String _file ) ;

	public void clean() ;		// Cleanup unused Resources
	public void clear() ;		// Ceanup all Resources
	
	public void shutdown() ;	// Shutdown connections to systems if required
}