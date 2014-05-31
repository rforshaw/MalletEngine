package com.linxonline.mallet.renderer.android ;

import java.util.ArrayList ;

import android.graphics.* ;

import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.renderer.* ;

public class Android2DDraw
{
	private static final Path polygon = new Path() ;

	public static void drawLine( final Canvas _canvas,
								 final Settings _settings,
								 final Vector2 _position,
								 final Paint _paint )
	{
		final Line line = _settings.getObject( "DRAWLINE", null ) ;
		if( line != null )
		{
			_canvas.drawLine( line.start.x + _position.x,
								line.start.y + _position.y,
								line.end.x + _position.x,
								line.end.y + _position.y, _paint ) ;
		}
	}
	
	public static void drawLines( final Canvas _canvas,
								  final Settings _settings,
								  final Vector2 _position,
								  final Paint _paint )
	{
		final Shape shape = _settings.getObject( "DRAWLINES", null ) ;
		if( shape != null )
		{
			final int size = shape.indicies.length ;
			for( int i = 0; i < size; i += 2 )
			{
				final Vector2 start = shape.points[shape.indicies[i]] ;
				final Vector2 end = shape.points[shape.indicies[i + 1]] ;

				_canvas.drawLine( start.x + _position.x,
									start.y + _position.y,
									end.x + _position.x,
									end.y + _position.y, _paint ) ;
			}
		}
	}
	
	public static void drawPolygon( final Canvas _canvas,
								    final Settings _settings,
								    final Vector2 _position,
								    final Paint _paint )
	{
		final Shape shape = _settings.getObject( "DRAWPOLYGON", null ) ;
		if( shape != null )
		{
			final int size = shape.indicies.length ;
			final Vector2 start = shape.points[shape.indicies[0]] ;
			polygon.moveTo( start.x + _position.x, start.y + _position.y ) ;

			for( int i = 1; i < size; i++ )
			{
				final Vector2 point = shape.points[shape.indicies[i]] ;
				polygon.lineTo( point.x + _position.x, point.y + _position.y ) ;
			}

			polygon.lineTo( start.x + _position.x, start.y + _position.y ) ;
			polygon.close() ;

			_canvas.drawPath( polygon, _paint ) ;
			polygon.reset() ;
		}
	}

	public static void drawPoints( final Canvas _canvas,
								   final Settings _settings,
								   final Vector2 _position,
								   final Paint _paint )
	{
		final Shape shape = _settings.getObject( "POINTS", null ) ;
		if( shape != null )
		{
			for( final Integer index : shape.indicies )
			{
				final Vector2 point = shape.points[index] ;
				_canvas.drawLine( point.x + _position.x,
									point.y + _position.y,
									point.x + _position.x,
									point.y + _position.y, _paint ) ;
			}
		}
	}
}
