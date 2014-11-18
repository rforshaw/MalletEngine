package com.linxonline.mallet.renderer.desktop.G2D ;

import java.awt.Font ;
import java.awt.FontMetrics ;
import java.awt.font.GlyphVector ;

import java.awt.image.BufferedImage ;
import java.awt.geom.Rectangle2D ;
import java.awt.RenderingHints ;
import java.awt.Graphics2D ;
import java.awt.Canvas ;

import com.linxonline.mallet.resources.texture.Texture ;
import com.linxonline.mallet.renderer.font.FontMap ;
import com.linxonline.mallet.renderer.font.Glyph ;
import com.linxonline.mallet.maths.Vector2 ;

public class G2DFontGenerator
{
	public G2DFontGenerator() {}

	public FontMap generateFontMap( final String _name, final int _size, final String _charsToMap, final int _spacing )
	{
		return generateFontMap( new Font( _name, Font.PLAIN, _size ), _charsToMap, _spacing ) ;
	}

	public FontMap generateFontMap( final Font _font, final String _charsToMap, int _spacing )
	{
		final int length = _charsToMap.length() ;
		final Dimensions dim = determineDimensions( _font, _charsToMap ) ;
		final BufferedImage buffer = new BufferedImage( dim.width + ( _spacing * length ), dim.height, BufferedImage.TYPE_INT_ARGB ) ;
		final Graphics2D g2D = buffer.createGraphics() ;

		g2D.setFont( _font ) ;
		g2D.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) ;
		final FontMetrics metrics = g2D.getFontMetrics() ;
		
		int width = 0 ;
		final char[] c = new char[1] ;
		final Glyph[] glyphs = new Glyph[length] ;

		for( int i = 0; i < length; i++ )
		{
			c[0] = _charsToMap.charAt( i ) ;
			final int start = width + ( int )( dim.position.x + ( i * _spacing ) ) ;
			g2D.drawChars( c, 0, 1, start, ( int )dim.position.y ) ;

			final int advance = metrics.charWidth( c[0] ) ;
			glyphs[i] = new Glyph( c[0], start, advance ) ;
			width += advance ;
		}

		return new FontMap( glyphs, new Texture( new G2DImage( buffer ) ), metrics.getHeight() ) ;
	}

	private Dimensions determineDimensions( final Font _font, final String _text )
	{
		final BufferedImage buffer = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_RGB ) ;
		final Graphics2D g2D = buffer.createGraphics() ;
		g2D.setFont( _font ) ;
		final FontMetrics metrics = g2D.getFontMetrics() ;

		final Rectangle2D rect = metrics.getStringBounds( _text, g2D ) ;
		final int width = ( int )( rect.getMaxX() - rect.getMinX() ) ;
		final int height = ( int )( rect.getMaxY() - rect.getMinY() ) ;
		final float y = metrics.getAscent() ;

		return new Dimensions( new Vector2( 0, y ), width, height ) ;
	}

	private static class Dimensions
	{
		public final int width ;
		public final int height ;
		public final Vector2 position ;

		public Dimensions( final Vector2 _pos, final int _width, final int _height )
		{
			position = _pos ;
			width = _width ;
			height = _height ;
		}
		
		public String toString()
		{
			return "WIDTH: " + width + " HEIGHT: " + height + " POS: " + position.toString() ;
		}
	}
}