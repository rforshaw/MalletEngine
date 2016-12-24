package com.linxonline.mallet.renderer.web.gl ;

import java.util.List ;

import org.teavm.jso.dom.html.* ;
import org.teavm.jso.webgl.WebGLRenderingContext ;
import org.teavm.jso.canvas.CanvasRenderingContext2D ;
import org.teavm.jso.canvas.ImageData ;

import com.linxonline.mallet.util.Utility ;

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
	private final CanvasRenderingContext2D canvas ;

	public GLFontGenerator( final GLTextureManager _manager )
	{
		manager = _manager ;
		final HTMLDocument doc = HTMLDocument.current() ;
		final HTMLCanvasElement element = ( HTMLCanvasElement )doc.getElementById( "text-canvas" ) ;
		canvas = ( CanvasRenderingContext2D )element.getContext( "2d" ) ;
	}

	public GLFontMap generateFontMap( final String _name, final int _size, final String _charsToMap, final int _spacing )
	{
		final StringBuilder builder = new StringBuilder() ;
		builder.append( _size ) ;
		builder.append( "pt;" ) ;
		builder.append( _name ) ;

		final String font = builder.toString() ;
		canvas.setFont( font ) ;

		return generateFontMap( canvas, _charsToMap, _spacing ) ;
	}

	/**
		Needs to be updated to provide higher resolution font maps.
		We should render out a high resolution map, but use geometry 
		suitable for the font specified. Currently texture is generated 
		based on 
	*/
	public GLFontMap generateFontMap( final CanvasRenderingContext2D _canvas, final String _charsToMap, final int _spacing )
	{
		final int length = _charsToMap.length() ;
		float increment = 0 ;
		final char[] c = new char[1] ;
		final Glyph[] glyphs = new GLGlyph[length] ;

		for( int i = 0; i < length; i++ )
		{
			c[0] = _charsToMap.charAt( i ) ;
			final float start = increment + ( i * _spacing ) ;
			final float advance = _canvas.measureText( String.valueOf( c ) ).getWidth() ;

			glyphs[i] = new GLGlyph( c[0], start, advance ) ;
			increment += advance ;
		}

		// Create a GLFontMap and wrap it around a FontMap
		return new GLFontMap( new FontMap( glyphs, null, getHeight( _canvas, _charsToMap ) ) ) ;

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

		final HTMLCanvasElement element = canvas.getCanvas() ;
		element.setWidth( width ) ;
		element.setHeight( height ) ;

		final StringBuilder builder = new StringBuilder() ;
		builder.append( _font.size ) ;
		builder.append( "pt;" ) ;
		builder.append( _font.fontName ) ;

		canvas.setFont( builder.toString() ) ;
		canvas.setFillStyle( "#FFFFFF" ) ;
		canvas.clearRect( 0.0, 0.0, ( double )width, ( double )height ) ;

		final char[] c = new char[1] ;
		final double point = 1.0 / width ;
		final float ascent = height / 2.0f ;//10.0f ;

		final List<Shape> shapes = Utility.<Shape>newArrayList() ;

		for( int i = 0; i < length; i++ )
		{
			final GLGlyph glyph = ( GLGlyph )glyphs[i] ;
			//System.out.println( "Index: " + i + " Glyph: " + glyph ) ;
			if( glyph != null )
			{
				final float start = glyph.start ;
				final String txt = String.valueOf( glyph.character ) ;
				canvas.fillText( txt, start, ascent ) ;

				final float advance = glyph.advance ;
				final float x1 = ( float )( start * point ) ;
				final float x2 = ( float )( ( start + advance ) * point ) ;

				final Vector3 maxPoint = new Vector3( advance, height, 0.0f ) ;
				final Vector2 uv1 = new Vector2( x1, 0.0f ) ;
				final Vector2 uv2 = new Vector2( x2, 1.0f ) ;

				glyph.setShape( Shape.constructPlane( maxPoint, uv1, uv2 ) ) ;
			}
		}

		_map.fontMap.setTexture( manager.bind( canvas.getCanvas() ) ) ;
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

		final GLGlyph glyph = ( GLGlyph )_glyphs[_glyphs.length - 1] ;
		if( glyph != null )
		{
			width += glyph.advance ;
		}

		return width ;
	}

	private int getHeight( final CanvasRenderingContext2D _canvas, final String _text )
	{
		final HTMLDocument doc = HTMLDocument.current() ;
		final HTMLElement div = doc.createElement( "div" ) ;
		final HTMLElement txt = div.withText( _text ) ;

		div.withAttr( "style", _canvas.getFont() + ";position:absolute;top:0;left:0" ) ;
		doc.getBody().withChild( div ) ;

		final int height = div.getClientHeight() ;
		doc.getBody().removeChild( div ) ;

		return height ;
	}
}
