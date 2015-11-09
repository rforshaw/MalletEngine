package com.linxonline.mallet.renderer.desktop.GL ;

import com.linxonline.mallet.renderer.font.Glyph ;
import com.linxonline.mallet.renderer.Shape ;

public class GLGlyph extends Glyph
{
	public final int index ;

	public GLGlyph( final int _index, final char _char, final float _start, final float _advance )
	{
		super( _char, _start, _advance ) ;
		index = _index ;
	}

	@Override
	public void destroy() {}
}