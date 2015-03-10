package com.linxonline.mallet.renderer.android.GL ;

import com.linxonline.mallet.resources.model.Model ;
import com.linxonline.mallet.renderer.font.Glyph ;

public class GLGlyph extends Glyph
{
	public Model model ;

	public GLGlyph( final char _char, final float _start, final float _advance )
	{
		super( _char, _start, _advance ) ;
	}

	public void setModel( final Model _model )
	{
		model = _model ;
	}
	
	public GLGeometry getGLGeometry()
	{
		return model.getGeometry( GLGeometry.class ) ;
	}
}