package com.linxonline.mallet.io.writer ;

import java.util.ArrayList ;
import java.io.* ;

import com.linxonline.mallet.io.filesystem.* ;

public class WriteFile
{
	private WriteFile() {}

	/**
		Writes each String of ArrayList to a new line of txt file.
	*/
	public static final boolean write( final String _file, final ArrayList<String> _list )
	{
		final FileStream file = GlobalFileSystem.getFile( _file ) ;
		final StringOutStream stream = file.getStringOutStream() ;

		for( final String line : _list )
		{
			stream.writeLine( line ) ;
		}

		return stream.close() ;
	}

	public static final boolean write( final String _file, final String _data )
	{
		final FileStream file = GlobalFileSystem.getFile( _file ) ;
		final StringOutStream stream = file.getStringOutStream() ;

		stream.writeLine( _data ) ;

		return stream.close() ;
	}
}