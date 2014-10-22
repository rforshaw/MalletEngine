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
	public void scanBaseDirectory() ;

	public FileStream getFile( final String _path ) ;
}