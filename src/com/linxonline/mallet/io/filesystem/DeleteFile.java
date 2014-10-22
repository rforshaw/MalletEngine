package com.linxonline.mallet.io.filesystem ;

public abstract class DeleteFile
{
	public static boolean delete( final String _file )
	{
		final FileStream file = GlobalFileSystem.getFile( _file ) ;
		return ( file != null ) ? false : file.delete() ;
	}
}