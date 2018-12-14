package com.linxonline.mallet.renderer.android.GL ;

import java.util.Arrays ;

import com.linxonline.mallet.io.Resource ;

public class GLImage extends Resource
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
