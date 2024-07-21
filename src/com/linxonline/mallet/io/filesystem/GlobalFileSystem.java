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
		fileSystem = _system ;
	}

	/**
		Recursively map files located within the specified directory.
		Zip files contained within the directory will also be mapped.
	*/
	public static boolean mapDirectory( final String _directory )
	{
		return fileSystem.mapDirectory( _directory ) ;
	}

	/**
		Return the FileStream associated with the passed in path.
		Files mapped using mapDirectory will be returned first, if 
		they exist.
	*/
	public static FileStream getFile( final String _path )
	{
		return fileSystem.getFile( _path ) ;
	}

	/**
		Return the expected applications home directory path.
		Return the application's internal storage path on Android.

		The home directory should be used by the developer 
		to store application save data. This may include save 
		files, user configs or mod data.

		Any data stored within the home directory will be deleted 
		when uninstalling from an Android device.

		iOS and Web has yet to be determined.
	*/
	public static String getHomeDirectory( final String _projectName )
	{
		return fileSystem.getHomeDirectory( _projectName ) ;
	}

	/**
		Determine whether the passed in _path is one of the 
		specified extensions.

		Returns true if the path is one of the specified 
		extensions.
	*/
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
