package com.linxonline.mallet.renderer.desktop.GL ;

import com.linxonline.mallet.resources.model.Model ;
import com.linxonline.mallet.renderer.font.Glyph ;

public class GLGlyph extends Glyph
{
	public final Model model ;

	public GLGlyph( final Model _model, final char _char, final float _start, final float _advance )
	{
		super( _char, _start, _advance ) ;
		model = _model ; 
	}

	@Override
	public void destroy()
	{
		model.destroy() ;
	}

	public GLGeometry getGLGeometry()
	{
		return model.getGeometry( GLGeometry.class ) ;
	}
}