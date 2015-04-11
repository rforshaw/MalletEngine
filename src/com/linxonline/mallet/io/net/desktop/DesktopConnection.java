package com.linxonline.mallet.io.net.desktop ;

import java.net.Socket ;
import java.io.OutputStreamWriter ;
import java.io.IOException ;

import com.linxonline.mallet.io.net.* ;
import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.filesystem.desktop.* ;

public class DesktopConnection implements Connection
{
	private final CloseStreams toClose = new CloseStreams() ;
	private final Socket socket ;

	public DesktopConnection( final Socket _socket )
	{
		assert _socket != null ;
		socket = _socket ;
	}

	public ByteInStream getByteInStream()
	{
		try
		{
			return ( DesktopByteIn )toClose.add( new DesktopByteIn( socket.getInputStream() )) ;
		}
		catch( IOException ex )
		{
			return null ;
		}
	}

	public StringInStream getStringInStream()
	{
		try
		{
			return ( DesktopStringIn )toClose.add( new DesktopStringIn( socket.getInputStream() )) ;
		}
		catch( IOException ex )
		{
			return null ;
		}
	}

	public boolean getByteInCallback( final ByteInCallback _callback, final int _length )
	{
		return ReadFile.getRaw( getByteInStream(), _callback, _length ) ;
	}

	public boolean getStringInCallback( final StringInCallback _callback, final int _length )
	{
		return ReadFile.getString( getStringInStream(), _callback, _length ) ;
	}

	public ByteOutStream getByteOutStream()
	{
		try
		{
			return ( DesktopByteOut )toClose.add( new DesktopByteOut( socket.getOutputStream() ) ) ;
		}
		catch( IOException ex )
		{
			return null ;
		}
	}

	public StringOutStream getStringOutStream()
	{
		try
		{
			return ( DesktopStringOut )toClose.add( new DesktopStringOut( new OutputStreamWriter( socket.getOutputStream() ) ) ) ;
		}
		catch( IOException ex )
		{
			return null ;
		}
	}

	/**
		Close all the stream input/output that has 
		been returned and close them.
	*/
	public boolean close()
	{
		return toClose.close() ;
	}
}