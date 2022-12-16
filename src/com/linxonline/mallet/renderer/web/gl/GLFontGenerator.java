package com.linxonline.mallet.renderer.web.gl ;

import java.util.List ;

import org.teavm.jso.dom.html.* ;
import org.teavm.jso.webgl.WebGLRenderingContext ;
import org.teavm.jso.canvas.CanvasRenderingContext2D ;

import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.Glyph ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class GLFontGenerator
{
	private final static float PADDING = 4.0f ;
	private final CanvasRenderingContext2D canvas ;

	public GLFontGenerator()
	{
		final HTMLDocument doc = HTMLDocument.current() ;
		final HTMLCanvasElement element = ( HTMLCanvasElement )doc.getElementById( "text-canvas" ) ;
		canvas = ( CanvasRenderingContext2D )element.getContext( "2d" ) ;
	}

	private static void setCanvasFont( final CanvasRenderingContext2D _canvas, final String _name, final int _style, final int _size )
	{
		final StringBuilder builder = new StringBuilder() ;
		builder.append( _size ) ;
		builder.append( "pt " ) ;
		builder.append( _name ) ;

		System.out.println( builder.toString() ) ;

		final String font = builder.toString() ;
		_canvas.setFont( font ) ;
	}

	public MalletFont.Metrics generateMetrics( final String _name, final int _style, final int _size, final String _characters  )
	{
		setCanvasFont( canvas, _name, _style, _size ) ;
		return generateMetrics( canvas, _name, _size, _characters ) ;
	}

	public MalletFont.Metrics generateMetrics( final CanvasRenderingContext2D _canvas, final String _name, final int _size, final String _characters )
	{
		final int length = _characters.length() ;
		final Glyph[] glyphs = new Glyph[length] ;

		for( int i = 0; i < length; i++ )
		{
			final char c = _characters.charAt( i ) ;
			final float width = _canvas.measureText( String.valueOf( c ) ).getWidth() ;
			glyphs[i] = new Glyph( c, width ) ;
		}

		final float height = getHeight( _name, _size, _characters ) ;
		return new MalletFont.Metrics( glyphs, height,
											   height / 2.0f,
											   0.0f,
											   0.0f ) ;
	}

	public Glyph generateGlyph( final String _name, final int _style, final int _size, final int _code )
	{
		setCanvasFont( canvas, _name, _style, _size ) ;

		final char c = ( char )_code ;
		final float width = canvas.measureText( String.valueOf( c ) ).getWidth() ;
		final Glyph glyph = new Glyph( c, width ) ;
		return glyph ;
	}

	public Bundle generateFont( final MalletFont _font )
	{
		final MalletFont.Metrics metrics = _font.getMetrics() ;
		final Glyph[] glyphs = metrics.getGlyphs() ;

		final float height = metrics.getHeight() ;
		final float width = calculateWidth( glyphs ) ;

		System.out.println( "Width: " + width + " Height: " + height ) ;

		final HTMLCanvasElement element = canvas.getCanvas() ;
		element.setWidth( ( int )width ) ;
		element.setHeight( ( int )height ) ;

		setCanvasFont( canvas, _font.getFontName(), _font.getStyle(), _font.getPointSize() ) ;
		canvas.setFillStyle( "#FF0000" ) ;
		canvas.clearRect( 0.0, 0.0, ( double )width, ( double )height ) ;

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
				final String txt = String.valueOf( glyph.getCharacter() ) ;
				canvas.fillText( txt, start, ascent ) ;

				final float advance = glyph.getWidth() ;
				final float x1 = ( float )( start * point ) ;
				final float x2 = ( float )( ( start + advance ) * point ) ;

				final Vector3 maxPoint = new Vector3( advance, height, 0.0f ) ;
				final Vector2 uv1 = new Vector2( x1, 0.0f ) ;
				final Vector2 uv2 = new Vector2( x2, 1.0f ) ;

				shapes[i] = Shape.constructPlane( maxPoint, uv1, uv2 ) ;
				start += advance + PADDING ;
			}
		}

		return new Bundle( shapes, canvas.getCanvas() ) ;
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

	private int getHeight( final String _name, final int _size, final String _text )
	{
		final HTMLDocument doc = HTMLDocument.current() ;
		final HTMLElement div = doc.createElement( "div" ) ;
		final HTMLElement txt = div.withText( _text ) ;

		div.withAttr( "style", String.format( "font-family:%s;font-size:%dpt;", _name, _size ) ) ;
		doc.getBody().withChild( div ) ;

		final int height = div.getClientHeight() ;
		doc.getBody().removeChild( div ) ;

		return height ;
	}

	public static class Bundle
	{
		public final Shape[] shapes ;
		public final HTMLCanvasElement canvas ;

		public Bundle( final Shape[] _shapes, final HTMLCanvasElement _canvas )
		{
			shapes = _shapes ;
			canvas = _canvas ;
		}
	}
}
