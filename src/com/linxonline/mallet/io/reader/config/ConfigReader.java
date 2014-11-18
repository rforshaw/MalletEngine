package com.linxonline.mallet.io.reader.config ;

import java.util.ArrayList ;

import com.linxonline.mallet.io.reader.TextReader ;
import com.linxonline.mallet.util.settings.* ;

public abstract class ConfigReader
{
	public ConfigReader() {}
	
	public static Settings getConfig( final String _file )
	{
		final ArrayList<String> file = TextReader.getTextFile( _file ) ;
		final Settings config = new Settings() ;

		for( String line : file )
		{
			String[] split = line.split( " " ) ;
			if( split.length >= 2 )
			{
				process( split, config ) ;
			}
		}

		return config ;
	}

	private static void process( final String[] _split, final Settings _config )
	{
		final String keyword = _split[0].toUpperCase() ;			// Ensure keyword is UPPERCASE
		final StringBuffer buffer = new StringBuffer() ;

		for( int i = 1; i < _split.length; i++ )
		{
			buffer.append( _split[i] ) ;
		}

		_config.addString( keyword, buffer.toString() ) ;
	}
}
