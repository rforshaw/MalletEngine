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

	public static Shape triangulate( final Shape _shape )
	{
		if( _shape.indicies.length <= 3 )
		{
			return new Shape( _shape ) ;
		}

		final ArrayList<Integer> indicies = constructTriangulatedIndex( _shape ) ;
		final Shape triangulated = new Shape( indicies.size(), _shape.points.length, _shape.colours.length ) ;
		triangulated.setStyle( _shape.style ) ;

		for( final Integer index : indicies )
		{
			triangulated.addIndex( index ) ;
		}

		{
			final int size = _shape.points.length ;
			for( int i = 0; i < size; i++ )
			{
				final Vector3 point = _shape.points[i] ;
				triangulated.addPoint( point.x, point.y, point.z ) ;
			}
		}

		if( _shape.colours != null )
		{
			final int size = _shape.colours.length ;
			for( int i = 0; i < size; i++ )
			{
				triangulated.addColour( new MalletColour( _shape.colours[i] ) ) ;
			}
		}

		return triangulated ;
	}

	private static ArrayList<Integer> constructTriangulatedIndex( final Shape _shape )
	{
		final ArrayList<Vector3> vertices = new ArrayList<Vector3>( _shape.points.length ) ;
		for( final Vector3 point : _shape.points )
		{
			vertices.add( point ) ;
		}

		final ArrayList<Integer> newIndicies = new ArrayList<Integer>() ;
		{
			int size = vertices.size() ;
			while( size >= 3 )
			{
				for( int i = 1; i < size - 1; i++ )
				{
					final Vector3 previous = vertices.get( i - 1 ) ;
					final Vector3 current = vertices.get( i ) ;
					final Vector3 next = vertices.get( i + 1 ) ;

					if( isInteriorVertex( current, previous, next ) == true &&
						isTriangleEmpty( current, previous, next, _shape.points ) == true )
					{
						newIndicies.add( i - 1 ) ;
						newIndicies.add( i ) ;
						newIndicies.add( i + 1 ) ;

						vertices.remove( i ) ;
						break ;
					}
				}

				size = vertices.size() ;
				System.out.println( "Size: " + size ) ;
			}
		}

		return newIndicies ;
	}

	private static boolean isInteriorVertex( final Vector3 _current, final Vector3 _previous, final Vector3 _next )
	{
		final float area1 = ( _previous.x * _current.y ) - ( _previous.y * _current.x ) ;
		final float area2 = ( _next.x * _current.y ) - ( _next.y * _current.x ) ;
		
		System.out.println( "Area1: " + area1 + " Area2: " + area2 ) ;
		return area1 >= 0.0f && area2 >= 0.0f ;
	}

	private static boolean isTriangleEmpty( final Vector3 _current, final Vector3 _previous, final Vector3 _next, final Vector3[] _points )
	{
		/*for( int i = 0; i < _points.length; i++ )
		{
			final boolean b1 = sign( _points[i], _current, _previous ) < 0.0f ;
			final boolean b2 = sign( _points[i], _current, _next ) < 0.0f ;
			final boolean b3 = sign( _points[i], _previous, _next ) < 0.0f ;

			if( ( b1 == b2 ) && ( b2 == b3 ) )
			{
				return false ;
			}
		}*/

		return true ;
	}

	private static float sign( final Vector3 _point, final Vector3 _tri1, final Vector3 _tri2 )
	{
		return ( _point.x - _tri2.x ) * ( _tri1.y - _tri2.y ) - ( _tri1.x - _tri2.x ) * ( _point.y - _tri2.y ) ;
	}
}