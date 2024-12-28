package com.linxonline.mallet.renderer.android.opengl ;

import java.util.Arrays ;

import com.linxonline.mallet.renderer.Texture ;
import com.linxonline.mallet.io.Resource ;

public final class GLImage extends Resource
{
	public final int[] textureIDs ;			// Buffer ID for openGL
	public final long consumption ;

	/**
		Store the texture image reference handle 
		and the amount of memory it has allocated.

		Consumption is used to determine what resources 
		are most suitable for destruction if memory 
		availability becomes an issue.
	*/
	public GLImage( final int _textureID, final long _consumption )
	{
		textureIDs = new int[1] ;
		textureIDs[0] = _textureID ;
		consumption = _consumption ;
	}

	public static int calculateMagFilter( Texture.Filter _filter )
	{
		switch( _filter )
		{
			default          : return MGL.GL_LINEAR ;
			case LINEAR      : return MGL.GL_LINEAR ;
			case NEAREST     : return MGL.GL_NEAREST ;
		}
	}

	public static int calculateMinFilter( Texture.Filter _filter )
	{
		switch( _filter )
		{
			default          : return MGL.GL_LINEAR ;
			case MIP_LINEAR  : return MGL.GL_LINEAR_MIPMAP_LINEAR ;
			case MIP_NEAREST : return MGL.GL_NEAREST_MIPMAP_NEAREST ;
			case LINEAR      : return MGL.GL_LINEAR ;
			case NEAREST     : return MGL.GL_NEAREST ;
		}
	}

	public static int calculateWrap( Texture.Wrap _wrap )
	{
		switch( _wrap )
		{
			default         :
			case REPEAT     : return MGL.GL_REPEAT ;
			case CLAMP_EDGE : return MGL.GL_CLAMP_TO_EDGE ;
		}
	}

	@Override
	public boolean equals( final Object _obj )
	{
		if( this == _obj )
		{
			return true ;
		}

		if( _obj == null )
		{
			return false ;
		}

		if( _obj instanceof GLImage )
		{
			final GLImage image = ( GLImage )_obj ;
			if( textureIDs.length != image.textureIDs.length )
			{
				return false ;
			}

			final int size = textureIDs.length ;
			for( int i = 0; i < size; i++ )
			{
				if( textureIDs[i] != image.textureIDs[i] )
				{
					return false ;
				}
			}
		}

		return true ;
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( textureIDs ) ;
	}

	@Override
	public long getMemoryConsumption()
	{
		return consumption ;
	}

	public final void destroy()
	{
		MGL.glDeleteTextures( 1, textureIDs, 0 ) ;
	}
}
