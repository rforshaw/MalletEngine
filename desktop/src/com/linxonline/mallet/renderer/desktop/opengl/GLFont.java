package com.linxonline.mallet.renderer.desktop.opengl ;

import com.linxonline.mallet.io.Resource ;

import com.linxonline.mallet.renderer.Shape ;

public final class GLFont extends Resource
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
			return glyphs[_code] ;
		}

		return null ;
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
