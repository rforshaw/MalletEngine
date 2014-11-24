package com.linxonline.mallet.resources.android ;

import java.util.HashMap ;

import android.content.res.Resources ;
import android.graphics.BitmapFactory ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.io.reader.ByteReader ;
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
		
		final ResourceLoader<Texture> loader = getResourceLoader() ;
		loader.add( new ResourceDelegate<Texture>()
		{
			public boolean isLoadable( final String _file )
			{
				return true ;
			}

			public Texture load( final String _file, final Settings _settings )
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
				final byte[] image = ByteReader.readBytes( _file ) ;
				if( _file != null )
				{
					return new Texture( new AndroidImage( BitmapFactory.decodeByteArray( image, 0, image.length ) ) ) ;
				}

				return null ;
			}

			private boolean isResource( String _file )
			{
				return _file.contains( ":drawable/" ) ;
			}
		} ) ;
	}
}
