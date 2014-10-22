package com.linxonline.mallet.io.reader ;

import java.util.ArrayList ;

import com.linxonline.mallet.io.filesystem.* ;

public class TextReader
{
	private TextReader() {}

	/**
		Returns an array of strings, one string per line.
	*/
	public static ArrayList<String> getTextFile( final String _file )
	{
		return readFile( _file ) ;
	}

	private static ArrayList<String> readFile( final String _file )
	{
		final FileStream file = GlobalFileSystem.getFile( _file ) ;
		final StringInStream in = file.getStringInStream() ;

		final ArrayList<String> lines = new ArrayList<String>() ;
		String line = null ;

		while( ( line = in.readLine() ) != null )
		{
			lines.add( line ) ;
		}

		return lines ;
	}
}