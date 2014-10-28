package com.linxonline.mallet.io.filesystem.android ;

import java.io.File ;

public class AndroidFileTraversal
{
	public AndroidFileTraversal() {}
	
	public final void traverse( final File _file )
	{
		if( _file.isDirectory() == true )
		{
			foundDirectory( _file ) ;
			final File[] children = _file.listFiles() ;
			for( File child : children )
			{
				traverse( child ) ;
			}

			return ;
		}

		foundFile( _file ) ;
	}

	/**
		Called when a Directory is found.
	**/
	public void foundDirectory( final File _directory ) {}

	/**
		Called when a File is found
	**/
	public void foundFile( final File _file ) {}
}