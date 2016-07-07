package com.linxonline.mallet.renderer.web.gl ;

import com.linxonline.mallet.renderer.font.Glyph ;
import com.linxonline.mallet.renderer.Shape ;

public class GLGlyph extends Glyph
{
	public Shape shape ;

	public GLGlyph( final char _char, final float _start, final float _advance )
	{
		super( _char, _start, _advance ) ;
	}

	public void setShape( final Shape _shape )
	{
		shape = _shape ;
	}

	@Override
	public void destroy() {}
}