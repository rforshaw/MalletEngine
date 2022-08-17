package com.linxonline.mallet.io.net ;

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
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.io.serialisation.Serialise ;

/**
	UDPClient is designed to send information to 
	the specified address and port passed into init().
	
*/
public class UDPClient implements AutoCloseable
{
	private DatagramChannel channel ;
	private SocketAddress target ;

	private byte[] sendBuffers = new byte[200] ;
	private final Address sourceAddress = new Address() ;

	private final Serialise.ByteOut out = new Serialise.ByteOut( null ) ;

	public UDPClient() {}

	public boolean init( final Address _target, final int _timeout )
	{
		try
		{
			if( channel != null )
			{
				close() ;
			}

			target = new InetSocketAddress( _target.getHost(), _target.getPort() ) ;

			channel = DatagramChannel.open() ;
			channel.bind( new InetSocketAddress( 0 ) ) ;
			channel.configureBlocking( false ) ;

			DatagramSocket socket = channel.socket();
			socket.setSoTimeout( _timeout ) ;

			return true ;
		}
		catch( Exception ex )
		{
			ex.printStackTrace() ;
			return false ;
		}
	}

	public boolean send( final IOutStream _out )
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
			channel.send( buffer, target ) ;
			return true ;
		}
		catch( IOException ex )
		{
			return false ;
		}
	}

	public InStream receive( final InStream _stream  )
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

	@Override
	public void close() throws Exception
	{
		channel.close() ;
	}
}
