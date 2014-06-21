package com.linxonline.mallet.resources.language ;

import java.util.HashMap ;
import java.util.ArrayList ;
import java.io.* ;

import com.linxonline.mallet.io.reader.* ;

/*====================================================*/
// Lanaguage Manager allows a developer to write 
// applications with multi-language support.
/*====================================================*/
public class LanguageManager
{
	private HashMap<String, String> strings = new HashMap<String, String>() ;
	private String languageFolder = "en" ;
	private ArrayList<String> filesLoaded = new ArrayList<String>() ;

	public LanguageManager() {}

	/** 
		setLangauge defines what folder within base/languages
		the Manager will look in.
		By Default this is "en" -> base/langagues/en
	**/
	public void setLanguage( final String _language )
	{
		assert _language != null ;
		languageFolder = _language ;
	}

	/** 
		Specifies what langauge file you wish to load.
		If you wish to support multiple langauges,
		simply stick each lanaguage text in their own 
		directory (en, fr), but have them use the same 
		file names(main.lan).
	**/
	public boolean loadLanguageFile( final String _file )
	{
		assert _file != null ;
		final String file = "base/languages/" + languageFolder + "/" + _file ;
		ArrayList<String> textFile = TextReader.getTextFile( file ) ;
		if( textFile == null )
		{
			return false ;
		}

		filesLoaded.add( _file ) ;
		return loadFile( textFile ) ;
	}

	/**
		Check to see if the file has already been loaded
	**/
	public boolean containsLanguageFile( final String _file )
	{
		assert _file != null ;
		for( String file : filesLoaded )
		{
			if( _file.equals( file ) == true )
			{
				return true ;
			}
		}

		return false ;
	}

	/**
		Get the Language specific text, using the defined 
		keyword.
	**/
	public String getText( final String _keyword )
	{
		if( exists( _keyword ) == true )
		{
			return strings.get( _keyword ) ;
		}

		return null ;
	}

	public void clear()
	{
		strings.clear() ;
		filesLoaded.clear() ;
	}

	private final boolean exists( final String _keyword )
	{
		assert _keyword != null ;
		return strings.containsKey( _keyword ) ;
	}

	private final boolean loadFile( ArrayList<String> _textFile )
	{
		for( String line : _textFile )
		{
			String[] split = line.split( " " ) ;
			if( split.length >= 2 )
			{
				process( split ) ;
			}
		}

		return true ;
	}
	
	private void process( String[] _split )
	{
		final String keyword = _split[0].toUpperCase() ;
		final StringBuffer buffer = new StringBuffer() ;
		
		for( int i = 1; i < _split.length; i++ )
		{
			buffer.append( _split[i] ) ;
		}
		
		strings.put( keyword, buffer.toString() ) ;
	}
}