package com.linxonline.mallet.io.language ;

public final class GlobalLanguage
{
	private final static LanguageManager language = new LanguageManager() ;

	private GlobalLanguage() {}

	public static void setLanguage( final String _language )
	{
		language.setLanguage( _language ) ;
	}

	/**
		Load the language file into the specified namespace.
	*/
	public boolean load( final String _namespace, final String _file )
	{
		return language.load( _namespace, _file ) ;
	}

	public void remove( final String _namespace )
	{
		language.remove( _namespace ) ;
	}

	public String get( final String _namespace, final String _keyword )
	{
		return language.get( _namespace, _keyword ) ;
	}
}
