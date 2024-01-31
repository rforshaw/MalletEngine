package com.linxonline.mallet.io.filesystem.web ;

import java.util.List ;

import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;

import org.teavm.jso.JSObject ;
import org.teavm.jso.browser.Window ;
import org.teavm.jso.browser.Location ;
import org.teavm.jso.dom.html.HTMLBodyElement ;
import org.teavm.jso.dom.html.HTMLDocument ;
import org.teavm.jso.dom.html.HTMLElement ;
import org.teavm.jso.dom.html.HTMLImageElement ;
import org.teavm.jso.dom.html.HTMLSourceElement ;
import org.teavm.jso.dom.events.* ;

import org.teavm.jso.ajax.XMLHttpRequest ;
import org.teavm.jso.ajax.ReadyStateChangeHandler ;
import org.teavm.jso.typedarrays.ArrayBuffer ;
import org.teavm.jso.typedarrays.Int8Array ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.MalletList ;

public class WebFile implements FileStream
{
	private static final HTMLDocument document = Window.current().getDocument() ;
	private static final Location location = Location.current() ;
	
	private final String path ;
	private final String url ;
	private final HTMLSourceElement element ;

	public WebFile( final String _path )
	{
		element = document.createElement( "source" ).cast() ;
		element.setSrc( _path ) ;

		path = _path ;
		url = String.format( "%s%s", location.getFullURL(), path ) ;
	}

	public String getURL()
	{
		return url ;
	}

	public HTMLSourceElement getHTMLSource()
	{
		return element ;
	}

	public HTMLImageElement getHTMLImage()
	{
		return get( url ) ;
	}

	@Async
	public static native HTMLImageElement get( String _url ) ;
	private static void get( String _url, AsyncCallback<HTMLImageElement> _callback )
	{
		final HTMLImageElement img = document.createElement( "img" ).cast() ;
		img.setSrc( _url ) ;
		document.getBody().appendChild( img ) ;

		img.getStyle().setProperty( "display", "none" ) ;
		img.addEventListener( "load", new EventListener()
		{
			@Override
			public void handleEvent( final Event _event )
			{
				_callback.complete( img ) ;
			}
		} ) ;
	}

	@Override
	public ByteInStream getByteInStream()
	{
		final XMLHttpRequest request = XMLHttpRequest.create() ;
		request.open( "get", url, false ) ;
		request.overrideMimeType( "text/plain; charset=x-user-defined" ) ;
		request.send() ;

		final String responseText = request.getResponseText() ;
		final byte[] result = new byte[responseText.length()] ;
		for( int i = 0; i < result.length; ++i)
		{
			result[i] = ( byte )responseText.charAt( i ) ;
		}

		return new WebByteIn( result ) ;
	}

	@Override
	public StringInStream getStringInStream()
	{
		final XMLHttpRequest request = XMLHttpRequest.create() ;
		request.open( "get", url, false ) ;
		request.overrideMimeType( "text/plain; charset=x-user-defined" ) ;
		request.send() ;

		return new WebStringIn( request.getResponseText().getBytes() ) ;
	}

	@Override
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

				try
				{
					stream.close() ;
				}
				catch( Exception ex )
				{
					ex.printStackTrace() ;
				}
				_callback.end() ;
			}
		} ) ;

		request.send() ;
		return true ;
	}

	@Override
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

				final List<String> strings = MalletList.<String>newList() ;

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

				try
				{
					stream.close() ;
				}
				catch( Exception ex )
				{
					ex.printStackTrace() ;
				}
				_callback.end() ;
			}
		} ) ;

		request.send() ;
		return true ;
	}

	@Override
	public ByteOutStream getByteOutStream()
	{
		return null ;
	}

	@Override
	public StringOutStream getStringOutStream()
	{
		return null ;
	}

	public boolean create()
	{
		return false ;
	}

	/**
		Copy the File Stream to the requested location.
		This should only work if the File Stream is a file.
	*/
	@Override
	public boolean copyTo( final String _dest )
	{
		return false ;
	}

	@Override
	public boolean isFile()
	{
		return element != null ;
	}

	@Override
	public boolean isDirectory()
	{
		return false ;
	}

	@Override
	public boolean isReadable()
	{
		return exists() ;
	}

	@Override
	public boolean isWritable()
	{
		return false ;
	}

	@Override
	public boolean exists()
	{
		return element != null ;
	}

	/**
		Delete the File repreented by this File Stream.
		This also includes deleting folders.
		Not supported on web platform.
	*/
	@Override
	public boolean delete()
	{
		return false ;
	}

	/**
		Create the Directory structure represented 
		by this File Stream.
		Not supported on web platform.
	*/
	@Override
	public boolean mkdirs()
	{
		return false ;
	}

	@Override
	public String[] list()
	{
		return new String[0] ;
	}

	/**
		Return the File size of this FileStream.
	*/
	@Override
	public long getSize()
	{
		final XMLHttpRequest request = XMLHttpRequest.create() ;
		request.open( "get", url, false ) ;
		request.overrideMimeType( "text/plain; charset=x-user-defined" ) ;
		request.send() ;

		final String text = request.getResponseText() ;
		return text.length() ;
	}
}
