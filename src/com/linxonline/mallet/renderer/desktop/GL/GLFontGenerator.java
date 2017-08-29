package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.List ;

import javax.imageio.ImageIO ;
import java.io.File ;
import java.io.IOException ;

import java.awt.Font ;
import java.awt.FontMetrics ;
import java.awt.font.GlyphVector ;

import java.awt.image.BufferedImage ;
import java.awt.geom.Rectangle2D ;
import java.awt.RenderingHints ;
import java.awt.Graphics2D ;
import java.awt.Canvas ;
import java.awt.AlphaComposite ;

import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.font.Glyph ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class GLFontGenerator
{
	private final static float PADDING = 4.0f ;
	private final GLTextureManager manager ;

	public GLFontGenerator( final GLTextureManager _manager )
	{
		manager = _manager ;
	}

	public MalletFont.Metrics generateMetrics( final String _name, final int _style, final int _size, final String _characters  )
	{
		return generateMetrics( new Font( _name, Font.PLAIN, _size ), _characters ) ;
	}

	public MalletFont.Metrics generateMetrics( final Font _font, final String _characters )
	{
		// Used to get Metric information for geometry
		final BufferedImage geometryBuffer = new BufferedImage( 1, 1, BufferedImage.TYPE_BYTE_GRAY ) ;
		final Graphics2D gGeom2D = geometryBuffer.createGraphics() ;
		gGeom2D.setFont( _font ) ;

		final FontMetrics metrics = gGeom2D.getFontMetrics() ;
		final int length = _characters.length() ;
		final Glyph[] glyphs = new Glyph[length] ;

		for( int i = 0; i < length; i++ )
		{
			final char c = _characters.charAt( i ) ;
			glyphs[i] = new Glyph( c, metrics.charWidth( c ) ) ;
		}

		return new MalletFont.Metrics( glyphs, metrics.getHeight(),
											   metrics.getAscent(),
											   metrics.getDescent(),
											   metrics.getLeading() ) ;
	}

	public GLFont generateFont( final MalletFont _font )
	{
		final MalletFont.Metrics metrics = _font.getMetrics() ;
		final Glyph[] glyphs = metrics.getGlyphs() ;

		// This allows us to render the text at a higher resolution 
		// than the font has requested - change multiplier to increase 
		// the base point size .
		final int multiplier = 1 ;
		final MalletFont bigger = new MalletFont( _font.getFontName(), ( int )( _font.getPointSize() * multiplier ) ) ;

		final MalletFont.Metrics biggerMetrics = bigger.getMetrics() ;
		final Glyph[] biggerGlyphs = biggerMetrics.getGlyphs() ;
		final float height = biggerMetrics.getHeight() ;
		final float width = calculateWidth( biggerGlyphs ) ;

		// Used to render the texture
		final Font font = new Font( bigger.getFontName(), Font.PLAIN, bigger.getPointSize() ) ;
		final BufferedImage textureBuffer = new BufferedImage( ( int )width, ( int )height, BufferedImage.TYPE_BYTE_GRAY ) ;
		final Graphics2D g2D = textureBuffer.createGraphics() ;

		g2D.setFont( font.deriveFont( font.getSize2D() ) ) ;
		//g2D.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) ;

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

		return new GLFont( shapes, manager.bind( textureBuffer, GLTextureManager.InternalFormat.UNCOMPRESSED ) ) ;
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
}
