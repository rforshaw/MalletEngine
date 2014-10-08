package com.linxonline.mallet.io.filesystem ;

/**
	The Aim of the File System is to scan available resources located
	in the base directory.
	
	Resources are then mapped to allow generic and easy access to all 
	content. If a resource is accessed but has not been mapped, then it 
	should be mapped first.
*/
public interface FileSystem
{
	public void scanBaseDirectory() ;

	/**
		Blocks calling Thread
	**/
	public byte[] getResourceRaw( final String _file ) ;
	public String getResourceAsString( final String _file ) ;

	/**
		Doesn't block calling Thread, when resource has started reading,
		it'll call ResourceCallback.
		Return boolean informs whether it successfully started reading.

		Using a length of zero will result in the entire file/stream being returned.
		Specifying a length greater than zero will determine the maximum amount of bytes/lines
		that are read and then passed to the callback. If the end of the stream/file does not reach
		the length size then it is still returned.
	*/
	public boolean getResourceRaw( final String _file, final int _length, final ResourceCallback _callback ) ;
	public boolean getResourceAsString( final String _file, final int _length, final ResourceCallback _callback ) ;

	/**
		Blocks calling Thread
	**/
	public boolean writeResourceAsString( final String _file, final String _data ) ;
	public boolean writeResourceRaw( final String _file, final byte[] _data ) ;

	/**
		Check to see whether a file or directory exists.
	*/
	public boolean exist( final String _path ) ;

	/**
		Delete a file or directory represented by the path.
		Deleting a directory will delete its children.
	*/
	public boolean delete( final String _path ) ;

	/**
		Create the directory structure represented by _path.
	*/
	public boolean makeDirectories( final String _path ) ;

	public boolean isFile( final String _file ) ;
	public boolean isDirectory( final String _path ) ;

	/**
		Returns the filesize in bytes.
	*/
	public long getFileSize( final String _path ) ;
}