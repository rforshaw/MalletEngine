package com.linxonline.mallet.io.net ;

import java.net.SocketAddress ;
import java.net.InetSocketAddress ;

public class Address
{
	private String host ;
	private int port ;

	public Address()
	{
		this( "", -1 ) ;
	}

	public Address( final String _host, final int _port )
	{
		host = _host ;
		port = _port ;
	}

	public Address( final SocketAddress _address )
	{
		set( _address ) ;
	}

	public void setHost( final String _host )
	{
		host = _host ;
	}

	public void setPort( final int _port )
	{
		port = _port ;
	}

	public void set( final Address _address )
	{
		host = _address.host ;
		port = _address.port ;
	}

	public void set( final SocketAddress _address )
	{
		if( _address instanceof InetSocketAddress add )
		{
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
		if( _obj instanceof Address addr )
		{
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
