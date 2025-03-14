package com.linxonline.mallet.renderer.desktop.opengl ;

import java.awt.image.BufferedImage ;
import java.awt.RenderingHints ;
import java.awt.Graphics2D ;

import com.linxonline.mallet.renderer.Font ;
import com.linxonline.mallet.renderer.Glyph ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public final class GLFontGenerator
{
	private final static float PADDING = 4.0f ;

	private final BufferedImage geometryBuffer = new BufferedImage( 1, 1, BufferedImage.TYPE_BYTE_GRAY ) ;
	private final Graphics2D gGeom2D ;

	public GLFontGenerator()
	{
		gGeom2D = geometryBuffer.createGraphics() ;
	}

	public Font.Metrics generateMetrics( final String _name, final int _style, final int _size, final String _characters  )
	{
		return generateMetrics( new java.awt.Font( _name, java.awt.Font.PLAIN, _size ), _characters ) ;
	}

	public Glyph generateGlyph( final String _name, final int _style, final int _size, final int _code )
	{
		return generateGlyph( new java.awt.Font( _name, _style, _size ), _code ) ;
	}

	public Glyph generateGlyph( final java.awt.Font _font, final int _code )
	{
		gGeom2D.setFont( _font ) ;

		final java.awt.FontMetrics metrics = gGeom2D.getFontMetrics() ;
		final char c = ( char )_code ;
		return new Glyph( c, metrics.charWidth( c ) ) ;
	}

	public Font.Metrics generateMetrics( final java.awt.Font _font, final String _characters )
	{
		// Used to get Metric information for geometry
		gGeom2D.setFont( _font ) ;

		final java.awt.FontMetrics metrics = gGeom2D.getFontMetrics() ;
		final int length = _characters.length() ;
		final Glyph[] glyphs = new Glyph[length] ;

		for( int i = 0; i < length; i++ )
		{
			final char c = _characters.charAt( i ) ;
			glyphs[i] = new Glyph( c, metrics.charWidth( c ) ) ;
		}

		return new Font.Metrics( glyphs, metrics.getHeight(),
											   metrics.getAscent(),
											   metrics.getDescent(),
											   metrics.getLeading() ) ;
	}

	public Bundle generateFont( final Font _font )
	{
		final Font.Metrics metrics = _font.getMetrics() ;
		final Glyph[] glyphs = metrics.getGlyphs() ;

		//System.out.println( "Gen: " + _font + " Length: " + glyphs.length ) ;

		// This allows us to render the text at a higher resolution 
		// than the font has requested - change multiplier to increase 
		// the base point size .
		final int multiplier = 2 ;
		final Font bigger = _font ;//new Font( _font.getFontName(), ( int )( _font.getPointSize() * multiplier ) ) ;

		final Font.Metrics biggerMetrics = bigger.getMetrics() ;
		final Glyph[] biggerGlyphs = biggerMetrics.getGlyphs() ;
		final float height = biggerMetrics.getHeight() ;
		final float width = calculateWidth( biggerGlyphs ) ;

		// Used to render the texture
		final java.awt.Font font = new java.awt.Font( bigger.getFontName(), java.awt.Font.PLAIN, bigger.getPointSize() ) ;
		final BufferedImage textureBuffer = new BufferedImage( ( int )width, ( int )height, BufferedImage.TYPE_BYTE_GRAY ) ;
		final Graphics2D g2D = textureBuffer.createGraphics() ;

		g2D.setFont( font.deriveFont( font.getSize2D() ) ) ;
		g2D.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) ;
		g2D.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP ) ;

		final char[] c = new char[1] ;
		final double point = 1.0 / width ;
		final float ascent = biggerMetrics.getAscent() ;
		float start = 0.0f ;

		final int length = glyphs.length ;
		final Shape[] shapes = new Shape[length] ;

		for( int i = 0; i < length; i++ )
		{
			final Glyph glyph = glyphs[i] ;
			if( glyph != null )
			{
				c[0] = glyph.getCharacter() ;
				g2D.drawChars( c, 0, 1, ( int )start, ( int )ascent ) ;

				//System.out.println( "Char: " + c[0] ) ;

				// We do not want to include the padding when uv-mapping
				final float advance = biggerGlyphs[i].getWidth() ;
				final float x1 = ( float )( start * point ) ;
				final float x2 = ( float )( ( start + advance ) * point ) ;

				final Vector3 maxPoint = new Vector3( glyph.getWidth(), metrics.getHeight(), 0.0f ) ;
				final Vector2 uv1 = new Vector2( x1, 0.0f ) ;
				final Vector2 uv2 = new Vector2( x2, 1.0f ) ;

				shapes[i] = Shape.constructPlane( maxPoint, uv1, uv2 ) ;
				start += advance + PADDING ;
			}
		}

		return new Bundle( shapes, textureBuffer ) ;
	}

	/**
		The glyph array used in Metrics contains 
		potentially null slots.
		As not all characters in world are rendered out.
		To calculate the correct width for the texture 
		we need to skip these null glyphs.
	*/
	private static float calculateWidth( final Glyph[] _glyphs )
	{
		float width = 0.0f ;
		for( int i = 0; i < _glyphs.length; i++ )
		{
			final Glyph glyph = _glyphs[i] ;
			width += glyph != null ? glyph.getWidth() + PADDING : PADDING ;
		}
		return width ;
	}
	
	public static class Bundle
	{
		public final Shape[] shapes ;
		public final BufferedImage image ;

		public Bundle( final Shape[] _shapes, final BufferedImage _image )
		{
			shapes = _shapes ;
			image = _image ;
		}
	}
}
