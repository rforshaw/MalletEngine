package com.linxonline.mallet.io.reader.config ;

import java.util.List ;

import com.linxonline.mallet.io.reader.ParseInterface ;
import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.ui.UI ;

public class ConfigParser
{
	private final List<ParseInterface> parsers = MalletList.<ParseInterface>newList() ;

	public ConfigParser()
	{
		init() ;
	}

	public ConfigParser( final ParseInterface _parse )
	{
		addParser( _parse ) ;
	}

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
		parsers.add( new Config() ) ;
	}
	
	private static class Config implements ParseInterface
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

			var = _src.getString( "TEXTURECOMPRESSION", "true" ) ;
			_dest.addBoolean( "TEXTURECOMPRESSION", Boolean.parseBoolean( var ) ) ;

			var = _src.getString( "CAPTUREMOUSE", "false" ) ;
			_dest.addBoolean( "CAPTUREMOUSE", Boolean.parseBoolean( var ) ) ;

			var = _src.getString( "KEEPRATIO", "true" ) ;
			_dest.addBoolean( "KEEPRATIO", Boolean.parseBoolean( var ) ) ;

			var = _src.getString( "DISPLAYRENDERPARITY", "false" ) ;
			_dest.addBoolean( "DISPLAYRENDERPARITY", Boolean.parseBoolean( var ) ) ;

			var = _src.getString( "MASTERVOLUME", "100" ) ;
			_dest.addInteger( "MASTERVOLUME", Integer.parseInt( var ) ) ;

			var = _src.getString( "MUSICVOLUME", "100" ) ;
			_dest.addInteger( "MUSICVOLUME", Integer.parseInt( var ) ) ;

			var = _src.getString( "VOCALVOLUME", "100" ) ;
			_dest.addInteger( "VOCALVOLUME", Integer.parseInt( var ) ) ;

			var = _src.getString( "EFFECTVOLUME", "100" ) ;
			_dest.addInteger( "EFFECTVOLUME", Integer.parseInt( var ) ) ;

			var = _src.getString( "DOUBLEBUFFER", "true" ) ;
			_dest.addBoolean( "DOUBLEBUFFER", Boolean.parseBoolean( var ) ) ;

			var = _src.getString( "DPIX", null ) ;
			if( var != null )
			{
				// We make the assumption that if no DPI is 
				// specified we use the monitors DPI.
				_dest.addInteger( "DPIX", Integer.parseInt( var ) ) ;
			}

			var = _src.getString( "DPIY", null ) ;
			if( var != null )
			{
				// We make the assumption that if no DPI is 
				// specified we use the monitors DPI.
				_dest.addInteger( "DPIY", Integer.parseInt( var ) ) ;
			}

			var = _src.getString( "UI_UNIT", "CENTIMETRE" ) ;
			_dest.addObject( "UI_UNIT", UI.Unit.derive( var ) ) ;
		}
	}
}
