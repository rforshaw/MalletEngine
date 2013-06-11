package com.linxonline.mallet.io.reader ;

import java.io.* ;
import java.util.ArrayList ;

import com.linxonline.mallet.resources.* ;

public abstract class TextReader
{
	public TextReader() {}
	
	/**
		Returns an array of strings, one string per line.
	*/
	static public ArrayList<String> getTextFile( final String _file )
	{
		return readFile( _file ) ;
	}
	
	static private ArrayList<String> readFile( final String _file )
	{
		final ResourceManager resources = ResourceManager.getResourceManager() ;
		final ArrayList<String> textFile = new ArrayList<String>() ;

		try
		{
			String txt = resources.getFileSystem().getResourceAsString( _file ) ;
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