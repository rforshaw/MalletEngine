package com.linxonline.mallet.io.filesystem ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

/**
	Should be used by StringInStream, StringOutStream, 
	ByteInStream and ByteOutStream.
	When a stream is requested, the FileStream will 
	retain a reference in toClose, when the FileStream 
	is eventually closed it will cleanup any streams 
	that a developer may have forgotten to close 
	manually.
	This is a safety net, and should not be relied on.
*/
public class CloseStreams implements Close
{
	private final List<Close> toClose = MalletList.<Close>newList() ;

	public CloseStreams() {}

	public Close add( final Close _close )
	{
		toClose.add( _close ) ;
		return _close ;
	}

	/**
		Close the connection and remove from the list 
		of active connections.
	*/
	public boolean remove( final Close _close )
	{
		if( toClose.remove( _close ) == true )
		{
			_close.close() ;
			return true ;
		}
		return false ;
	}

	public boolean isEmpty()
	{
		return toClose.isEmpty() ;
	}

	public boolean close()
	{
		boolean success = true ;
		final int length = toClose.size() ;
		for( int i = 0; i < length; ++i )
		{
			if( toClose.get( i ).close() == false )
			{
				// We still want to iterate over all
				// of the objects, to guarantee they 
				// are closed.
				success = false ;
			}
		}

		// It might be sensible for close() to 
		// throw an exception, allowing us to relay
		// more details about what has failed.
		toClose.clear() ;
		return success ;
	}
}
