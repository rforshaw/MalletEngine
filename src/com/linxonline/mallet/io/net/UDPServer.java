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
import com.linxonline.mallet.io.filesystem.Close ;
import com.linxonline.mallet.io.serialisation.Serialise ;

/**
	UDPServer is designed to receive information from 
	the specified address and port. 
	It can then send a response to the sender using the 
	address and port provided by the sender.
*/
public class UDPServer implements Close
{
	private final Map<Address, InetSocketAddress> addresses = MalletMap.<Address, InetSocketAddress>newMap() ;

	private DatagramChannel channel ;
	private byte[] sendBuffers = new byte[200] ;

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
		catch( IOException ex )
		{
			ex.printStackTrace() ;
			return false ;
		}
	}

	public boolean send( final Address _address, final IOutStream _out )
	{
		try
		{
			final int length = _out.getLength() ;
			if( sendBuffers.length < length )
			{
				sendBuffers = new byte[length] ;
			}

			_out.serialise( new Serialise.ByteOut( sendBuffers ) ) ;
			final ByteBuffer buffer = ByteBuffer.wrap( sendBuffers, 0, length ) ;

			channel.send( buffer, create( _address ) ) ;
			return true ;
		}
		catch( IOException ex )
		{
			return false ;
		}
	}

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

			_stream.setSender( new Address( source ) ) ;
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
	public boolean close()
	{
		try
		{
			channel.close() ;
			return true ;
		}
		catch( IOException ex )
		{
			return false ;
		}
	}
}
