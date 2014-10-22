package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.* ;
import java.util.zip.* ;
import java.util.* ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.logger.Logger ;

import com.linxonline.mallet.io.formats.json.desktop.* ;

/**
	Provides access tot he filesystem on desktop platforms.
	This currently includes: Linux, Windows, & Mac.
*/
public class DesktopFileSystem implements FileSystem
{
	public DesktopFileSystem()
	{
		initJSONConstructors() ;
	}

	/**
		Allows reading and parsing JSON formatted files.
		Mallet Engine provides a wrapper around a platform 
		JSON library.
	*/
	protected void initJSONConstructors()
	{
		DesktopJSONObject.init() ;
		DesktopJSONArray.init() ;
	}

	/**
		Scans the ROOT_DIRECTORY directory and map it to the HashMap.
		Supports Zip files & uncompressed files located within the 
		ROOT_DIRECTORY.
	*/
	@Override
	public void scanBaseDirectory() {}

	@Override
	public FileStream getFile( final String _path )
	{
		return new DesktopFile( new File( _path ) ) ;
	}
}