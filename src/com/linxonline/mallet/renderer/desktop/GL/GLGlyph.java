package com.linxonline.mallet.renderer.desktop.GL ;

import com.linxonline.mallet.renderer.font.Glyph ;

public class GLGlyph extends Glyph
{
	public final GLGeometry index ;

	public GLGlyph( final GLGeometry _index, final char _char, final float _start, final float _advance )
	{
		super( _char, _start, _advance ) ;
		index = _index ;
	}

	@Override
	public void destroy()
	{
		index.destroy() ;
	}
}