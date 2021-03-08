package com.linxonline.mallet.io.net ;

import java.net.InetAddress ;
import java.net.DatagramSocket ;
import java.net.DatagramPacket ;
import java.net.UnknownHostException ;
import java.net.SocketException ;
import java.net.SocketTimeoutException ;
import java.io.IOException ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.io.filesystem.Close ;
import com.linxonline.mallet.io.serialisation.Serialise ;

/**
	UDPClient is designed to send information to 
	the specified address and port passed into init().
	
*/
public class UDPClient implements Close
{
	private DatagramSocket socket ;
	private DatagramPacket sendPacket ;
	private DatagramPacket recvPacket ;

	private byte[] sendBuffers = new byte[200] ;
	private byte[] recvBuffers = new byte[200] ;

	public UDPClient() {}

	public boolean init( final int _port, final Address _address, final int _timeout )
	{
		try
		{
			if( socket != null )
			{
				socket.close() ;
			}

			socket = new DatagramSocket() ;
			socket.setSoTimeout( _timeout ) ;

			sendPacket = new DatagramPacket( sendBuffers, 0, sendBuffers.length, _address.createInetAddress(), _port ) ;
			recvPacket = new DatagramPacket( recvBuffers, 0, recvBuffers.length ) ;
			return true ;
		}
		catch( UnknownHostException ex )
		{
			ex.printStackTrace() ;
			return false ;
		}
		catch( SocketException ex )
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

			sendPacket.setData( sendBuffers, 0, length ) ;
			_out.serialise( new Serialise.ByteOut( sendBuffers ) ) ;

			socket.send( sendPacket ) ;
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
			final byte[] buffer = _stream.getBuffer() ;
			recvPacket.setData( buffer, 0, buffer.length ) ;

			socket.receive( recvPacket ) ;
			//_stream.setSender( recvPacket.getPort(), new Address( recvPacket.getAddress() ) ) ;
			_stream.setDataLength( recvPacket.getLength() ) ;

			return _stream ;
		}
		catch( SocketTimeoutException ex )
		{
			return null ;
		}
		catch( IOException ex )
		{
			return null ;
		}
	}

	@Override
	public boolean close()
	{
		socket.close() ;
		return true ;
	}
}
