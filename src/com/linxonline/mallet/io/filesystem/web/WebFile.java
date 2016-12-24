package com.linxonline.mallet.io.filesystem.web ;

import java.util.List ;

import org.teavm.jso.browser.Window ;
import org.teavm.jso.dom.html.HTMLBodyElement ;
import org.teavm.jso.dom.html.HTMLDocument ;
import org.teavm.jso.dom.html.HTMLElement ;
import org.teavm.jso.dom.html.HTMLImageElement ;
import org.teavm.jso.dom.html.HTMLSourceElement ;

import org.teavm.jso.ajax.XMLHttpRequest ;
import org.teavm.jso.ajax.ReadyStateChangeHandler ;
import org.teavm.jso.typedarrays.ArrayBuffer ;
import org.teavm.jso.typedarrays.Int8Array ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.Utility ;

public class WebFile implements FileStream
{
	private static final HTMLDocument document = Window.current().getDocument() ;
	private final String path ;
	private final HTMLSourceElement element ;

	public WebFile( final String _path )
	{
		element = document.createElement( "source" ).cast() ;
		element.setSrc( _path ) ;

		path = _path ;
	}

	public HTMLSourceElement getHTMLSource()
	{
		return element ;
	}

	public HTMLImageElement getHTMLImage()
	{
		final HTMLImageElement img = document.createElement( "img" ).cast() ;
		img.setSrc( path ) ;
		return img ;
	}
	
	public ByteInStream getByteInStream()
	{
		final XMLHttpRequest request = XMLHttpRequest.create() ;
		request.open( "GET", path, false ) ;
		request.send() ;

		final Int8Array array = Int8Array.create( ( ArrayBuffer )request.getResponse() ) ;
		final byte[] data = new byte[array.getByteLength()] ;

		for( int i = 0; i < data.length; ++i )
		{
			data[i] = array.get( i ) ;
		}

		return new WebByteIn( data ) ;
	}

	public StringInStream getStringInStream()
	{
		final XMLHttpRequest request = XMLHttpRequest.create() ;
		request.open( "GET", path, false ) ;
		request.overrideMimeType( "text/plain; charset=x-user-defined" ) ;
		request.send() ;

		return new WebStringIn( request.getResponseText().getBytes() ) ;
	}

	public boolean getByteInCallback( final ByteInCallback _callback, final int _length )
	{
		final XMLHttpRequest request = XMLHttpRequest.create() ;
		request.open( "GET", element.getSrc(), true ) ;
		request.setResponseType( "arraybuffer" ) ;
		request.setOnReadyStateChange( new ReadyStateChangeHandler()
		{
			@Override
			public void stateChanged()
			{
				if( request.getReadyState() != XMLHttpRequest.DONE )
				{
					return;
				}

				if( request.getStatus() != 200 )
				{
					return;
				}

				final Int8Array array = Int8Array.create( ( ArrayBuffer )request.getResponse() ) ;
				final byte[] data = new byte[array.getByteLength()] ;

				for( int i = 0; i < data.length; ++i )
				{
					data[i] = array.get( i ) ;
				}

				final WebByteIn stream = new WebByteIn( data ) ;
				_callback.start() ;
				
				int toReadNum = _length ;
				int readNum = 0 ;

				while( ( readNum > -1 ) && ( toReadNum > ByteInCallback.STOP ) )
				{
					final byte[] buffer = new byte[toReadNum] ;
					readNum = stream.readBytes( buffer, 0, toReadNum ) ;
					toReadNum = _callback.readBytes( buffer, readNum ) ;
				}

				stream.close() ;
				_callback.end() ;
			}
		} ) ;

		request.send() ;
		return true ;
	}

	public boolean getStringInCallback( final StringInCallback _callback, final int _length )
	{
		final XMLHttpRequest request = XMLHttpRequest.create() ;
		request.open( "GET", element.getSrc(), true ) ;
		request.overrideMimeType( "text/plain; charset=x-user-defined" ) ;
		request.setResponseType( "arraybuffer" ) ;
		request.setOnReadyStateChange( new ReadyStateChangeHandler()
		{
			@Override
			public void stateChanged()
			{
				if( request.getReadyState() != XMLHttpRequest.DONE )
				{
					return;
				}

				if( request.getStatus() != 200 )
				{
					return;
				}

				final Int8Array array = Int8Array.create( ( ArrayBuffer )request.getResponse() ) ;
				final byte[] data = new byte[array.getByteLength()] ;

				for( int i = 0; i < data.length; ++i )
				{
					data[i] = array.get( i ) ;
				}

				final WebStringIn stream = new WebStringIn( data ) ;
				int toReadNum = _length ;
				_callback.start() ;

				final List<String> strings = Utility.<String>newArrayList() ;

				String line = null ;
				while( ( ( line = stream.readLine() ) != null ) && ( toReadNum > StringInCallback.STOP ) )
				{
					strings.add( line ) ;
					if( toReadNum == StringInCallback.RETURN_ALL )
					{
						continue ;
					}
					else if( strings.size() >= toReadNum )
					{
						final int size = strings.size() ;
						toReadNum = _callback.resourceAsString( strings.toArray( new String[size] ), size ) ;
						strings.clear() ;
					}
				}

				{
					final int size = strings.size() ;
					_callback.resourceAsString( strings.toArray( new String[size] ), size ) ;
				}

				stream.close() ;
				_callback.end() ;
			}
		} ) ;

		request.send() ;
		return true ;
	}

	public ByteOutStream getByteOutStream()
	{
		return null ;
	}

	public StringOutStream getStringOutStream()
	{
		return null ;
	}

	/**
		Copy the File Stream to the requested location.
		This should only work if the File Stream is a file.
	*/
	public boolean copyTo( final String _dest )
	{
		return false ;
	}

	public boolean isFile()
	{
		return element != null ;
	}

	public boolean isDirectory()
	{
		return false ;
	}

	public boolean exists()
	{
		return element != null ;
	}

	/**
		Delete the File repreented by this File Stream.
		This also includes deleting folders.
		Not supported on web platform.
	*/
	public boolean delete()
	{
		return false ;
	}

	/**
		Create the Directory structure represented 
		by this File Stream.
		Not supported on web platform.
	*/
	public boolean mkdirs()
	{
		return false ;
	}

	/**
		Return the File size of this FileStream.
	*/
	public long getSize()
	{
		return 0L ;
	}

	/**
		Close all streams that a developer has requested.
		This will close streams currently in use, and dead 
		streams.
	*/
	public boolean close()
	{
		return true ;
	}

	private static class ByteInTransfer
	{
		public ByteInStream stream ;

		public ByteInTransfer() {}

		public void set( final ByteInStream _stream )
		{
			stream = _stream ;
		}
	}

	private static class StringInTransfer
	{
		public StringInStream stream ;

		public StringInTransfer() {}

		public void set( final StringInStream _stream )
		{
			stream = _stream ;
		}
	}
}
