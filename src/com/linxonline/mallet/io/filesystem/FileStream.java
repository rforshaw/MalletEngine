package com.linxonline.mallet.io.filesystem ;

import java.util.ArrayList ;

public interface FileStream extends Close
{
	public ByteInStream getByteInStream() ;
	public StringInStream getStringInStream() ;

	public boolean getByteInCallback( final ByteInCallback _callback, final int _length ) ;
	public boolean getByteInCallback( final StringInCallback _callback, final int _length ) ;

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

	public boolean close() ;

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
					success = false ;
				}
			}

			toClose.clear() ;
			return success ;
		}
	}
}