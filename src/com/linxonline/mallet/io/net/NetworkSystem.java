package com.linxonline.mallet.io.net ;

public interface NetworkSystem
{
	public ServerConnection createServerConnection( final int _port ) ;

	public Connection createClientConnection( final String _host, final int _port ) ;
}