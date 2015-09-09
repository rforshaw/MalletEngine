package com.linxonline.mallet.renderer.android.GL ;

import com.linxonline.mallet.renderer.font.Glyph ;

public class GLGlyph extends Glyph
{
	public GLGeometry index ;

	public GLGlyph( final char _char, final float _start, final float _advance )
	{
		super( _char, _start, _advance ) ;
	}

	public void setIndex( final GLGeometry _index )
	{
		index = _index ;
	}

	@Override
	public void destroy()
	{
		if( index != null )
		{
			index.destroy() ;
		}
	}

	public GLGeometry getGLGeometry()
	{
		return index ;
	}
}