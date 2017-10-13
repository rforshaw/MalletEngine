package com.linxonline.mallet.io.reader ;

import java.util.List ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.MalletList ;

public class TextReader
{
	private TextReader() {}

	/**
		Returns an array of strings, one string per line.
	*/
	public static List<String> getTextAsArray( final String _file )
	{
		return readFileToArray( _file ) ;
	}

	/**
		Returns an array of strings, one string per line.
	*/
	public static List<String> getTextAsArray( final FileStream _file )
	{
		return readFileToArray( _file ) ;
	}

	/**
		Returns an array of strings, one string per line.
	*/
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

		file.close() ;
		return buffer.toString() ;
	}

	private static List<String> readFileToArray( final String _file )
	{
		final FileStream file = GlobalFileSystem.getFile( _file ) ;
		if( file.exists() == false )
		{
			System.out.println( "Failed to read file: " + _file ) ;
			return MalletList.<String>newList() ;
		}

		return readFileToArray( file ) ;
	}

	private static List<String> readFileToArray( final FileStream _file )
	{
		final List<String> lines = MalletList.<String>newList() ;
		final StringInStream in = _file.getStringInStream() ;
		String line = null ;

		while( ( line = in.readLine() ) != null )
		{
			lines.add( line ) ;
		}

		in.close() ;
		return lines ;
	}
}
