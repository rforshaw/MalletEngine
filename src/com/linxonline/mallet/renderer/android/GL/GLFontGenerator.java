package com.linxonline.mallet.renderer.android.GL ;

import java.util.List ;

import android.graphics.Typeface ;
import android.graphics.Bitmap.Config ;
import android.graphics.Canvas ;
import android.graphics.Bitmap ;
import android.graphics.Paint ;
import android.graphics.Rect ;
import android.graphics.Color ;

import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.texture.Texture ;
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
		return generateFontMap( Typeface.create( _name, Typeface.NORMAL), _size, _charsToMap, _spacing ) ;
	}

	/**
		Create a skeleton FontMap for the requested font.
		This will allow the developer to query text length, and height.
		Creation of models and textures is done later during the 
		draw cycle.
	*/
	public GLFontMap generateFontMap( final Typeface _typeface, final int _size, final String _charsToMap, final int _spacing )
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
			//System.out.println( "Index: " + i + " Glyph: " + glyphs[i] ) ;
			increment += advance ;
		}

		// Create a GLFontMap and wrap it around a FontMap
		final Rect bounds = new Rect() ;
		paint.getTextBounds( _charsToMap, 0, length, bounds ) ;
		return new GLFontMap( new FontMap<GLImage>( glyphs, null, bounds.height() ) ) ;
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
		final int width = calculateWidth( glyphs ) ;

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

		final List<Shape> shapes = MalletList.<Shape>newList() ;
		for( int i = 0; i < length; i++ )
		{
			final GLGlyph glyph = ( GLGlyph )glyphs[i] ;
			//System.out.println( "Index: " + i + " Glyph: " + glyph ) ;
			if( glyph != null )
			{
				c[0] = glyph.character ;
				final float start = glyph.start ;
				canvas.drawText( c, 0, 1, start, ascent, paint ) ;

				final float advance = glyph.advance ;
				final float x1 = ( float )( start * point ) ;
				final float x2 = ( float )( ( start + advance ) * point ) ;

				final Vector3 maxPoint = new Vector3( advance, height, 0.0f ) ;
				final Vector2 uv1 = new Vector2( x1, 0.0f ) ;
				final Vector2 uv2 = new Vector2( x2, 1.0f ) ;

				glyph.setShape( Shape.constructPlane( maxPoint, uv1, uv2 ) ) ;
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
