package com.linxonline.mallet.io.reader.config ;

import java.util.ArrayList ;

import com.linxonline.mallet.io.reader.ParseInterface ;
import com.linxonline.mallet.util.settings.* ;

public class ConfigParser
{
	private final ArrayList<ParseInterface> parsers = new ArrayList<ParseInterface>() ;

	public ConfigParser() { init() ; }

	/**
		Allows the developer to extend the config to accept custom settings.
	*/
	public void addParser( final ParseInterface _parse )
	{
		parsers.add( _parse ) ;
	}

	public Settings parseSettings( final Settings _src, Settings _dest )
	{
		if( _src == null ) { return _dest ; }
		if( _dest == null ) { _dest = new Settings() ; }

		for( final ParseInterface parse : parsers )
		{
			parse.parse( _src, _dest ) ;
		}

		return _dest ;
	}

	private void init()
	{
		parsers.add( new ParseInterface()
		{
			public void parse( final Settings _src, final Settings _dest )
			{
				String var = null ;
				var = _src.getString( "RENDERWIDTH", "640" ) ;
				_dest.addInteger( "RENDERWIDTH", Integer.parseInt( var ) ) ;

				var = _src.getString( "RENDERHEIGHT", "480" ) ;
				_dest.addInteger( "RENDERHEIGHT", Integer.parseInt( var ) ) ;

				var = _src.getString( "DISPLAYWIDTH", "640" ) ;
				_dest.addInteger( "DISPLAYWIDTH", Integer.parseInt( var ) ) ;

				var = _src.getString( "DISPLAYHEIGHT", "480" ) ;
				_dest.addInteger( "DISPLAYHEIGHT", Integer.parseInt( var ) ) ;

				var = _src.getString( "VSYNC", "0" ) ;
				_dest.addInteger( "VSYNC", Integer.parseInt( var ) ) ;

				var = _src.getString( "FULLSCREEN", "false" ) ;
				_dest.addBoolean( "FULLSCREEN", Boolean.parseBoolean( var ) ) ;

				var = _src.getString( "DISPLAYMOUSE", "false" ) ;
				_dest.addBoolean( "DISPLAYMOUSE", Boolean.parseBoolean( var ) ) ;

				var = _src.getString( "MAXFPS", "60" ) ;
				_dest.addInteger( "MAXFPS", Integer.parseInt( var ) ) ;
			}
		} ) ;
	}
}