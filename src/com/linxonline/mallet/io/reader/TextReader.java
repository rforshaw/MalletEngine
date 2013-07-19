package com.linxonline.mallet.io.reader ;

import java.io.* ;
import java.util.ArrayList ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

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
		final ArrayList<String> textFile = new ArrayList<String>() ;

		try
		{
			String txt = GlobalFileSystem.getResourceAsString( _file ) ;
			if( txt == null )
			{
				return null ;
			}

			final StringReader fileReader = new StringReader( txt ) ;
			final BufferedReader bufferedReader = new BufferedReader( fileReader ) ;
			String line = null ;

			while( ( line = bufferedReader.readLine() ) != null )
			{
				textFile.add( line ) ;
			}

			bufferedReader.close() ;
		}
		catch( FileNotFoundException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
		catch( IOException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}

		return textFile ;
	}
}