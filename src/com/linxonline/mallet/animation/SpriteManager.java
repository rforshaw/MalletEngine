package com.linxonline.mallet.animation ;

import java.util.List ;
import java.io.* ;

import com.linxonline.mallet.io.reader.* ;
import com.linxonline.mallet.resources.AbstractManager ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.Utility ;

public class SpriteManager extends AbstractManager<Sprite>
{
	public SpriteManager()
	{
		final ResourceLoader<Sprite> loader = getResourceLoader() ;
		loader.add( new ResourceDelegate<Sprite>()
		{
			public boolean isLoadable( final String _file )
			{
				return _file.endsWith( ".anim" ) ;
			}

			/**
				Load the animation file using the format:
				Framerate
				image/location
			*/
			public Sprite load( final String _file, final Settings _settings )
			{
				final List<String> texts = TextReader.getTextAsArray( _file ) ;
				if( texts.size() == 0 )
				{
					return null ;
				}

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

					sprite.addFrame( new Sprite.Frame( frameText[0], u1, v1, u2, v2 ) ) ;
				}

				return sprite ;
			}
		} ) ;
	}
}
