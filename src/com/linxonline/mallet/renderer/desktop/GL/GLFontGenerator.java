package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.List ;

import javax.imageio.ImageIO ;
import java.io.File ;
import java.io.IOException ;

import javax.media.opengl.* ;

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
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.font.Glyph ;
import com.linxonline.mallet.renderer.font.FontMap ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class GLFontGenerator
{
	private final GLTextureManager manager ;

	public GLFontGenerator( final GLTextureManager _manager )
	{
		manager = _manager ;
	}

	public GLFontMap generateFontMap( final String _name, final int _size, final String _charsToMap, final int _spacing )
	{
		return generateFontMap( new Font( _name, Font.PLAIN, _size ), _charsToMap, _spacing ) ;
	}

	/**
		Needs to be updated to provide higher resolution font maps.
		We should render out a high resolution map, but use geometry 
		suitable for the font specified. Currently texture is generated 
		based on 
	*/
	public GLFontMap generateFontMap( final Font _font, final String _charsToMap, final int _spacing )
	{
		// Used to get Metric information for geometry
		final BufferedImage geometryBuffer = new BufferedImage( 1, 1, BufferedImage.TYPE_BYTE_GRAY ) ;
		final Graphics2D gGeom2D = geometryBuffer.createGraphics() ;
		gGeom2D.setFont( _font ) ;
		final FontMetrics metrics = gGeom2D.getFontMetrics() ;

		final int length = _charsToMap.length() ;
		int increment = 0 ;
		final char[] c = new char[1] ;
		final Glyph[] glyphs = new GLGlyph[length] ;

		final Shape[] shapes = new Shape[length] ;
		for( int i = 0; i < length; i++ )
		{
			c[0] = _charsToMap.charAt( i ) ;
			final float start = increment + ( i * _spacing ) ;
			final float advance = metrics.charWidth( c[0] ) ;

			glyphs[i] = new GLGlyph( c[0], start, advance ) ;
			//System.out.println( "Index: " + i + " Glyph: " + glyphs[i] ) ;
			increment += advance ;
		}

		// Create a GLFontMap and wrap it around a FontMap
		return new GLFontMap( new FontMap<GLImage>( glyphs, null, metrics.getHeight() ) ) ;
	}

	public GLFontMap generateFontGeometry( final MalletFont _font, final GLFontMap _map )
	{
		final Glyph[] glyphs = _map.fontMap.glyphs ;
		final int length = glyphs.length ;

		final int height = _map.fontMap.height ;
		final int width = calculateWidth( glyphs ) ;
	
		// Used to render the texture
		final Font font = new Font( _font.fontName, Font.PLAIN, _font.size ) ;
		final BufferedImage textureBuffer = new BufferedImage( width, height, BufferedImage.TYPE_BYTE_GRAY ) ;
		final Graphics2D g2D = textureBuffer.createGraphics() ;

		g2D.setFont( font.deriveFont( font.getSize2D() ) ) ;
		g2D.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) ;
		final FontMetrics metrics = g2D.getFontMetrics() ;

		final char[] c = new char[1] ;
		final double point = 1.0f / width ;
		final float ascent = Math.abs( metrics.getAscent() ) ;

		final List<Shape> shapes = MalletList.<Shape>newList() ;
		for( int i = 0; i < length; i++ )
		{
			final GLGlyph glyph = ( GLGlyph )glyphs[i] ;
			//System.out.println( "Index: " + i + " Glyph: " + glyph ) ;
			if( glyph != null )
			{
				c[0] = glyph.character ;
				final float start = glyph.start ;
				g2D.drawChars( c, 0, 1, ( int )start, ( int )ascent ) ;

				final float advance = glyph.advance ;
				final float x1 = ( float )( start * point ) ;
				final float x2 = ( float )( ( start + advance ) * point ) ;

				final Vector3 maxPoint = new Vector3( advance, height, 0.0f ) ;
				final Vector2 uv1 = new Vector2( x1, 0.0f ) ;
				final Vector2 uv2 = new Vector2( x2, 1.0f ) ;

				glyph.setShape( Shape.constructPlane( maxPoint, uv1, uv2 ) ) ;
			}
		}
	
		_map.fontMap.setTexture( manager.bind( textureBuffer, GLTextureManager.InternalFormat.UNCOMPRESSED ) ) ;
		return _map ;
	}

	private static int calculateWidth( final Glyph[] _glyphs )
	{
		final int length = _glyphs.length ;
		int width = 0 ;
		for( int i = 0; i < length; i++ )
		{
			final GLGlyph glyph = ( GLGlyph )_glyphs[i] ;
			if( glyph != null )
			{
				final int t = ( int )( glyph.start ) ;
				if( t > width )
				{
					width = t ;
				}
			}
		}

		return width ;
	}
}
