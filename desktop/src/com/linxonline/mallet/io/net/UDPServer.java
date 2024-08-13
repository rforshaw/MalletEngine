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
	private ByteBuffer sendBuffer = ByteBuffer.allocate( 200 ) ;
	private ByteBuffer receiveBuffer = ByteBuffer.allocate( 200 ) ;
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
			if( sendBuffer.capacity() < length )
			{
				sendBuffer = ByteBuffer.allocate( length ) ;
			}

			out.set( sendBuffer.array() ) ;
			_out.serialise( out ) ;

			sendBuffer.position( 0 ) ;
			sendBuffer.limit( length ) ;

			channel.send( sendBuffer, create( _address ) ) ;
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
			final byte[] buffer = _stream.getBuffer() ;
			if( receiveBuffer.capacity() < buffer.length )
			{
				receiveBuffer = ByteBuffer.allocate( buffer.length ) ;
			}

			receiveBuffer.position( 0 ) ;
			final SocketAddress source = channel.receive( receiveBuffer ) ;
			if( source == null )
			{
				return null ;
			}

			final int length = receiveBuffer.position() ;
			System.arraycopy( receiveBuffer.array(), 0, buffer, 0, length ) ;

			sourceAddress.set( source ) ;

			_stream.setSender( sourceAddress ) ;
			_stream.setDataLength( length ) ;

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
