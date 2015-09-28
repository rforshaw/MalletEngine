package com.linxonline.mallet.renderer.desktop.GL ;

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

import com.linxonline.mallet.resources.model.Model ;
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
	public GLFontMap generateFontMap( final Font _font, final String _charsToMap, int _spacing )
	{
		// We want to render the texture at a higher resolution
		// Allowing the font to be scaled/zoomed without 
		// significant quality loss.
		final Font textureFont = _font.deriveFont( _font.getSize2D() ) ;//* 2.0f ) ;
		final Dimensions textureDim = determineDimensions( textureFont, _charsToMap ) ;

		final int length = _charsToMap.length() ;
		final float textureWidth = textureDim.width + ( _spacing * length ) ;

		// Used to render the texture
		final BufferedImage textureBuffer = new BufferedImage( ( int )textureWidth, textureDim.height, BufferedImage.TYPE_BYTE_GRAY ) ;
		final Graphics2D gTex2D = textureBuffer.createGraphics() ;
		gTex2D.setFont( textureFont ) ;
		gTex2D.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) ;
		final FontMetrics textureMetrics = gTex2D.getFontMetrics() ;

		// Used to get Metric information for geometry
		final BufferedImage geometryBuffer = new BufferedImage( 1, 1, BufferedImage.TYPE_BYTE_GRAY ) ;
		final Graphics2D gGeom2D = geometryBuffer.createGraphics() ;
		gGeom2D.setFont( _font ) ;
		final FontMetrics geometryMetrics = gGeom2D.getFontMetrics() ;

		int increment = 0 ;
		final char[] c = new char[1] ;
		final float point = 1.0f / textureWidth ;
		final Glyph[] glyphs = new GLGlyph[length] ;
		final int geometryHeight = geometryMetrics.getHeight() ;

		final GLGeometry geometry = new GLGeometry( 0, 4 * length ) ;
		final GL3 gl = GLRenderer.getCanvas().getContext().getCurrentGL().getGL3() ;
		if( gl == null )
		{
			System.out.println( "GL context doesn't exist" ) ;
			return null ;
		}

		int j = 0 ;
		for( int i = 0; i < length; i++ )
		{
			c[0] = _charsToMap.charAt( i ) ;

			// Render character to texture buffer starting from textureStart.
			final int textureStart = increment + ( int )( textureDim.position.x + ( i * _spacing ) ) ;
			gTex2D.drawChars( c, 0, 1, textureStart, ( int )textureDim.position.y ) ;

			final int geometryAdvance = geometryMetrics.charWidth( c[0] ) ;
			final Vector2 maxPoint = new Vector2( geometryAdvance, geometryHeight ) ;

			final int textureAdvance = textureMetrics.charWidth( c[0] ) ;
			final Vector2 uv1 = new Vector2( ( float )textureStart * point, 0.0f ) ;
			final Vector2 uv2 = new Vector2( ( float )( textureStart + textureAdvance ) * point, 1.0f ) ;

			// Must be destroyed manually destroyed, as not added to the 
			// ModelManager automatically.
			// Glyp geometry as located in a massive pool, stored in font map.
			geometry.addVertex( new Vector3( 0, 0, 0 ),
								new Vector2( uv1.x, uv1.y ) ) ;		// 0
			geometry.addVertex( new Vector3( maxPoint.x, 0, 0 ),
								new Vector2( uv2.x, uv1.y ) ) ;		// 1
			geometry.addVertex( new Vector3( 0, maxPoint.y, 0 ),
								new Vector2( uv1.x, uv2.y ) ) ;		// 2
			geometry.addVertex( new Vector3( maxPoint.x, maxPoint.y, 0 ),
								new Vector2( uv2.x, uv2.y ) ) ;		// 3

			// Glyph index buffer is stored within the glyph.
			final GLGeometry indexGeom = new GLGeometry( 6, 0 ) ;
			indexGeom.addIndices( j + 0 ) ;
			indexGeom.addIndices( j + 1 ) ;
			indexGeom.addIndices( j + 2 ) ;
			indexGeom.addIndices( j + 2 ) ;
			indexGeom.addIndices( j + 1 ) ;
			indexGeom.addIndices( j + 3 ) ;

			GLModelManager.bindIndex( gl, indexGeom ) ;
			glyphs[i] = new GLGlyph( indexGeom, c[0], 0, geometryAdvance ) ;

			j += 4 ;
			increment += textureAdvance ;
		}

		final Model model = new Model( geometry ) ;
		GLModelManager.bindVBO( gl, geometry ) ;

		try
		{
			final File outputfile = new File( "saved.png" ) ;
			ImageIO.write( textureBuffer, "png", outputfile ) ;
		}
		catch (IOException e) {}

		// Create a GLFontMap and wrap it around a FontMap
		// buffer is not automatically destroyed by TextureManager,
		// must be manually destroyed.
		return new GLFontMap( new FontMap( glyphs, 
										   manager.bind( textureBuffer, GLTextureManager.InternalFormat.UNCOMPRESSED ),
										   geometryHeight ), model ) ;
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