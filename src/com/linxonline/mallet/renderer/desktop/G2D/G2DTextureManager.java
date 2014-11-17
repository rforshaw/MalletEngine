package com.linxonline.mallet.renderer.desktop.G2D ;

import javax.imageio.* ;
import java.io.* ;

import com.linxonline.mallet.system.GlobalConfig ;
import com.linxonline.mallet.io.reader.ByteReader ;

import com.linxonline.mallet.resources.AbstractManager ;
import com.linxonline.mallet.resources.texture.Texture ;
import com.linxonline.mallet.util.settings.Settings ;

public class G2DTextureManager extends AbstractManager<Texture>
{
	public G2DTextureManager()
	{
		final ResourceLoader<Texture> loader = getResourceLoader() ;
		loader.add( new ResourceDelegate<Texture>()
		{
			public boolean isLoadable( final String _file )
			{
				return true ;
			}

			public Texture load( final String _file, final Settings _settings )
			{
				final String file = redirectResourceLocation( _file ) ;

				try
				{
					final byte[] image = ByteReader.readBytes( file ) ;
					if( image == null )
					{
						return null ;
					}

					final InputStream in = new ByteArrayInputStream( image ) ;
					return new Texture( new G2DImage( ImageIO.read( in ) ) ) ;
				}
				catch( IOException _ex )
				{
					_ex.printStackTrace() ;
				}

				return null ;
			}

			/**
				Implemented to display high-res textures when developing
				an iOS app on a normal computer.
				Simulates the @2x addition when using Retina display.
				Should provide flag to prevent automatic usage.
			*/
			protected String redirectResourceLocation( final String _file )
			{
				String file = _file ;
				final int diff = GlobalConfig.getInteger( "DISPLAYWIDTH", 800 ) / GlobalConfig.getInteger( "RENDERWIDTH", 800 ) ;
				if( diff >= 2 )
				{
					final StringBuilder builder = new StringBuilder( _file ) ;
					final int index = builder.lastIndexOf( "." ) ;
					builder.insert( index, "@2x" ) ;
					file = builder.toString() ;
				}

				return file ;
			}
		} ) ;
	}
}