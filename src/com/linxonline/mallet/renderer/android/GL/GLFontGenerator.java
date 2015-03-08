package com.linxonline.mallet.renderer.android.GL ;

import android.graphics.Typeface ;
import android.graphics.Bitmap.Config ;
import android.graphics.Canvas ;
import android.graphics.Bitmap ;
import android.graphics.Paint ;
import android.graphics.Rect ;

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
		System.out.println( "Generate font map." ) ;
		return generateFontMap( Typeface.create( _name, Typeface.NORMAL), _size, _charsToMap, _spacing ) ;
	}

	public GLFontMap generateFontMap( final Typeface _typeface, final int _size, final String _charsToMap, int _spacing )
	{
		return null ;
		/*final Paint paint = new Paint( Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG ) ;
		paint.setStyle( Paint.Style.FILL ) ;
		paint.setTextAlign( Paint.Align.CENTER ) ;
		paint.setTypeface( _typeface ) ;
		paint.setTextSize( ( float )_size ) ;

		final int length = _charsToMap.length() ;
		final Rect bounds = new Rect();
		paint.getTextBounds( _charsToMap, 0, length, bounds ) ;

		final int height = nextPowerOf2( bounds.height() ) ;
		final int width = nextPowerOf2( bounds.width() + ( _spacing * length ))  ;

		System.out.println( "Width: " + width + " Height: " + height ) ;
		
		final Bitmap bitmap = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 ) ;
		final Canvas canvas = new Canvas( bitmap ) ;

		int increment = 0 ;
		final char[] c = new char[1] ;
		final double point = 1.0f / width ;
		final Glyph[] glyphs = new GLGlyph[length] ;
		final Vector2 position = new Vector2( 0, paint.getFontMetrics().ascent ) ;

		for( int i = 0; i < length; i++ )
		{
			c[0] = _charsToMap.charAt( i ) ;
			final int start = increment + ( int )( position.x + ( i * _spacing ) ) ;
			canvas.drawText( c, 0, 1, start, position.y, paint ) ;

			final float advance = paint.measureText( c, 0, 1 ) ;
			final float x1 = ( float )( start * point ) ;
			final float x2 = ( float )( ( start + advance ) * point ) ;
			final Model model = GLModelGenerator.genPlaneModel( new Vector2( advance, height ),
																new Vector2( x1, 0.0f ),
																new Vector2( x2, 1.0f ) ) ;
			glyphs[i] = new GLGlyph( model, c[0], start, advance ) ;
			increment += advance ;
		}

		// Create a GLFontMap and wrap it around a FontMap
		return new GLFontMap( new FontMap( glyphs, manager.bind( bitmap ), height ) ) ;*/
	}
	
	public static int nextPowerOf2(final int a)
	{
		int b = 1;
		while (b < a)
		{
			b = b << 1;
		}
		return b;
	}
}