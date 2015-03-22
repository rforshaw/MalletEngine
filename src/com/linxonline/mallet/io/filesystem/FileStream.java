package com.linxonline.mallet.io.filesystem ;

import java.util.ArrayList ;

public interface FileStream extends Close
{

	public ByteInStream getByteInStream() ;
	public StringInStream getStringInStream() ;

	public boolean getByteInCallback( final ByteInCallback _callback, final int _length ) ;
	public boolean getStringInCallback( final StringInCallback _callback, final int _length ) ;

	public ByteOutStream getByteOutStream() ;
	public StringOutStream getStringOutStream() ;

	/**
		Copy the File Stream to the requested location.
		This should only work if the File Stream is a file.
	*/
	public boolean copyTo( final String _dest ) ;

	public boolean isFile() ;
	public boolean isDirectory() ;

	public boolean exists() ;

	/**
		Delete the File repreented by this File Stream.
		This also includes deleting folders.
	*/
	public boolean delete() ;

	/**
		Create the Directory structure represented 
		by this File Stream.
	*/
	public boolean mkdirs() ;

	/**
		Return the File size of this FileStream.
	*/
	public long getSize() ;

	/**
		Close all streams that a developer has requested.
		This will close streams currently in use, and dead 
		streams.
	*/
	public boolean close() ;

	
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
	public static class CloseStreams implements Close
	{
		private final ArrayList<Close> toClose = new ArrayList<Close>() ;

		public CloseStreams() {}

		public Close add( final Close _close )
		{
			toClose.add( _close ) ;
			return _close ;
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
}