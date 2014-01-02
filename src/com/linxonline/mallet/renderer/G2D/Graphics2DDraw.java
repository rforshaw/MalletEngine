package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;
import java.awt.* ;
import java.awt.image.BufferStrategy ;
import java.awt.geom.AffineTransform ;

import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.resources.texture.* ;

public class Graphics2DDraw
{
	private static final Vector2 DEFAULT_OFFSET = new Vector2( 0, 0 ) ;
	private static final String BLANK_TEXT = "" ;
	private static final Polygon polygon = new Polygon() ;

	/**
		Draw a line, defining the start and end locations.
	*/
	public static void drawLine( final Graphics2D _graphics,
								final Settings _settings,
								final Vector2 _position )
	{
		final Line line = ( Line )_settings.getObject( "DRAWLINE", null ) ;
		if( line != null )
		{
			_graphics.drawLine( ( int )( line.start.x + _position.x ),
								( int )( line.start.y + _position.y ),
								( int )( line.end.x + _position.x ),
								( int )( line.end.y + _position.y ) ) ;
		}
	}

	/**
		Draw a set of lines, starting from the last point drawn.
	*/
	public static void drawLines( final Graphics2D _graphics,
								 final Settings _settings,
								 final Vector2 _position )
	{
		final Shape shape = _settings.getObject( "DRAWLINES", null ) ;
		if( shape != null )
		{
			final int size = shape.indicies.size() - 1 ;
			for( int i = 0; i < size; ++i )
			{
				final Vector2 start = shape.points.get( shape.indicies.get( i ) ) ;
				final Vector2 end = shape.points.get( shape.indicies.get( i + 1 ) ) ;

				_graphics.drawLine( ( int )( start.x + _position.x ),
									( int )( start.y + _position.y ),
									( int )( end.x + _position.x ),
									( int )( end.y + _position.y ) ) ;
			}
		}
	}

	/**
		Draw a filled polygon, starting from the last point drawn.
	*/
	public static void drawPolygon( final Graphics2D _graphics,
								   final Settings _settings,
								   final Vector2 _position )
	{
		final Shape shape = _settings.getObject( "DRAWPOLYGON", null ) ;
		if( shape != null )
		{
			final int size = shape.indicies.size() ;
			for( int i = 0; i < size; i++ )
			{
				final Vector2 point = shape.points.get( shape.indicies.get( i ) ) ;
				polygon.addPoint( ( int )( point.x + _position.x ),
								( int )( point.y + _position.y ) ) ;
			}

			_graphics.fillPolygon( polygon ) ;
			polygon.reset() ;
		}
	}

	/**
		Draw the list of points definied.
	*/
	public static void drawPoints( final Graphics2D _graphics,
								  final Settings _settings,
								  final Vector2 _position )
	{
		final Shape shape = _settings.getObject( "POINTS", null ) ;
		final Vector2 offset = _settings.getObject( "OFFSET", DEFAULT_OFFSET ) ;
		if( shape != null )
		{
			for( final Integer index : shape.indicies )
			{
				final Vector2 point = shape.points.get( index ) ;
				_graphics.drawLine( ( int )( point.x + _position.x + offset.x ),
									( int )( point.y + _position.y + offset.y ),
									( int )( point.x + _position.x + offset.x ),
									( int )( point.y + _position.y + offset.y ) ) ;
			}
		}
	}

	/**
		Define the region of space that is allowed to be drawn on.
	*/
	public static void setClip( final Graphics2D _graphics,
							   final Settings _settings,
							   final Vector2 _position )
	{
		final Vector2 clipOffset = _settings.getObject( "CLIPOFFSET", DEFAULT_OFFSET ) ;
		final Vector2 clip = _settings.getObject( "CLIP", null ) ;
		final Vector2 position = new Vector2( _position.x + clipOffset.x, _position.y + clipOffset.y ) ;

		if( clip != null )
		{
			_graphics.setClip( ( int )position.x, ( int )position.y, ( int )( clip.x ), ( int )( clip.y ) ) ;
		}
	}

	/**
		Define the colour to use when rendering objects affected by it.
		Textures are not affected by this.
	*/
	public static void setGraphicsColour( final Graphics2D _graphics, final Settings _settings )
	{
		final MalletColour colour = _settings.getObject( "COLOUR", null ) ;
		if( colour != null )
		{
			if( colour.colour == null )
			{
				colour.setColour( ( Object )new Color( colour.red, colour.green, colour.blue ) ) ;
			}

			_graphics.setColor( ( Color )colour.colour ) ;
			return ;
		}
		// Set to White if not specified
		_graphics.setColor( Color.WHITE ) ;
	}
}