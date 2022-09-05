package com.linxonline.mallet.io.language ;

import java.util.Map ;

import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.io.reader.* ;
import com.linxonline.mallet.io.formats.json.JObject ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;

/**
	Allow the use of different languages within the engine.
	Use setLanguage() to specify the directory to load.
	Use load() to load a .lang file and map it to a namespace.
	Load files with the format specified below:
	{
		"KEYWORD1": {
			"response": "This is text.",
			"description": "This is context for the response."
		},
		"KEYWORD1": {
			"response": "This is more text."
			"description": "This is context for the response."
		}
	}
*/
public class LanguageManager
{
	private static final String LANGUAGE_DEFAULT = "en" ;
	private static final String EMPTY_STRING = "" ;

	private final Map<String, JObject> lookup = MalletMap.<String, JObject>newMap() ;
	private String languageFolder = "en" ;

	public LanguageManager() {}

	/** 
		setLangauge defines what folder within base/languages
		the Manager will look in.
		By Default this is "en" -> base/langagues/en
	**/
	public void setLanguage( final String _language )
	{
		languageFolder = ( _language != null ) ? _language : LANGUAGE_DEFAULT ;
	}

	public String getLanguage()
	{
		return languageFolder ;
	}

	/** 
		Specifies what langauge file you wish to load.
		If you wish to support multiple langauges,
		simply stick each lanaguage text in their own 
		directory (en, fr), but have them use the same 
		file names(main.lan).
	**/
	public boolean load( final String _namespace, final String _file )
	{
		if( lookup.get( _namespace ) != null )
		{
			Logger.println( "Language namespace already in use.", Logger.Verbosity.NORMAL ) ;
			return false ;
		}

		if( GlobalFileSystem.isExtension( _file, ".lang", ".LANG" ) == false )
		{
			Logger.println( "File extension is not recognised.", Logger.Verbosity.NORMAL ) ;
			return false ;
		}

		final String file = "base/languages/" + languageFolder + "/" + _file ;
		final FileStream stream = GlobalFileSystem.getFile( file ) ;
		if( stream.exists() == false )
		{
			Logger.println( "Language file does not exist.", Logger.Verbosity.NORMAL ) ;
			return false ;
		}

		final JObject map = JObject.construct( stream ) ;
		lookup.put( _namespace, map ) ;
		return true ;
	}

	public void remove( final String _namespace )
	{
		lookup.remove( _namespace ) ;
	}

	/**
		Get the Language specific text, using the defined 
		keyword.
	**/
	public String get( final String _namespace, final String _keyword )
	{
		final JObject map = lookup.get( _namespace ) ;
		if( map == null )
		{
			return _keyword ;
		}

		final JObject item = map.optJObject( _keyword, null ) ;
		if( item == null )
		{
			return _keyword ;
		}

		return item.optString( "response", _keyword ) ;
	}
}
