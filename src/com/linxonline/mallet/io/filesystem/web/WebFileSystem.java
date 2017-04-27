package com.linxonline.mallet.io.filesystem.web ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.io.formats.json.web.* ;

public class WebFileSystem implements FileSystem
{
	public WebFileSystem()
	{
		initJSONConstructors() ;
	}

	/**
		Map all of the files contained within _directory.
		Recursively go through all directories within 
		_directory and map any files found.
	*/
	public boolean mapDirectory( final String _directory )
	{
		return false ;
	}

	/**
		Return a FileStream that represents _path.
		A FileStream can be a directory or file, 
		a returned FileStream does not guarantee that a 
		directory, or file exists. However you can use 
		the FileStream to create a directory or file.
	*/
	public FileStream getFile( final String _path )
	{
		return new WebFile( _path ) ;
	}

	@Override
	public String getHomeDirectory( final String _projectName )
	{
		final StringBuilder builder = new StringBuilder() ;
		// Need to define a Home Directory for Web implementations.
		return builder.toString() ;
	}

	/**
		Allows reading and parsing JSON formatted files.
		Mallet Engine provides a wrapper around a platform 
		JSON library.
	*/
	protected void initJSONConstructors()
	{
		WebJSONObject.init() ;
		WebJSONArray.init() ;
	}
}
