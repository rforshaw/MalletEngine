package com.linxonline.mallet.resources.texture ;

import java.util.ArrayList ;
import java.io.* ;

import com.linxonline.mallet.io.reader.* ;
import com.linxonline.mallet.resources.AbstractManager ;

public class SpriteManager extends AbstractManager<Sprite>
{
	public SpriteManager() {}

	@Override
	protected Sprite createResource( final String _file )
	{
		final Sprite sprite = loadSprite( _file ) ;
		if( sprite != null )
		{
			resources.put( _file, sprite ) ;
			return sprite ;
		}

		System.out.println( "Failed to load Sprite: " + _file ) ;
		return null ;
	}

	/**
		Load the animation file using the format:
		Framerate
		image/location
	*/
	protected Sprite loadSprite( final String _file )
	{
		final ArrayList<String> texts = TextReader.getTextFile( _file ) ;

		final int framerate = Integer.parseInt( texts.get( 0 ) ) ;
		final Sprite sprite = new Sprite( framerate ) ;

		final int length = texts.size() ;
		for( int i = 1; i < length; ++i )
		{
			final String line = texts.get( i ) ;
			final String[] frameText = line.split( "\\s*,[,\\s]*" ) ;

			final float u1 = Float.parseFloat( frameText[1] ) ;
			final float v1 = Float.parseFloat( frameText[2] ) ;

			final float u2 = Float.parseFloat( frameText[3] ) ;
			final float v2 = Float.parseFloat( frameText[4] ) ;

			sprite.addFrame( sprite.new Frame( frameText[0], u1, v1, u2, v2 ) ) ;
		}

		return sprite ;
	}
}
