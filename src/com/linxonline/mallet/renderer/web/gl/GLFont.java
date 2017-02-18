package com.linxonline.mallet.renderer.web.gl ;

import com.linxonline.mallet.resources.Resource ;

import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.texture.Texture ;

public class GLFont extends Resource
{
	private final Shape[] glyphs ;
	private final Texture<GLImage> texture ;

	public GLFont( final Shape[] _glyphs, final Texture<GLImage> _texture )
	{
		glyphs = _glyphs ;
		texture = _texture ;
	}

	public Texture<GLImage> getTexture()
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
	public void destroy()
	{
		texture.destroy() ;
	}
}
