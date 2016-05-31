package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Ratio ;

/**
	Provides access to the Meta information of a texture
	without the requirement to manage a texture buffer.
	Provides meta data to the developer without the 
	need to access Renderer/Platform specific texture 
	implementations, or wait for the Renderer to load 
	the requested texture.
*/
public class MalletTexture
{
	private final Meta meta ;

	public MalletTexture( final String _texturePath )
	{
		meta = TextureAssist.createMeta( _texturePath ) ;
	}

	public String getPath()
	{
		return meta.path ;
	}

	public int getHeight()
	{
		return meta.height ;
	}

	public int getWidth()
	{
		return meta.width ;
	}

	public Ratio getRatio()
	{
		return meta.ratio ;
	}

	public String toString()
	{
		return meta.toString() ;
	}

	public static class Meta
	{
		public final String path ;
		public final int height ;
		public final int width ;
		public final Ratio ratio ;

		public Meta( final String _path,
					 final int _height,
					 final int _width )
		{
			path = _path ;
			width = _width ;
			height = _height ;
			ratio = Ratio.calculateRatio( width, height ) ;
		}

		public String toString()
		{
			final StringBuilder buffer = new StringBuilder() ;
			buffer.append( "Path: " + path + '\n' ) ;
			buffer.append( "Height: " + height + '\n' ) ;
			buffer.append( "Width: " + width + '\n') ;
			buffer.append( "Ratio: " + ratio ) ;
			return buffer.toString() ;
		}
	}
}