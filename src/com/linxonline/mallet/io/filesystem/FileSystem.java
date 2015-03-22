package com.linxonline.mallet.io.filesystem ;

/**
	The Aim of the File System is to scan available resources located
	in the base directory.
	
	Resources are then mapped to allow generic and easy access to all 
	content. If a resource is accessed but has not been mapped, then it 
	should be mapped first.
*/
public interface FileSystem
{
	/**
		Map all of the files contained within _directory.
		Recursively go through all directories within 
		_directory and map any files found.
	*/
	public boolean mapDirectory( final String _directory ) ;

	
	/**
		Return a FileStream that represents _path.
		A FileStream can be a directory or file, 
		a returned FileStream does not guarantee that a 
		directory, or file exists. However you can use 
		the FileStream to create a directory or file.
	*/
	public FileStream getFile( final String _path ) ;
}