package com.linxonline.mallet.io.filesystem ;

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

	public boolean isReadable() ;
	public boolean isWritable() ;

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
		Used to close a specific stream.
	*/
	public boolean close( final Close _close ) ;

	/**
		Close all streams that a developer has requested.
		This will close streams currently in use, and dead 
		streams.
	*/
	public boolean close() ;
}
