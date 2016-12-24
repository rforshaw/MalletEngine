package com.linxonline.mallet.io.writer ;

import java.util.Collection ;
import java.io.* ;

import com.linxonline.mallet.io.filesystem.* ;

public final class WriteFile
{
	private WriteFile() {}

	/**
		Writes each String of Collection to a new line of txt file.
	*/
	public static final boolean write( final String _file, final Collection<String> _list )
	{
		return write( GlobalFileSystem.getFile( _file ), _list ) ;
	}

	public static final boolean write( final FileStream _file, final Collection<String> _list )
	{
		final StringOutStream stream = _file.getStringOutStream() ;
		for( final String line : _list )
		{
			stream.writeLine( line ) ;
		}

		return stream.close() ;
	}

	public static final boolean write( final String _file, final String _data )
	{
		return write( GlobalFileSystem.getFile( _file ), _data ) ;
	}

	public static final boolean write( final FileStream _file, final String _data )
	{
		final StringOutStream stream = _file.getStringOutStream() ;
		stream.writeLine( _data ) ;
		return stream.close() ;
	}
}
