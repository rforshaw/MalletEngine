package com.linxonline.mallet.io.reader ;

import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.game.GameState ;

public abstract class ConfigParser
{
	public ConfigParser() {}

	public static Settings parseSettings( final Settings _src, Settings _dest )
	{
		if( _src == null )
		{
			System.out.println( "Failed to Parse Config File." ) ;
			return _dest ;
		}

		if( _dest == null )
		{
			_dest = new Settings() ;
		}

		String var = null ;
		
		try
		{
			var = _src.getString( "RENDERWIDTH" ) ;
			_dest.addInteger( "RENDERWIDTH", Integer.parseInt( var ) ) ;
		}
		catch( Exception _ex ) {}

		try
		{
			var = _src.getString( "RENDERHEIGHT" ) ;
			_dest.addInteger( "RENDERHEIGHT", Integer.parseInt( var ) ) ;
		}
		catch( Exception _ex ) {}
		
		try
		{
			var = _src.getString( "DISPLAYWIDTH" ) ;
			_dest.addInteger( "DISPLAYWIDTH", Integer.parseInt( var ) ) ;
		}
		catch( Exception _ex ) {}
		
		try
		{
			var = _src.getString( "DISPLAYHEIGHT" ) ;
			_dest.addInteger( "DISPLAYHEIGHT", Integer.parseInt( var ) ) ;
		}
		catch( Exception _ex ) {}
		
		var = _src.getString( "MODE", "GAME_MODE" ) ;
		if( var.equals( "GAME_MODE" ) == true )
		{
			_dest.addInteger( "MODE", GameState.GAME_MODE ) ;
		}
		else if( var.equals( "APPLICATION_MODE" ) == true )
		{
			_dest.addInteger( "MODE", GameState.APPLICATION_MODE ) ;
		}

		return _dest ;
	}
}