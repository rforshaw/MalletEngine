package com.linxonline.mallet.physics ;

import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.Interpolation ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.physics.hulls.Hull ;

public class Debug
{
	private Debug() {}

	public static Draw createDraw( final Hull _hull )
	{
		final Draw draw = DrawAssist.createDraw( new Vector3(),
													new Vector3(),
													new Vector3(),
													new Vector3( 1, 1, 1 ),
													10 ) ;

		final Shape.Swivel[] swivel = new Shape.Swivel[2] ;
		swivel[0] = Shape.Swivel.POINT ;
		swivel[1] = Shape.Swivel.COLOUR ;

		final Shape shape = new Shape( Shape.Style.LINE_STRIP, swivel, 5, 4 ) ;
		final MalletColour white = MalletColour.white() ;

		final float[] points = _hull.getPoints() ;
		int index = 0 ;
		for( int i = 0; i < points.length; i += 2 )
		{
			final float x = points[i + 0] ;
			final float y = points[i + 1] ;

			shape.addVertex( Shape.Swivel.createVert( new Vector3( x, y, 0.0f ), white ) ) ;
			shape.addIndex( index++ ) ;
		}

		shape.addIndex( 0 ) ;

		final Vector2 pos = _hull.getPosition() ;
		DrawAssist.amendPosition( draw, pos.x, pos.y, 0.0f ) ;

		DrawAssist.amendShape( draw, shape ) ;
		DrawAssist.attachProgram( draw, ProgramAssist.create( "SIMPLE_GEOMETRY" ) ) ;
		DrawAssist.amendInterpolation( draw, Interpolation.LINEAR ) ;

		return draw ;
	}

	public static void updateDraw( final Draw _draw, final Hull _hull )
	{
		final Vector2 pos = _hull.getPosition() ;
		DrawAssist.amendPosition( _draw, pos.x, pos.y, 0.0f ) ;

		final Shape shape = DrawAssist.getDrawShape( _draw ) ;

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
