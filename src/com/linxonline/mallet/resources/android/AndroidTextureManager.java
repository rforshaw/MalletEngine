package com.linxonline.mallet.resources.android ;

import java.util.HashMap ;

import android.content.res.Resources ;
import android.graphics.BitmapFactory ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.resources.texture.* ;

import com.linxonline.mallet.resources.android.* ;

public class AndroidTextureManager extends AbstractManager<Texture>
{
	private final Resources activityResources ;
	private BitmapFactory.Options options = new BitmapFactory.Options() ;

	public AndroidTextureManager( final Resources _resources )
	{
		options.inScaled = false ;
		activityResources = _resources ;
	}

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
		if( isResource( _file ) == true )
		{
			return loadTextureFromResource( _file ) ;
		}

		return loadTextureFromPath( _file ) ;
	}
	
	private Texture loadTextureFromResource( String _file )
	{
		final int id = activityResources.getIdentifier( _file, null, null ) ;
		return new Texture( new AndroidImage( BitmapFactory.decodeResource( activityResources, id, options ) ) ) ;
	}
	
	private Texture loadTextureFromPath( String _file )
	{
		byte[] image = GlobalFileSystem.getResourceRaw( _file ) ;
		return new Texture( new AndroidImage( BitmapFactory.decodeByteArray( image, 0, image.length ) ) ) ;
	}

	private boolean isResource( String _file )
	{
		return _file.contains( ":drawable/" ) ;
	}
}
