package com.linxonline.mallet.renderer.desktop.GL ;

import com.linxonline.mallet.renderer.font.Glyph ;
import com.linxonline.mallet.renderer.Shape ;

public class GLGlyph extends Glyph
{
	public final Shape shape ;

	public GLGlyph( final Shape _shape, final char _char, final float _start, final float _advance )
	{
		super( _char, _start, _advance ) ;
		shape = _shape ;
	}

	@Override
	public void destroy() {}
}