package com.linxonline.mallet.renderer.GL ;

import com.linxonline.mallet.resources.Resource ;
import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.resources.texture.* ;

import com.linxonline.mallet.renderer.font.FontMap ;
import com.linxonline.mallet.renderer.font.Glyph ;

public class GLFontMap extends Resource
{
	public final FontMap fontMap ;

	public GLFontMap( final FontMap _fontMap )
	{
		super() ;
		fontMap = _fontMap ;
	}

	public GLGlyph getGlyphWithChar( final char _character )
	{
		return ( GLGlyph )fontMap.getGlyphWithCode( ( int )_character ) ;
	}

	public GLGlyph getGlyphWithCode( final int _code )
	{
		return ( GLGlyph )fontMap.getGlyphWithCode( _code ) ;
	}

	public GLImage getGLImage()
	{
		return fontMap.texture.getImage( GLImage.class ) ;
	}

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