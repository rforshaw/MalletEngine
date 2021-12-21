package com.linxonline.mallet.io.net ;

import java.net.SocketAddress ;
import java.net.InetSocketAddress ;

public class Address
{
	private final String host ;
	private final int port ;

	public Address( final String _host, final int _port )
	{
		host = _host ;
		port = _port ;
	}

	public Address( final SocketAddress _address )
	{
		if( _address instanceof InetSocketAddress )
		{
			final InetSocketAddress add = ( InetSocketAddress )_address ;
			host = add.getHostName() ;
			port = add.getPort() ;
			return ;
		}

		throw new RuntimeException( "SocketAdress not castable to InetSocketAddress" ) ;
	}

	public int getPort()
	{
		return port ;
	}

	public String getHost()
	{
		return host ;
	}

	@Override
	public int hashCode()
	{
		return host.hashCode() * port ;
	}

	@Override
	public boolean equals( final Object _obj )
	{
		if( _obj == null )
		{
			return false ;
		}
	
		if( _obj instanceof Address )
		{
			final Address addr = ( Address )_obj ;
			if( host.equals( addr.host ) == false )
			{
				return false ;
			}

			if( port != addr.port )
			{
				return false ;
			}
		}

		return true ;
	}
}
