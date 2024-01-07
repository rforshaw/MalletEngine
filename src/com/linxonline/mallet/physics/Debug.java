package com.linxonline.mallet.physics ;

import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.Interpolation ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public final class Debug
{
	private Debug() {}

	public static Draw createDraw( final Hull _hull )
	{
		final Vector2 pos = _hull.getPosition( new Vector2() ) ;
		final Vector2 off = _hull.getOffset( new Vector2() ) ;

		final Draw draw = new Draw( pos.x, pos.y, 0.0f,
									off.x, off.y, 0.0f ) ;

		final Shape.Attribute[] swivel = new Shape.Attribute[2] ;
		swivel[0] = Shape.Attribute.VEC3 ;
		swivel[1] = Shape.Attribute.FLOAT ;

		final Shape shape = new Shape( Shape.Style.LINE_STRIP, swivel, 5, 4 ) ;
		final MalletColour white = MalletColour.white() ;

		final float[] points = _hull.getPoints() ;
		int index = 0 ;
		for( int i = 0; i < points.length; i += 2 )
		{
			final float x = points[i + 0] ;
			final float y = points[i + 1] ;

			shape.copyVertex( Shape.Attribute.createVert( new Vector3( x, y, 0.0f ), white ) ) ;
			shape.addIndex( index++ ) ;
		}

		shape.addIndex( 0 ) ;
		draw.setShape( shape ) ;

		return draw ;
	}

	public static void updateDraw( final Draw _draw, final Hull _hull )
	{
		final Vector2 pos = _hull.getPosition( new Vector2() ) ;
		final Vector2 offset = _hull.getOffset( new Vector2() ) ;

		_draw.setPosition( pos.x, pos.y, 0.0f ) ;
		_draw.setOffset( offset.x, offset.y, 0.0f ) ;

		final Shape shape = ( Shape )_draw.getShape() ;

		int index = 0 ;
		final float[] points = _hull.getPoints() ;
		for( int i = 0; i < points.length; i += 2 )
		{
			final float x = points[i + 0] ;
			final float y = points[i + 1] ;

			shape.setVector3( index++, 0,  x, y, 0.0f ) ;
		}
	}
}
