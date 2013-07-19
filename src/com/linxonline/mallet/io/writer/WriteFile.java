package com.linxonline.mallet.io.writer ;

import java.util.ArrayList ;
import java.io.* ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

public class WriteFile
{
	private WriteFile() {}

	/**
		Writes each String of ArrayList to a new line of txt file.
	**/
	public static final boolean write( final String _file, ArrayList<String> _list )
	{
		final StringBuffer buffer = new StringBuffer() ;
		final int size = _list.size() ;
		String line = null ;

		for( int i = 0; i < size; ++i )
		{
			line = _list.get( i ) ;
			buffer.append( line + "\n" ) ;
		}

		return write( _file, buffer.toString() ) ;
	}

	public static final boolean write( final String _file, final String _data )
	{
		if( _file.length() < 1 )
		{
			return false ;
		}

		
		return GlobalFileSystem.writeResourceAsString( _file, _data ) ;
	}
}