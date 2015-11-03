package com.linxonline.mallet.renderer.android.GL ;

import com.linxonline.mallet.resources.Resource ;
import com.linxonline.mallet.resources.* ;

import com.linxonline.mallet.resources.model.Model ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.font.FontMap ;
import com.linxonline.mallet.renderer.font.Glyph ;

public class GLFontMap extends Resource
{
	public final FontMap<GLImage> fontMap ;
	public Model model ;
	public Shape shape ;

	public GLFontMap( final FontMap<GLImage> _fontMap )
	{
		super() ;
		fontMap = _fontMap ;
	}

	public void setModel( final Model _model, final Shape _shape )
	{
		model = _model ;
		shape = _shape ;
	}

	public int stringWidth( final String _text )
	{
		final int length = _text.length() ;
		int width = 0 ;
		for( int i = 0; i < length; ++i )
		{
			width += ( int )getGlyphWithChar( _text.charAt( i ) ).advance ;
		}

		return width ;
	}

	public GLGlyph getGlyphWithChar( final char _character )
	{
		return getGlyphWithCode( ( int )_character ) ;
	}

	public GLGlyph getGlyphWithCode( final int _code )
	{
		return ( GLGlyph )fontMap.getGlyphWithCode( _code ) ;
	}

	public GLImage getGLImage()
	{
		return fontMap.texture.getImage() ;
	}

	public int getHeight()
	{
		return fontMap.getHeight() ;
	}

	public GLGeometry getGLGeometry()
	{
		return model.getGeometry( GLGeometry.class ) ;
	}

	@Override
	public void destroy()
	{
		fontMap.destroy() ;
		if( model != null )
		{
			model.destroy() ;
		}
	}

	@Override
	public String toString()
	{
		return fontMap.toString() ;
	}

	@Override
	public String type()
	{
		return "FONT" ;
	}
}