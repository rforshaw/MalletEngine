package com.linxonline.mallet.io.net ;

import java.net.InetAddress ;
import java.net.UnknownHostException ;

public class Address
{
	private final String host ;
	private final InetAddress address ;

	public Address( final String _host )
	{
		host = _host ;
		address = null ;
	}
	
	public Address( final InetAddress _address )
	{
		host = null ;
		address = _address ;
	}

	@Override
	public int hashCode()
	{
		return ( host != null ) ? host.hashCode() : address.hashCode() ;
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
			if( host != null )
			{
				return host.equals( addr.host ) ;
			}

			return address.equals( addr.address ) ;
		}

		return false ;
	}

	public InetAddress createInetAddress() throws UnknownHostException
	{
		return ( address != null ) ? address : InetAddress.getByName( host ) ;
	}
}
