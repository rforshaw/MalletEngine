package com.linxonline.mallet.io.net ;

import java.util.Map ;

import java.net.SocketAddress ;
import java.net.InetSocketAddress ;
import java.net.DatagramSocket ;
import java.nio.channels.DatagramChannel ;
import java.nio.ByteBuffer ;
import java.net.UnknownHostException ;
import java.net.SocketException ;
import java.net.SocketTimeoutException ;
import java.io.IOException ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.io.serialisation.Serialise ;

/**
	UDPServer is designed to receive information from 
	the specified address and port. 
	It can then send a response to the sender using the 
	address and port provided by the sender.
*/
public class UDPServer implements AutoCloseable, IServer
{
	private final Map<Address, InetSocketAddress> addresses = MalletMap.<Address, InetSocketAddress>newMap() ;

	private DatagramChannel channel ;
	private byte[] sendBuffers = new byte[200] ;
	private final Address sourceAddress = new Address() ;

	private final Serialise.ByteOut out = new Serialise.ByteOut( null ) ;
	
	public UDPServer() {}

	public boolean init( final Address _address, final int _timeout )
	{
		try
		{
			if( channel != null )
			{
				close() ;
			}

			channel = DatagramChannel.open() ;
			channel.bind( create( _address ) ) ;
			channel.configureBlocking( false ) ;

			DatagramSocket socket = channel.socket() ;
			socket.setSoTimeout( _timeout ) ;

			return true ;
		}
		catch( Exception ex )
		{
			ex.printStackTrace() ;
			return false ;
		}
	}

	@Override
	public boolean send( final Address _address, final IOutStream _out )
	{
		try
		{
			final int length = _out.getLength() ;
			if( sendBuffers.length < length )
			{
				sendBuffers = new byte[length] ;
			}

			out.set( sendBuffers ) ;
			_out.serialise( out ) ;

			final ByteBuffer buffer = ByteBuffer.wrap( sendBuffers, 0, length ) ;
			channel.send( buffer, create( _address ) ) ;
			return true ;
		}
		catch( IOException ex )
		{
			return false ;
		}
	}

	@Override
	public InStream receive( final InStream _stream )
	{
		try
		{
			final ByteBuffer wrap = ByteBuffer.wrap( _stream.getBuffer() ) ;
			final SocketAddress source = channel.receive( wrap ) ;
			if( source == null )
			{
				return null ;
			}

			sourceAddress.set( source ) ;

			_stream.setSender( sourceAddress ) ;
			_stream.setDataLength( wrap.position() ) ;

			return _stream ;
		}
		catch( IOException ex )
		{
			return null ;
		}
	}

	private InetSocketAddress create( final Address _address )
	{
		InetSocketAddress add = addresses.get( _address ) ;
		if( add != null )
		{
			return add ;
		}

		add = new InetSocketAddress( _address.getHost(), _address.getPort() ) ;
		addresses.put( _address, add ) ;
		return add ;
	}

	@Override
	public void close() throws Exception
	{
		channel.close() ;
	}
}
