package com.linxonline.mallet.renderer.android.GL ;

import android.graphics.Typeface ;
import android.graphics.Bitmap.Config ;
import android.graphics.Canvas ;
import android.graphics.Bitmap ;
import android.graphics.Paint ;
import android.graphics.Rect ;
import android.graphics.Color ;

import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.resources.texture.Texture ;
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
		return generateFontMap( Typeface.create( _name, Typeface.NORMAL), _size, _charsToMap, _spacing ) ;
	}

	/**
		Create a skeleton FontMap for the requested font.
		This will allow the developer to query text length, and height.
		Creation of models and textures is done later during the 
		draw cycle.
	*/
	public GLFontMap generateFontMap( final Typeface _typeface, final int _size, final String _charsToMap, int _spacing )
	{
		final Paint paint = new Paint() ;
		paint.setTypeface( _typeface ) ;
		paint.setTextSize( ( float )_size ) ;

		final int length = _charsToMap.length() ;
		float increment = 0 ;
		final char[] c = new char[1] ;
		final Glyph[] glyphs = new GLGlyph[length] ;

		for( int i = 0; i < length; i++ )
		{
			c[0] = _charsToMap.charAt( i ) ;
			final float start = increment + ( i * _spacing ) ;
			final float advance = paint.measureText( c, 0, 1 ) ;

			glyphs[i] = new GLGlyph( c[0], start, advance ) ;
			increment += advance ;
		}

		// Create a GLFontMap and wrap it around a FontMap
		final Rect bounds = new Rect() ;
		paint.getTextBounds( _charsToMap, 0, length, bounds ) ;
		return new GLFontMap( new FontMap( glyphs, null, bounds.height() ) ) ;
	}

	/**
		Create the fontmap's texture and generate the geometry to 
		map the texture to.
		This is called during a drawText call, when a font has yet to have 
		this data initialised. 
	*/
	public GLFontMap generateFontGeometry( final MalletFont _font, final GLFontMap _map )
	{
		final Glyph[] glyphs = _map.fontMap.glyphs ;
		final int length = glyphs.length ;

		final int height = _map.fontMap.height ;
		int width = calculateWidth( glyphs ) ;

		final Paint paint = new Paint( Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG ) ;
		paint.setStyle( Paint.Style.FILL ) ;
		paint.setTypeface( Typeface.create( _font.getFontName(), Typeface.NORMAL) ) ;
		paint.setTextSize( ( float )_font.size ) ;
		paint.setColor( Color.WHITE ) ;

		final Bitmap bitmap = Bitmap.createBitmap( width, height, Bitmap.Config.ALPHA_8 ) ;
		final Canvas canvas = new Canvas( bitmap ) ;

		final char[] c = new char[1] ;
		final double point = 1.0f / width ;
		final float ascent = Math.abs( paint.ascent() ) ;

		for( int i = 0; i < length; i++ )
		{
			final GLGlyph glyph = ( GLGlyph )glyphs[i] ;
			if( glyph != null )
			{
				c[0] = glyph.character ;
				final float start = glyph.start ;
				canvas.drawText( c, 0, 1, start, ascent, paint ) ;

				final float advance = glyph.advance ;
				final float x1 = ( float )( start * point ) ;
				final float x2 = ( float )( ( start + advance ) * point ) ;
				final Model model = GLModelGenerator.genPlaneModel( new Vector2( advance, height ),
																	new Vector2( x1, 0.0f ),
																	new Vector2( x2, 1.0f ) ) ;
				glyph.setModel( model ) ;
			}
		}

		_map.fontMap.setTexture( manager.bind( bitmap ) ) ;
		bitmap.recycle() ;

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