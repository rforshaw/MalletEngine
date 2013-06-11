package com.linxonline.mallet.io.filesystem ;

import com.linxonline.mallet.resources.* ;

public abstract class DeleteFile
{
	final static ResourceManager resources = ResourceManager.getResourceManager() ;
	
	public static boolean delete( final String _file )
	{
		return resources.getFileSystem().deleteResource( _file ) ;
	}
}