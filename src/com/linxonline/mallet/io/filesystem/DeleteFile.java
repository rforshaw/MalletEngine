package com.linxonline.mallet.io.filesystem ;

public abstract class DeleteFile
{
	public static boolean delete( final String _file )
	{
		return GlobalFileSystem.delete( _file ) ;
	}
}