package com.linxonline.mallet.io.reader ;

import java.util.List ;

import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.util.Utility ;

/**
	RFReader is a lexical parser of the RF format.

	RF is a custom text text format that operates on a 
	keyword + value basis. Wrapped in { }.

	By default the RFReader stores each pair as a String type.
	If you want to parse them into integers or other types, 
	you'll need to do that yourself.
**/
public abstract class RFReader
{
	private static final String BLANK = "" ;
	private static final String OBRACKET = "{" ;
	private static final String CBRACKET = "}" ;
	private static final String COMMENT = "//" ;
	private static final String QUOTE = "\"" ; 
	private static final String firstDelims = "[\n\t]+" ;
	private static final String secondDelims = " " ;

	private static final List<String> strip = Utility.<String>newArrayList() ;
	private static final StringBuilder buffer = new StringBuilder() ;
	
	public RFReader() {}

	public static List<Settings> loadFile( final String _file )
	{
		final List<String> file = TextReader.getTextAsArray( _file ) ;
		final List<String> stripped = stripComments( file ) ;

		boolean openBracket = false ;
		boolean closedBracket = false ;

		List<String> block = null ;
		final List<List<String>> blocks = Utility.<List<String>>newArrayList() ;

		for( final String line : stripped )
		{
			// Check line to see if it is {
			if( line.contains( OBRACKET ) == true )
			{
				if( openBracket == true && closedBracket == false )
				{
					System.out.println( "Found open bracket before closed bracket" ) ;
					return null ;
				}
				else
				{
					openBracket = true ;
					closedBracket = false ;
					block = Utility.<String>newArrayList() ;
					continue ;
				}
			}

			// Check line to see if it is }
			if( line.contains( CBRACKET ) == true )
			{
				if( closedBracket == true && openBracket == false )
				{
					System.out.println( "Found closed bracket before closed bracket" ) ;
					return null ;
				}
				else
				{
					openBracket = false ;
					closedBracket = true ;
					blocks.add( block ) ;
					continue ;
				}
			}
			
			// Skip line if it appears after a }
			if( closedBracket == true )
			{
				continue ;
			}
			else if( openBracket == true )
			{
				block.add( line ) ;
			}
		}
		
		return parseBlocks( blocks ) ;
	}

	private static List<String> stripComments( final List<String> _file )
	{
		strip.clear() ;
		final int size = _file.size() ;
		String line = null ;

		for( int i = 0; i < size; ++i )
		{
			line = _file.get( i ) ;
			final int index = line.indexOf( COMMENT ) ;
			if( index > 0 )
			{
				line = line.substring( 0, index ) ;
			}

			strip.add( line ) ;
		}

		return strip ;
	}
	
	private static List<Settings> parseBlocks( final List<List<String>> _blocks )
	{
		final int blocksSize = _blocks.size() ;
		final List<Settings> settings = Utility.<Settings>newArrayList( blocksSize ) ;

		for( int i = 0; i < blocksSize; ++i )
		{
			settings.add( parseBlock( _blocks.get( i ) ) ) ;
		}

		return settings ;
	}

	private static Settings parseBlock( final List<String> _block )
	{
		final int blockSize = _block.size() ;
		String line = null ;

		int foundFirstQuote = -1 ;
		int foundSecondQuote = -1 ;

		final Settings object = new Settings() ;
		for( int i = 0; i < blockSize; ++i )
		{
			line = _block.get( i ) ;
			buffer.append( line ) ;

			// Called if FirstQuote hasn't been set yet
			if( foundFirstQuote < 0 )
			{
				foundFirstQuote = line.indexOf( QUOTE ) ;
				
			}

			foundSecondQuote = line.lastIndexOf( QUOTE ) ;
			// Check to ensure we aren't just finding the FirstQuote again
			if( foundSecondQuote == foundFirstQuote )
			{
				foundSecondQuote = -1 ;
			}

			if( foundSecondQuote > 0 )
			{
				// Remove unwanted tabs and newlines
				process( buffer.toString().replaceAll( firstDelims, BLANK ), object ) ;
				buffer.delete( 0, buffer.length() ) ;

				// Reset for next keyword/value combo
				foundFirstQuote = -1 ;
				foundSecondQuote = -1 ;
				continue ;
			}
		}

		return object ;
	}
	
	/**
		Find the Keyword and Value
		Store them in _object as a String
	**/
	private static void process( final String _replace, final Settings _object )
	{
		// Find Value
		final int firstIndex = _replace.indexOf( QUOTE ) ;
		final int lastIndex = _replace.lastIndexOf( QUOTE ) ;
		final String value = _replace.substring( firstIndex + 1, lastIndex ) ;

		// Find Keyword
		final String keyword = _replace.substring( 0, firstIndex - 1 ).toUpperCase() ;
		_object.addString( keyword, value ) ;
	}
}
