package com.linxonline.mallet.renderer.android.GL ;

import com.linxonline.mallet.io.Resource ;

import com.linxonline.mallet.renderer.Shape ;

public class GLFont extends Resource
{
	private final Shape[] glyphs ;
	private final GLImage texture ;

	public GLFont( final Shape[] _glyphs, final GLImage _texture )
	{
		glyphs = _glyphs ;
		texture = _texture ;
	}

	public GLImage getTexture()
	{
		return texture ;
	}

	public Shape getShapeWithChar( final char _char )
	{
		return getShapeWithCode( ( int )_char ) ;
	}
	
	public Shape getShapeWithCode( final int _code )
	{
		if( _code < glyphs.length )
		{
			final Shape glyph = glyphs[_code] ;
			if( glyph != null )
			{
				return glyph ;
			}
		}

		return getShapeWithChar( '\0' ) ;
	}

	@Override
	public long getMemoryConsumption()
	{
		return texture.getMemoryConsumption() ;
	}

	@Override
	public void destroy()
	{
		texture.destroy() ;
	}
}
