package com.linxonline.mallet.animation ;

import java.util.List ;

import com.linxonline.mallet.io.reader.* ;

public class SpriteAssist
{
	private SpriteAssist() {}

	public static MalletSprite.Meta create( final String _file ) throws IllegalStateException
	{
		final List<String> texts = TextReader.getTextAsArray( _file ) ;
		if( texts.size() == 0 )
		{
			throw new IllegalStateException( "Malformed Sprite file: " + _file ) ;
		}

		final int length = texts.size() ;

		final int framerate = Integer.parseInt( texts.get( 0 ) ) ;
		final MalletSprite.Frame[] frames = new MalletSprite.Frame[length - 1] ;

		for( int i = 1; i < length; ++i )
		{
			final String line = texts.get( i ) ;
			final String[] frameText = line.split( "\\s*,[,\\s]*" ) ;

			final float u1 = Float.parseFloat( frameText[1] ) ;
			final float v1 = Float.parseFloat( frameText[2] ) ;

			final float u2 = Float.parseFloat( frameText[3] ) ;
			final float v2 = Float.parseFloat( frameText[4] ) ;

			frames[i - 1] = new MalletSprite.Frame( frameText[0], u1, v1, u2, v2 ) ;
		}

		return new MalletSprite.Meta( _file, framerate, frames ) ;
	}
}
