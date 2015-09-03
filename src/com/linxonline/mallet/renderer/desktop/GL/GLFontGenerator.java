package com.linxonline.mallet.renderer.desktop.GL ;

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

import com.linxonline.mallet.resources.model.Model ;
import com.linxonline.mallet.renderer.font.Glyph ;
import com.linxonline.mallet.renderer.font.FontMap ;
import com.linxonline.mallet.maths.Vector2 ;

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
	public GLFontMap generateFontMap( final Font _font, final String _charsToMap, int _spacing )
	{
		final int length = _charsToMap.length() ;
		final Dimensions dim = determineDimensions( _font, _charsToMap ) ;
		final float width = dim.width + ( _spacing * length ) ;

		final BufferedImage buffer = new BufferedImage( ( int )width, dim.height, BufferedImage.TYPE_BYTE_GRAY ) ;
		final Graphics2D g2D = buffer.createGraphics() ;

		//g2D.setComposite( AlphaComposite.SrcOut ) ;
		g2D.setFont( _font ) ;
		g2D.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) ;
		final FontMetrics metrics = g2D.getFontMetrics() ;
		
		int increment = 0 ;
		final char[] c = new char[1] ;
		final double point = 1.0f / width ;
		final Glyph[] glyphs = new GLGlyph[length] ;

		for( int i = 0; i < length; i++ )
		{
			c[0] = _charsToMap.charAt( i ) ;
			final int start = increment + ( int )( dim.position.x + ( i * _spacing ) ) ;
			g2D.drawChars( c, 0, 1, start, ( int )dim.position.y ) ;

			final int advance = metrics.charWidth( c[0] ) ;
			final int height = metrics.getHeight() ;
			final float x1 = ( float )( start * point ) ;
			final float x2 = ( float )( ( start + advance ) * point ) ;
			
			// Must be destroyed manually destroyed, as not added to the 
			// ModelManager automatically. 
			final Model model = GLModelGenerator.genPlaneModel( new Vector2( advance, height ),
																new Vector2( x1, 0.0f ),
																new Vector2( x2, 1.0f ) ) ;
			glyphs[i] = new GLGlyph( model, c[0], start, advance ) ;
			increment += advance ;
		}

		/*try
		{
			final File outputfile = new File( "saved.png" ) ;
			ImageIO.write( buffer, "png", outputfile ) ;
		}
		catch (IOException e) {}*/

		// Create a GLFontMap and wrap it around a FontMap
		// buffer is not automatically destroyed by TextureManager,
		// must be manually destroyed.
		return new GLFontMap( new FontMap( glyphs, 
										   manager.bind( buffer, GLTextureManager.InternalFormat.UNCOMPRESSED ),
										   metrics.getHeight() ) ) ;
	}

	private Dimensions determineDimensions( final Font _font, final String _text )
	{
		final BufferedImage buffer = new BufferedImage( 1, 1, BufferedImage.TYPE_BYTE_GRAY ) ;
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