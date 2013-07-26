package com.linxonline.mallet.io.reader ;

import java.util.ArrayList ;

import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.game.GameState ;

public class ConfigParser
{
	private final ArrayList<ParseInterface> parsers = new ArrayList<ParseInterface>() ;

	public ConfigParser() { init() ; }

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

				var = _src.getString( "MODE", "GAME_MODE" ) ;
				if( var.equals( "GAME_MODE" ) == true )
				{
					_dest.addInteger( "MODE", GameState.GAME_MODE ) ;
				}
				else if( var.equals( "APPLICATION_MODE" ) == true )
				{
					_dest.addInteger( "MODE", GameState.APPLICATION_MODE ) ;
				}
			}
		} ) ;
	}
}