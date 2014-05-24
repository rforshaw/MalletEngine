package com.linxonline.mallet.io.filesystem ;

/**
	Provides global access point to the FileSystem.
	Must be setFileSystem() must be called before game 
	begin running.
*/
public class GlobalFileSystem
{
	private static FileSystem fileSystem = null ;

	private GlobalFileSystem() {}

	public static void setFileSystem( final FileSystem _system )
	{
		fileSystem = _system ;
	}

	public static void scanBaseDirectory()
	{
		fileSystem.scanBaseDirectory() ;
	}

	/**
		Blocks calling Thread
	**/
	public static byte[] getResourceRaw( final String _file )
	{
		return fileSystem.getResourceRaw( _file ) ;
	}

	public static String getResourceAsString( final String _file )
	{
		return fileSystem.getResourceAsString( _file ) ;
	}

	/**
		Doesn't block calling Thread, when resource has started reading,
		it'll call ResourceCallback.
		Return boolean informs whether it successfully started reading.

		Using a length of zero will result in the entire file/stream being returned.
		Specifying a length greater than zero will determine the maximum amount of bytes/lines
		that are read and then passed to the callback. If the end of the stream/file does not reach
		the length size then it is still returned.
	*/
	public static boolean getResourceRaw( final String _file, final int _length, final ResourceCallback _callback )
	{
		return fileSystem.getResourceRaw( _file, _length, _callback ) ;
	}

	public static boolean getResourceAsString( final String _file, final int _length, final ResourceCallback _callback )
	{
		return fileSystem.getResourceAsString( _file, _length, _callback ) ;
	}

	/**
		Blocks calling Thread
	*/
	public static boolean writeResourceAsString( final String _file, final String _data )
	{
		return fileSystem.writeResourceAsString( _file, _data ) ;
	}

	public static boolean writeResourceRaw( final String _file, final byte[] _data )
	{
		return fileSystem.writeResourceRaw( _file, _data ) ;
	}

	/**
		Check to see whether a file or directory exists.
	*/
	public static boolean exist( final String _path )
	{
		return fileSystem.exist( _path ) ;
	}

	/**
		Delete a file or directory represented by the path.
		Deleting a directory will delete its children.
	*/
	public static boolean delete( final String _path )
	{
		return fileSystem.delete( _path ) ;
	}

	/**
		Create the directory structure represented by _path.
	*/
	public boolean makeDirectories( final String _path )
	{
		return fileSystem.makeDirectories( _path ) ;
	}

	public boolean isFile( final String _file )
	{
		return fileSystem.isFile( _file ) ;
	}

	public boolean isDirectory( final String _path )
	{
		return fileSystem.isDirectory( _path ) ;
	}
}
