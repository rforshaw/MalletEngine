package com.linxonline.mallet.io.filesystem ;

import com.linxonline.mallet.util.Tuple ;

public class Home
{
	private final String homeDirectory ;

	public Home( final String _projectName )
	{
		homeDirectory = GlobalFileSystem.getHomeDirectory( _projectName ) ;
	}

	public boolean copy( final Tuple<String, String> ... _paths )
	{
		final FileStream stream = GlobalFileSystem.getFile( getHomeDirectory() ) ;
		if( stream.exists() == false )
		{
			// If the application directory doesn't exist then 
			// we need to construct it before we copy the 
			// base files across.
			if( stream.mkdirs() == false )
			{
				System.out.println( "Unable to build application home directory." ) ;
				// This could be caused by lack of permissions..
				return false ;
			}
		}

		for( final Tuple<String, String> path : _paths )
		{
			final FileStream fromStream = GlobalFileSystem.getFile( path.getLeft() ) ;
			if( fromStream.exists() == false || fromStream.isFile() == false )
			{
				System.out.println( "Can't copy: " + path.getLeft() ) ;
				return false ;
			}

			final String destination = getHomeDirectory() + path.getRight() ;
			if( GlobalFileSystem.getFile( destination ).exists() == false )
			{
				// If the destination file already exists
				// there is no point copying it again.
				if( fromStream.copyTo( destination ) == false )
				{
					System.out.println( "Failed to copy: " + path.getLeft() + " to: " + destination ) ;
					return false ;
				}
			}
		}

		return true ;
	}

	public String getHomeDirectory()
	{
		return homeDirectory ;
	}

	public String toString()
	{
		return homeDirectory ;
	}
}
