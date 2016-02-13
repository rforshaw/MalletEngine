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

	public enum Swivel
	{
		POINT,
		COLOUR,
		UV,
		NORMAL ;
		
		public static int getSwivelPointIndex( final Swivel[] _swivel )
		{
			for( int i = 0; i < _swivel.length; i++ )
			{
				if( _swivel[i] == Swivel.POINT )
				{
					return i ;
				}
			}

			return -1 ;
		}
	}

	private final Swivel[] swivel ;
	private final Object[] verticies ;
	public final int[] indicies ;

	public Style style = Style.LINE_STRIP ;

	private int vertexSize = 0 ;
	private int indexIncrement = 0 ;
	private int vertexIncrement = 0 ;

	public Shape( final int _indexSize,
				  final int _pointSize )
	{
		this( Style.LINE_STRIP, _indexSize, _pointSize ) ;
	}

	public Shape( final Style _style,
				  final int _indexSize,
				  final int _pointSize )
	{
		swivel = new Swivel[2] ;
		swivel[0] = Swivel.POINT ;
		swivel[1] = Swivel.COLOUR ;

		verticies   = new Object[swivel.length * _pointSize] ;
		indicies    = new int[_indexSize] ;

		style       = _style ;
		vertexSize  = _pointSize ; 
	}

	public Shape( final Style _style,
				  final Swivel[] _swivel,
				  final int _indexSize,
				  final int _pointSize )
	{
		swivel = _swivel ;
		verticies   = new Object[swivel.length * _pointSize] ;
		indicies    = new int[_indexSize] ;

		style       = _style ;
		vertexSize  = _pointSize ;
	}

	public Shape( final Shape _shape )
	{
		swivel = new Swivel[_shape.swivel.length] ;
		verticies   = new Object[_shape.verticies.length] ;
		indicies    = new int[_shape.indicies.length] ;
		style       = _shape.style ;

		indexIncrement  = _shape.indexIncrement ;
		vertexIncrement = _shape.vertexIncrement ;
		vertexSize      = _shape.vertexSize ;

		for( int i = 0; i < swivel.length; i++ )
		{
			swivel[i] = _shape.swivel[i] ;
		}

		for( int i = 0; i < indicies.length; i++ )
		{
			indicies[i] = _shape.indicies[i] ;
		}

		for( int i = 0; i < verticies.length; i++ )
		{
			final Object obj = _shape.verticies[i] ;
			if( obj instanceof Vector3 )
			{
				verticies[i] = new Vector3( ( Vector3 )_shape.verticies[i] ) ;
			}
			else if( obj instanceof Vector2 )
			{
				verticies[i] = new Vector2( ( Vector2 )_shape.verticies[i] ) ;
			}
			else if( obj instanceof MalletColour )
			{
				verticies[i] = new MalletColour( ( MalletColour )_shape.verticies[i] ) ;
			}
		}
	}

	private Shape( final Swivel[] _swivel,
				   final Object[] _verticies,
				   final int[] _indicies,
				   final Style _style,
				   final int _vertexSize )
	{
		swivel      = _swivel ;
		verticies   = _verticies ;
		indicies    = _indicies ;
		style       = _style ;

		//indexIncrement = _shape.indexIncrement ;
		//vertexIncrement = _shape.vertexIncrement ;
		vertexSize = _vertexSize ;
	}

	public void addIndex( final int _index )
	{
		indicies[indexIncrement++] = _index ;
	}

	public void addVertex( final Object[] _vertex )
	{
		for( int i = 0; i < _vertex.length; i++ )
		{
			verticies[vertexIncrement++] = _vertex[i] ;
		}
	}

	public boolean getVertex( final Object[] _vertex, final int _index )
	{
		if( _vertex.length != swivel.length )
		{
			return false ;
		}

		int start = _index * swivel.length ;
		for( int i = 0; i < _vertex.length; i++ )
		{
			_vertex[i] = verticies[start++] ;
		}

		return true ;
	}

	public Vector3 getPoint( final int _index, final int _swivelIndex )
	{
		final int index = ( _index * swivel.length ) + _swivelIndex ;
		return ( Vector3 )verticies[index] ;
	}

	public Vector2 getUV( final int _index, final int _swivelIndex )
	{
		final int index = ( _index * swivel.length ) + _swivelIndex ;
		return ( Vector2 )verticies[index] ;
	}

	public MalletColour getColour( final int _index, final int _swivelIndex )
	{
		final int index = ( _index * swivel.length ) + _swivelIndex ;
		return ( MalletColour )verticies[index] ;
	}

	public Vector3 getNormal( final int _index, final int _swivelIndex )
	{
		final int index = ( _index * swivel.length ) + _swivelIndex ;
		return ( Vector3 )verticies[index] ;
	}

	public Shape.Style getStyle()
	{
		return style ;
	}

	public Swivel[] getSwivel()
	{
		return swivel ;
	}

	public int getIndexSize()
	{
		return indicies.length ;
	}
	
	public int getVertexSize()
	{
		return vertexSize ;
	}

	/**
		Inform the developer whether the Shape 
		has been correctly populated with data.
	*/
	public boolean isComplete()
	{
		return indexIncrement == indicies.length &&
			   vertexIncrement == verticies.length ;
	}

	public static Object[] construct( final float _x, final float _y, final float _z, final MalletColour _colour )
	{
		final Object[] swivel = new Object[2] ;
		swivel[0] = new Vector3( _x, _y, _z ) ;
		swivel[1] = new MalletColour( _colour ) ;

		return swivel ;
	}
	
	public static boolean isCorrectSwivel( final Swivel[] _swivel, final Object[] _object )
	{
		for( int i = 0; i < _swivel.length; i++ )
		{
			switch( _swivel[i] )
			{
				case POINT  :
				{
					if( ( _object[i] instanceof Vector3 ) == false )
					{
						return false ;
					}
					break ;
				}
				case COLOUR :
				{
					if( ( _object[i] instanceof MalletColour ) == false )
					{
						return false ;
					}
					break ;
				}
				case UV     :
				{
					if( ( _object[i] instanceof Vector2 ) == false )
					{
						return false ;
					}
					break ;
				}
				case NORMAL  :
				{
					if( ( _object[i] instanceof Vector3 ) == false )
					{
						return false ;
					}
					break ;
				}
			}
		}

		return true ;
	}

	public static Shape constructPlane( final Vector3 _length,
										final Vector2 _minUV,
										final Vector2 _maxUV )
	{
		final Swivel[] swivel = new Swivel[3] ;
		swivel[0] = Swivel.POINT ;
		swivel[1] = Swivel.COLOUR ;
		swivel[2] = Swivel.UV ;

		final MalletColour white = MalletColour.white() ;

		final Shape plane = new Shape( Shape.Style.FILL, swivel, 6, 4 ) ;
		plane.addVertex( new Object[] { new Vector3(), white, new Vector2( _minUV ) } ) ;
		plane.addVertex( new Object[] { new Vector3( _length ), white, new Vector2( _maxUV ) } ) ;
		plane.addVertex( new Object[] { new Vector3( 0.0f, _length.y, 0.0f ), white, new Vector2( _minUV.x, _maxUV.y ) } ) ;
		plane.addVertex( new Object[] { new Vector3( _length.x, 0.0f, 0.0f ), white, new Vector2( _maxUV.x, _minUV.y ) } ) ;

		plane.addIndex( 0 ) ;
		plane.addIndex( 2 ) ;
		plane.addIndex( 1 ) ;

		plane.addIndex( 0 ) ;
		plane.addIndex( 1 ) ;
		plane.addIndex( 3 ) ;

		return plane ;
	}

	public static Shape updatePlaneUV( final Shape _plane, final Vector2 _minUV, final Vector2 _maxUV )
	{
		_plane.getUV( 0, 2 ).setXY( _minUV ) ;
		_plane.getUV( 1, 2 ).setXY( _maxUV ) ;
		_plane.getUV( 2, 2 ).setXY( _minUV.x, _maxUV.y ) ;
		_plane.getUV( 3, 2 ).setXY( _maxUV.x, _minUV.y ) ;

		return _plane ;
	}

	/**
		Combine an array of shape objects into 1 shape object.
		Ensure all the shapes being combined have the same style.
		This is a convience function to improve rendering performance, 
		instead of multiple draw calls per shape.
		Constructing multiple shapes and then combining them is slow 
		and memory intensive.
	*/
	public static Shape combine( final Shape ... _shapes )
	{
		int totalIndicies = 0 ;
		int totalPoints = 0 ;

		for( int i = 0; i < _shapes.length; i++ )
		{
			totalIndicies += _shapes[i].indicies.length ;
			totalPoints += _shapes[i].getVertexSize() ;
		}

		final Shape combined = new Shape( _shapes[0].style, _shapes[0].swivel, totalIndicies, totalPoints ) ;
		int indexOffset = 0 ;

		for( int i = 0; i < _shapes.length; i++ )
		{
			final Shape shape = _shapes[i] ;
			for( int j = 0; j < shape.indicies.length; j++ )
			{
				combined.addIndex( indexOffset + shape.indicies[j] ) ;
			}

			final Object[] vertex = new Object[_shapes[0].swivel.length] ;
			final int size = shape.getVertexSize() ;

			for( int j = 0; j < size; j++ )
			{
				shape.getVertex( vertex, j ) ;
				combined.addVertex( vertex ) ;
			}

			indexOffset += size ;
		}

		return combined ;
	}

	/**
		Return a shape object that references the 
		vertex and colour arrays of _shape.
	*/
	public static Shape triangulate( final Shape _shape )
	{
		if( _shape.indicies.length <= 3 )
		{
			return new Shape( _shape.swivel,
							  _shape.verticies,
							  _shape.indicies,
							  _shape.style,
							  _shape.getVertexSize() ) ;
		}

		final ArrayList<Integer> tempIndicies = constructTriangulatedIndex( _shape ) ;
		final int[] indicies = new int[tempIndicies.size()] ;
		for( int i = 0; i < indicies.length; i++ )
		{
			indicies[i] = tempIndicies.get( i ) ;
		}

		final Shape triangulated = new Shape( _shape.swivel,
											  _shape.verticies,
											  indicies,
											  _shape.style,
											  _shape.getVertexSize() ) ;
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
		final int swivelPointIndex = Swivel.getSwivelPointIndex( _shape.swivel ) ;
		int size = indicies.size() ;

		while( size >= 3 )
		{
			for( int i = 1; i < size - 1; i++ )
			{
				final int previousIndex = indicies.get( i - 1 ) ;
				final int currentIndex = indicies.get( i ) ;
				final int nextIndex = indicies.get( i + 1 ) ;

				final Vector3 previous = _shape.getPoint( previousIndex, swivelPointIndex ) ;
				final Vector3 current = _shape.getPoint( currentIndex, swivelPointIndex ) ;
				final Vector3 next = _shape.getPoint( nextIndex, swivelPointIndex ) ;

				if( isInteriorVertex( current, previous, next ) == true &&
					isTriangleEmpty( current, previous, next, indicies, _shape ) == true )
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
											final Shape _shape )
	{
		final int swivelPointIndex = Swivel.getSwivelPointIndex( _shape.swivel ) ;
		final int size = _indicies.size() ; 

		for( int i = 0; i < size; i++ )
		{
			final Vector3 point = _shape.getPoint( _indicies.get( i ), swivelPointIndex ) ;
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