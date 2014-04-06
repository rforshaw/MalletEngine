package com.linxonline.mallet.resources ;

public class GlobalLanguage
{
	private final static LanguageManager language = new LanguageManager() ;
	
	private GlobalLanguage() {}
	
	public static void setLanguage( final String _language )
	{
		language.setLanguage( _language ) ;
	}

	public static boolean loadLanguageFile( final String _file )
	{
		return language.loadLanguageFile( _file ) ;
	}

	public static boolean containsLanguageFile( final String _file )
	{
		return language.containsLanguageFile( _file ) ;
	}

	public static String getText( final String _keyword )
	{
		return language.getText( _keyword ) ;
	}

	public static void clear()
	{
		language.clear() ;
	}
}