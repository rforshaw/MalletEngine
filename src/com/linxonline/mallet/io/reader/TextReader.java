package com.linxonline.mallet.io.reader ;

import java.util.ArrayList ;

import com.linxonline.mallet.io.filesystem.* ;

public class TextReader
{
	private TextReader() {}

	/**
		Returns an array of strings, one string per line.
	*/
	public static ArrayList<String> getTextAsArray( final String _file )
	{
		return readFileToArray( _file ) ;
	}

	public static String getTextAsString( final String _file )
	{
		return readFileToString( _file ) ;
	}

	private static String readFileToString( final String _file )
	{
		final StringBuilder buffer = new StringBuilder() ;
		final FileStream file = GlobalFileSystem.getFile( _file ) ;

		if( file.exists() == false )
		{
			System.out.println( "Failed to read file: " + _file ) ;
			return buffer.toString() ;
		}

		final StringInStream in = file.getStringInStream() ;
		String line = null ;

		while( ( line = in.readLine() ) != null )
		{
			buffer.append( line ) ;
			buffer.append( '\n' ) ;
		}

		in.close() ;
		return buffer.toString() ;
	}

	private static ArrayList<String> readFileToArray( final String _file )
	{
		final ArrayList<String> lines = new ArrayList<String>() ;
		final FileStream file = GlobalFileSystem.getFile( _file ) ;

		if( file.exists() == false )
		{
			System.out.println( "Failed to read file: " + _file ) ;
			return lines ;
		}

		final StringInStream in = file.getStringInStream() ;
		String line = null ;

		while( ( line = in.readLine() ) != null )
		{
			lines.add( line ) ;
		}

		in.close() ;
		return lines ;
	}
}