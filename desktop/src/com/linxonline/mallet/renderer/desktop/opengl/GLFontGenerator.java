package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.ArrayList ;

import com.jogamp.graph.curve.OutlineShape ;
import com.jogamp.graph.geom.Triangle ;
import com.jogamp.graph.geom.Vertex ;

import com.linxonline.mallet.renderer.Colour ;
import com.linxonline.mallet.renderer.Font ;
import com.linxonline.mallet.renderer.Glyph ;
import com.linxonline.mallet.renderer.Shape ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public final class GLFontGenerator
{
	public GLFontGenerator() {}

	public Font.Metrics generateMetrics( final Font _font, final com.jogamp.graph.font.Font _f, final String _characters )
	{
		final float scale = _font.getPointSize() ;
		final int style = _font.getStyle() ;

		final com.jogamp.graph.font.Font.Metrics metrics = _f.getMetrics() ;
		final int length = _characters.length() ;
		final Glyph[] glyphs = new Glyph[length] ;

		for( int i = 0; i < length; i++ )
		{
			final char c = _characters.charAt( i ) ;
			final int id = _f.getGlyphID​( c ) ;
			final float adv = _f.getAdvanceWidth( id ) * scale ;

			glyphs[i] = new Glyph( c, adv ) ;
		}

		return new Font.Metrics( glyphs, _f.getLineHeight() * scale,
										 metrics.getAscent() * scale,
										 metrics.getDescent() * scale,
										 metrics.getLineGap() * scale ) ;
	}

	public Glyph generateGlyph( final Font _font, final com.jogamp.graph.font.Font _f, final int _code )
	{
		final int style = _font.getStyle() ;
		final float scale = _font.getPointSize() ;

		final char c = ( char )_code ;
		final int id = _f.getGlyphID​( c ) ;
		return new Glyph( c, _f.getAdvanceWidth( id ) * scale ) ;
	}

	public Bundle generateFont( final Font _font, final com.jogamp.graph.font.Font _f )
	{
		final Font.Metrics metrics = _font.getMetrics() ;

		final Glyph[] glyphs = metrics.getGlyphs() ;
		final int length = glyphs.length ;
		final Shape[] shapes = new Shape[length] ;

		final Shape.Attribute[] swivel = new Shape.Attribute[3] ;
		swivel[0] = Shape.Attribute.VEC3 ;
		swivel[1] = Shape.Attribute.FLOAT ;
		swivel[2] = Shape.Attribute.VEC2 ;

		final Vector3 position = new Vector3() ;
		final Colour white = Colour.white() ;
		final Vector2 uv = new Vector2() ;
		final Object[] vertex = new Object[] { position, white, uv } ; 

		for( int i = 0; i < length; i++ )
		{
			final Glyph glyph = glyphs[i] ;
			if( glyph == null )
			{
				continue ;
			}

			final char c = glyph.getCharacter() ;
			final int id = _f.getGlyphID​( c ) ;
			com.jogamp.graph.font.Font.Glyph g = _f.getGlyph​( id ) ;
			if( g.isWhitespace() || g.isUndefined() )
			{
				shapes[i] = new Shape( Shape.Style.FILL, swivel, 0, 0 ) ;
				continue ;
			}

			final float width = glyph.getWidth() ;
			final float height = metrics.getHeight() ;

			final OutlineShape shape = g.getShape() ;
			final OutlineShape.VerticesState state = OutlineShape.VerticesState.QUADRATIC_NURBS ;
			final ArrayList<Triangle> triangles = shape.getTriangles( state ) ;

			final int size = triangles.size() * 3 ;

			final Shape plane = new Shape( Shape.Style.FILL, swivel, size, size ) ;
			shapes[i] = plane ;

			int index = 0 ;
			for( final Triangle tri : triangles )
			{
				for( final Vertex v : tri.getVertices() )
				{
					position.setXYZ( v.x(), -v.y(), v.z() ) ;
					plane.copyVertex( vertex ) ;
					plane.addIndex( index++ ) ;
				}
			}
		}

		return new Bundle( shapes ) ;
	}

	public static class Bundle
	{
		public final Shape[] shapes ;

		public Bundle( final Shape[] _shapes )
		{
			shapes = _shapes ;
		}
	}
}
