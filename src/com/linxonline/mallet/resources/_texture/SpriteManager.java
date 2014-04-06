package com.linxonline.mallet.resources ;

import java.util.ArrayList ;
import java.io.* ;

import com.linxonline.mallet.io.reader.* ;

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
		ArrayList<String> texts = TextReader.getTextFile( _file ) ;

		int framerate = Integer.parseInt( texts.get( 0 ) ) ;
		Sprite sprite = new Sprite( framerate ) ;

		final int length = texts.size() ;
		for( int i = 1; i < length; ++i )
		{
			sprite.addTexture( texts.get( i ) ) ;
		}

		return sprite ;
	}
}
