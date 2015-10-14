package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;

import com.linxonline.mallet.maths.* ;

public class Shape
{
	public enum Style
	{
		LINES,				// Requires a start and an end point to be defined for each line
		LINE_STRIP, 		// Will continue the line from the last point added
		FILL ; 				// Fill the geometry shape, requires the shape to be defined in polygons, will eventuall be auto generated.
	}

	public final int[] indicies ;
	public final Vector3[] points ;
	public final MalletColour[] colours ;

	public Style style = Style.LINE_STRIP ;

	private int indexIncrement = 0 ;
	private int pointIncrement = 0 ;
	private int colourIncrement = 0 ;

	public Shape( final int _indexSize, final int _pointSize, final int _colourSize )
	{
		indicies = new int[_indexSize] ;
		points = new Vector3[_pointSize] ;
		colours = new MalletColour[_colourSize] ;
	}

	public Shape( final int _indexSize, final int _pointSize )
	{
		indicies = new int[_indexSize] ;
		points = new Vector3[_pointSize] ;
		colours = null ;
	}

	public Shape( final Shape _shape )
	{
		{
			final int size = _shape.indicies.length ;
			indicies = new int[size] ;
			for( int i = 0; i < size; i++ )
			{
				indicies[i] = _shape.indicies[i] ;
			}
		}

		{
			final int size = _shape.points.length ;
			points = new Vector3[size] ;
			for( int i = 0; i < size; i++ )
			{
				points[i] = new Vector3( _shape.points[i] ) ;
			}
		}

		if( _shape.colours != null )
		{
			final int size = _shape.colours.length ;
			colours = new MalletColour[size] ;
			for( int i = 0; i < size; i++ )
			{
				colours[i] = new MalletColour( _shape.colours[i] ) ;
			}
		}
		else
		{
			colours = null ;
		}

		setStyle( _shape.style ) ;
	}

	private Shape( final int[] _indicies, final Vector3[] _points, final MalletColour[] _colours )
	{
		indicies = _indicies ;
		points = _points ;
		colours = _colours ;
	}

	public void setStyle( final Style _style )
	{
		style = _style ;
	}

	public void addIndex( final int _index )
	{
		if( indexIncrement < indicies.length )
		{
			indicies[indexIncrement++] = _index ;
		}
	}

	public void addPoint( final float _x, final float _y )
	{
		this.addPoint( _x, _y, 0.0f ) ;
	}

	public void addPoint( final float _x, final float _y, final float _z )
	{
		if( pointIncrement < points.length )
		{
			points[pointIncrement++] = new Vector3( _x, _y, _z ) ;
		}
	}

	public void addColour( final MalletColour _colour )
	{
		if( colourIncrement < colours.length )
		{
			colours[colourIncrement++] = _colour ;
		}
	}

	public Vector3 getPoint( final int _index )
	{
		return points[_index] ;
	}

	public MalletColour getColour( final int _index )
	{
		return colours[_index] ;
	}

	/**
		Return a shape object that references the 
		vertex and colour arrays of _shape.
	*/
	public static Shape triangulate( final Shape _shape )
	{
		if( _shape.indicies.length <= 3 )
		{
			return new Shape( _shape.indicies, _shape.points, _shape.colours ) ;
		}

		final ArrayList<Integer> tempIndicies = constructTriangulatedIndex( _shape ) ;
		final int[] indicies = new int[tempIndicies.size()] ;
		for( int i = 0; i < indicies.length; i++ )
		{
			indicies[i] = tempIndicies.get( i ) ;
		}

		final Shape triangulated = new Shape( indicies, _shape.points, _shape.colours ) ;
		triangulated.setStyle( _shape.style ) ;
		return triangulated ;
	}

	/**
		Construct a new index array that triangulates 
		the points stored in _shape.points.
		The point and colour array are not modified.
	*/
	private static ArrayList<Integer> constructTriangulatedIndex( final Shape _shape )
	{
		final ArrayList<Integer> indicies = new ArrayList<Integer>( _shape.indicies.length ) ;
		for( final Integer index : _shape.indicies )
		{
			indicies.add( index ) ;
		}

		final ArrayList<Integer> newIndicies = new ArrayList<Integer>() ;
		int size = indicies.size() ;

		while( size >= 3 )
		{
			for( int i = 1; i < size - 1; i++ )
			{
				final int previousIndex = indicies.get( i - 1 ) ;
				final int currentIndex = indicies.get( i ) ;
				final int nextIndex = indicies.get( i + 1 ) ;

				final Vector3 previous = _shape.getPoint( previousIndex ) ;
				final Vector3 current = _shape.getPoint( currentIndex ) ;
				final Vector3 next = _shape.getPoint( nextIndex ) ;

				if( isInteriorVertex( current, previous, next ) == true &&
					isTriangleEmpty( current, previous, next, indicies, _shape.points ) == true )
				{
					newIndicies.add( previousIndex ) ;
					newIndicies.add( currentIndex ) ;
					newIndicies.add( nextIndex ) ;

					indicies.remove( i ) ;
					break ;
				}
			}

			size = indicies.size() ;
		}

		return newIndicies ;
	}

	private static boolean isInteriorVertex( final Vector3 _current, final Vector3 _previous, final Vector3 _next )
	{
		final float area1 = ( _previous.x * _current.y ) - ( _previous.y * _current.x ) ;
		final float area2 = ( _next.x * _current.y ) - ( _next.y * _current.x ) ;
		return area1 >= 0.0f && area2 >= 0.0f ;
	}

	private static boolean isTriangleEmpty( final Vector3 _current,
											final Vector3 _previous,
											final Vector3 _next,
											final ArrayList<Integer> _indicies,
											final Vector3[] _points )
	{
		final int size = _indicies.size() ; 
		for( int i = 0; i < size; i++ )
		{
			final Vector3 point = _points[_indicies.get( i )] ;
			if( point != _current && point != _previous && point != _next )
			{
				final boolean b1 = sign( point, _current, _previous ) < 0.0f ;
				final boolean b2 = sign( point, _current, _next ) < 0.0f ;
				final boolean b3 = sign( point, _previous, _next ) < 0.0f ;

				if( ( b1 == b2 ) && ( b2 == b3 ) )
				{
					return false ;
				}
			}
		}

		return true ;
	}

	private static float sign( final Vector3 _p1, final Vector3 _p2, final Vector3 _p3 )
	{
		return ( _p1.x - _p3.x ) * ( _p2.y - _p3.y ) - ( _p2.x - _p3.x ) * ( _p1.y - _p3.y ) ;
	}
}