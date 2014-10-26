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
		assert _system != null ;
		fileSystem = _system ;
	}

	public static boolean mapDirectory( final String _directory )
	{
		return fileSystem.mapDirectory( _directory ) ;
	}

	public static FileStream getFile( final String _path )
	{
		return fileSystem.getFile( _path ) ;
	}
}
