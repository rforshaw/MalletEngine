package com.linxonline.mallet.resources ;

import javax.imageio.* ;
import java.io.* ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.resources.texture.* ;

public class TextureManager extends AbstractManager
{
	public TextureManager() {}

	@Override
	protected Texture createResource( final String _file )
	{
		Texture texture = loadTexture( _file ) ;
		if( texture != null )
		{
			resources.put( _file, texture ) ;
			return texture ;
		}

		System.out.println( "Failed to create Texture: " + _file ) ;
		return null ;
	}

	protected Texture loadTexture( final String _file )
	{
		final ResourceManager resources = ResourceManager.getResourceManager() ;
		final String file = redirectResourceLocation( _file ) ;

		try
		{
			byte[] image = resources.getFileSystem().getResourceRaw( file ) ;
			if( image == null )
			{
				image = resources.getFileSystem().getResourceRaw( _file ) ;
			}

			InputStream in = new ByteArrayInputStream( image ) ;
			return new Texture( new JavaImage( ImageIO.read( in ) ) ) ;
		}
		catch( IOException _ex )
		{
			_ex.printStackTrace() ;
		}

		return null ;
	}

	protected String redirectResourceLocation( final String _file )
	{
		final ResourceManager resources = ResourceManager.getResourceManager() ;
		final Settings config = resources.getConfig() ;

		String file = _file ;
		final int diff = config.getInteger( "DISPLAYWIDTH", 800 ) / config.getInteger( "RENDERWIDTH", 800 ) ;
		if( diff >= 2 )
		{
			StringBuilder builder = new StringBuilder( _file ) ;
			final int index = builder.lastIndexOf( "." ) ;
			builder.insert( index, "@2x" ) ;
			file = builder.toString() ;
		}

		return file ;
	}
}