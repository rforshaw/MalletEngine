package com.linxonline.mallet.io.filesystem ;

/**
	Provides global access point to the FileSystem.
	Must be setFileSystem() must be called before game 
	begin running.
*/
public final class GlobalFileSystem
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

	public static boolean isExtension( final String _path, final String ... _extensions )
	{
		for( final String extension : _extensions )
		{
			if( _path.endsWith( extension ) == true )
			{
				return true ;
			}
		}

		return false ;
	}
}
