package com.linxonline.mallet.renderer.desktop.G2D ;

import javax.imageio.* ;
import java.io.* ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.resources.AbstractManager ;
import com.linxonline.mallet.resources.texture.Texture ;

public class G2DTextureManager extends AbstractManager<Texture>
{
	public G2DTextureManager() {}

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
		final String file = redirectResourceLocation( _file ) ;

		try
		{
			byte[] image = GlobalFileSystem.getResourceRaw( file ) ;
			if( image == null )
			{
				image = GlobalFileSystem.getResourceRaw( _file ) ;
			}

			if( image != null )
			{
				final InputStream in = new ByteArrayInputStream( image ) ;
				return new Texture( new G2DImage( ImageIO.read( in ) ) ) ;
			}
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
			StringBuilder builder = new StringBuilder( _file ) ;
			final int index = builder.lastIndexOf( "." ) ;
			builder.insert( index, "@2x" ) ;
			file = builder.toString() ;
		}

		return file ;
	}
}