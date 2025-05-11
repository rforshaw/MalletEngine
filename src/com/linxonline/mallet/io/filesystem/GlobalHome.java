package com.linxonline.mallet.io.filesystem ;

import com.linxonline.mallet.util.Tuple ;

/**
	Most applications will require a location to store files 
	within the users home directory/My Documents/Internal Storage.
	Depending on what platform the application is running on.

	Before GlobalHome can be used you will need to specify the 
	project name, which is used when constructing the application 
	storage location.
*/
public final class GlobalHome
{
	private static Home home ;

	private GlobalHome() {}

	public static void setHome( final String _projectName )
	{
		home = new Home( _projectName ) ;
	}

	/**
		Automatically amend the home directory to _path.
		Return the FileStream associated with this path.
	*/
	public static FileStream getFile( final String _path )
	{
		return GlobalFileSystem.getFile( getHomeDirectory() + _path ) ;
	}

	/**
		Copy the files located within the application's directory 
		and copy it to the application's home directory.
	*/
	@SafeVarargs
	public static boolean copy( final Tuple<String, String> ... _paths )
	{
		if( home != null )
		{
			return home.copy( _paths ) ;
		}
		return false ;
	}

	public static String getHomeDirectory()
	{
		if( home != null )
		{
			return home.getHomeDirectory() ;
		}
		return null ;
	}
}
