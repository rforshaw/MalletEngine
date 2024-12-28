package com.linxonline.mallet.renderer.android.opengl ;

import java.util.List ;

import android.graphics.Typeface ;
import android.graphics.Bitmap.Config ;
import android.graphics.Canvas ;
import android.graphics.Bitmap ;
import android.graphics.Paint ;
import android.graphics.Rect ;
import android.graphics.Color ;

import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.renderer.Font ;
import com.linxonline.mallet.renderer.Glyph ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class GLFontGenerator
{
	private final GLTextureManager manager ;

	private final Paint paint = new Paint() ;

	public GLFontGenerator( final GLTextureManager _manager )
	{
		manager = _manager ;
	}

	public Font.Metrics generateMetrics( final String _name, final int _style, final int _size, final String _characters  )
	{
		return generateMetrics( Typeface.create( _name, Typeface.NORMAL), _size, _characters ) ;
	}

	public Glyph generateGlyph( final String _name, final int _style, final int _size, final int _code )
	{
		return generateGlyph( Typeface.create( _name, Typeface.NORMAL), _size, _code ) ;
	}

	public Glyph generateGlyph( final Typeface _typeface, final int _size, final int _code )
	{
		final char[] c = new char[1] ;
		c[0] = ( char )_code ;
		return new Glyph( c[0], paint.measureText( c, 0, 1 ) ) ;
	}

	public Font.Metrics generateMetrics( final Typeface _typeface, final int _size, final String _characters )
	{
		paint.setTypeface( _typeface ) ;
		paint.setTextSize( ( float )_size ) ;


		final int length = _characters.length() ;
		final char[] c = new char[1] ;
		final Glyph[] glyphs = new Glyph[length] ;

		for( int i = 0; i < length; i++ )
		{
			c[0] = _characters.charAt( i ) ;
			glyphs[i] = new Glyph( c[0], paint.measureText( c, 0, 1 ) ) ;
		}

		final Paint.FontMetrics metrics = paint.getFontMetrics() ;

		final Rect bounds = new Rect() ;
		paint.getTextBounds( _characters, 0, length, bounds ) ;
		return new Font.Metrics( glyphs, bounds.height(),
											   Math.abs( metrics.ascent ),
											   metrics.descent,
											   metrics.leading ) ;
	}

	public GLFont generateFont( final Font _font, final float _spacing )
	{
		final Font.Metrics metrics = _font.getMetrics() ;
		final Glyph[] glyphs = metrics.getGlyphs() ;

		final float height = metrics.getHeight() ;
		final float width = calculateWidth( glyphs ) ;

		final Paint paint = new Paint( Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG ) ;
		paint.setStyle( Paint.Style.FILL ) ;
		paint.setTypeface( Typeface.create( _font.getFontName(), Typeface.NORMAL) ) ;
		paint.setTextSize( _font.getPointSize() ) ;
		paint.setColor( Color.WHITE ) ;

		final Bitmap bitmap = Bitmap.createBitmap( ( int )width, ( int )height, Bitmap.Config.ALPHA_8 ) ;
		final Canvas canvas = new Canvas( bitmap ) ;

		final char[] c = new char[1] ;
		final double point = 1.0 / width ;
		final float ascent = metrics.getAscent() ;
		float start = 0.0f ;

		final int length = glyphs.length ;
		final Shape[] shapes = new Shape[length] ;

		for( int i = 0; i < length; i++ )
		{
			final Glyph glyph = glyphs[i] ;
			if( glyph != null )
			{
				c[0] = glyph.getCharacter() ;
				canvas.drawText( c, 0, 1, start, ascent, paint ) ;

				final float advance = glyph.getWidth() ;
				final float x1 = ( float )( start * point ) ;
				final float x2 = ( float )( ( start + advance ) * point ) ;

				final Vector3 maxPoint = new Vector3( advance, height, 0.0f ) ;
				final Vector2 uv1 = new Vector2( x1, 0.0f ) ;
				final Vector2 uv2 = new Vector2( x2, 1.0f ) ;

				shapes[i] = Shape.constructPlane( maxPoint, uv1, uv2 ) ;
				start += advance ;
			}
		}

		final GLFont font = new GLFont( shapes, manager.bind( bitmap ) ) ;
		bitmap.recycle() ;

		return font ;
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
			width += glyph != null ? glyph.getWidth() : 0.0f ;
		}
		return width ;
	}
}
