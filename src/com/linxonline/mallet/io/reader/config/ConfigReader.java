package com.linxonline.mallet.io.reader.config ;

import java.util.List ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;

import com.linxonline.mallet.io.reader.TextReader ;
import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.util.Logger ;

public abstract class ConfigReader
{
	public ConfigReader() {}

	public static Settings getConfig( final String _file )
	{
		return getConfig( GlobalFileSystem.getFile( _file ) ) ;
	}

	public static Settings getConfig( final FileStream _stream )
	{
		final List<String> file = TextReader.getTextAsArray( _stream ) ;
		final Settings config = new Settings() ;

		for( final String line : file )
		{
			final String[] split = line.split( " " ) ;
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

		//Logger.println( keyword + " : " + buffer.toString(), Logger.Verbosity.MINOR ) ;
		_config.addString( keyword, buffer.toString() ) ;
	}
}
