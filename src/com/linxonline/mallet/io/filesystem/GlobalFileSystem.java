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
	*/
	public static boolean getResourceRaw( final String _file, final ResourceCallback _callback )
	{
		return fileSystem.getResourceRaw( _file, _callback ) ;
	}

	public static boolean getResourceAsString( final String _file, final ResourceCallback _callback )
	{
		return fileSystem.getResourceAsString( _file, _callback ) ;
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
		return writeResourceRaw( _file, _data ) ;
	}

	public static boolean doesResourceExist( final String _file )
	{
		return fileSystem.doesResourceExist( _file ) ;
	}

	public static boolean deleteResource( final String _file )
	{
		return fileSystem.deleteResource( _file ) ;
	}
}
