package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.Arrays ;

import java.awt.image.BufferedImage ;
import javax.media.opengl.* ;

import com.linxonline.mallet.renderer.texture.ImageInterface ;

public class GLImage implements ImageInterface
{
	public final int[] textureIDs ;			// Buffer ID for openGL
	private final int width ;				// Width of texture
	private final int height ;				// Height of texture

	public GLImage( int _textureID, final int _width, final int _height )
	{
		textureIDs = new int[1] ;
		textureIDs[0] = _textureID ;

		width = _width ;
		height = _height ;
	}

	public final int getWidth()
	{
		return width ;
	}

	public final int getHeight()
	{
		return height ;
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

	public final void destroy()
	{
		//System.out.println( "Removing texture.." ) ;
		GLRenderer.getCanvas().getContext().makeCurrent() ;						// Get GL's Attention
		final GL3 gl = GLRenderer.getCanvas().getContext().getCurrentGL().getGL3() ;
		if( gl != null )
		{
			gl.glDeleteTextures( textureIDs.length, textureIDs, 0 ) ;
		}
		GLRenderer.getCanvas().getContext().release() ;
	}
}
