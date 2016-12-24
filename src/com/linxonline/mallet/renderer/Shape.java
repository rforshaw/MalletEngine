package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.Utility ;

public class Shape
{
	public enum Style
	{
		LINES,				// Requires a start and an end point to be defined for each line
		LINE_STRIP, 		// Will continue the line from the last point added
		FILL ; 				// Fill the geometry shape, requires the shape to be defined in polygons, will eventuall be auto generated.

		public static Style getStyleByString( final String _text )
		{
			switch( _text )
			{
				case "LINES"      : return LINES ;
				case "LINE_STRIP" : return LINE_STRIP ;
				case "FILL"       : return FILL ;
				default           : return LINES ;
			}
		}
	}

	public enum Swivel
	{
		POINT,		// Vector3
		COLOUR,		// MalletColour
		UV,			// Vector2
		NORMAL ;	// Vector3

		public static Swivel[] constructDefault()
		{
			final Swivel[] swivel = new Swivel[2] ;
			swivel[0] = Swivel.POINT ;
			swivel[1] = Swivel.COLOUR ;
			return swivel ;
		}
		
		public static Swivel[] getSwivelByArray( final List<String> _text )
		{
			final int size = _text.size() ;
			final Swivel[] swivel = new Swivel[size] ;

			for( int i = 0; i < size; i++ )
			{
				swivel[i] = getSwivelByString( _text.get( i ) ) ;
			}

			return swivel ;
		}

		public static Swivel getSwivelByString( final String _text )
		{
			switch( _text )
			{
				case "POINT"  : return POINT ;
				case "COLOUR" : return COLOUR ;
				case "UV"     : return UV ;
				case "NORMAL" : return NORMAL ;
				default       : return POINT ;
			}
		}

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

		public static int getSwivelFloatSize( final Swivel[] _swivel, final int _length )
		{
			int size = 0 ;
			for( int i = 0; i < _length; i++ )
			{
				switch( _swivel[i] )
				{
					case POINT  : size += 3 ; break ;	// Vector3
					case COLOUR : size += 1 ; break ;	// MalletColour
					case UV     : size += 2 ; break ;	// Vector2
					case NORMAL : size += 3 ; break ;	// Vector3
				}
			}

			return size ;
		}

		public static Object[] construct( final Swivel[] _swivel )
		{
			final Object[] obj = new Object[_swivel.length] ;
			for( int i = 0; i < _swivel.length; i++ )
			{
				switch( _swivel[i] )
				{
					case POINT  : obj[i] = new Vector3() ;      break ;
					case COLOUR : obj[i] = new MalletColour() ; break ;
					case UV     : obj[i] = new Vector2() ;      break ;
					case NORMAL : obj[i] = new Vector3() ;      break ;
				}
			}

			return obj ;
		}
	}

	private static Shape.Factory factory = new DefaultFactory() ;

	/**
		Allow the backend system to define a different 
		shape implementation. The Default Factory should be 
		fine for the majority of platforms: desktop and android.
		Web will require a custom implementation.
	*/
	public static void setFactory( final Shape.Factory _factory )
	{
		factory = _factory ;
	}

	private final Shape.Interface shape ;

	public Shape( final int _indexSize, final int _pointSize )
	{
		this( Style.LINE_STRIP, _indexSize, _pointSize ) ;
	}

	public Shape( final Style _style, final int _indexSize, final int _pointSize )
	{
		this( _style, Swivel.constructDefault(), _indexSize, _pointSize ) ;
	}

	public Shape( final Style _style,
				  final Swivel[] _swivel,
				  final int _indexSize,
				  final int _pointSize )
	{
		shape = factory.create() ;
		shape.init( _style, _swivel, _indexSize, _pointSize ) ;
	}

	public Shape( final Shape _shape )
	{
		shape = factory.create() ;
		shape.init( _shape.shape ) ;
	}

	public void addIndex( final int _index )
	{
		shape.addIndex( _index ) ;
	}

	public int getIndex( final int _index )
	{
		return shape.getIndex( _index ) ;
	}

	public void addVertex( final Object[] _vertex )
	{
		shape.addVertex( _vertex ) ;
	}

	public Object[] getVertex( final Object[] _vertex, final int _index )
	{
		return shape.getVertex( _vertex, _index ) ;
	}

	public void setVector3( final int _index, final int _swivelIndex, final Vector3 _point )
	{
		shape.setVector3( _index, _swivelIndex, _point ) ;
	}

	public void setVector3( final int _index, final int _swivelIndex, final float _x, final float _y, final float _z )
	{
		shape.setVector3( _index, _swivelIndex, _x, _y, _z ) ;
	}

	public Vector3 getVector3( final int _index, final int _swivelIndex )
	{
		return shape.getVector3( _index, _swivelIndex ) ;
	}

	public Vector3 getVector3( final int _index, final int _swivelIndex, final Vector3 _point )
	{
		return shape.getVector3( _index, _swivelIndex, _point ) ;
	}

	public void setVector2( final int _index, final int _swivelIndex, final Vector2 _point )
	{
		shape.setVector2( _index, _swivelIndex, _point ) ;
	}

	public void setVector2( final int _index, final int _swivelIndex, final float _x, final float _y )
	{
		shape.setVector2( _index, _swivelIndex, _x, _y ) ;
	}

	public Vector2 getVector2( final int _index, final int _swivelIndex )
	{
		return shape.getVector2( _index, _swivelIndex ) ;
	}

	public Vector2 getVector2( final int _index, final int _swivelIndex, final Vector2 _uv )
	{
		return shape.getVector2( _index, _swivelIndex, _uv ) ;
	}

	public float getFloat( final int _index, final int _swivelIndex )
	{
		return shape.getFloat( _index, _swivelIndex ) ;
	}
	
	public void setColour( final int _index, final int _swivelIndex, final MalletColour _colour )
	{
		shape.setColour( _index, _swivelIndex, _colour ) ;
	}

	public MalletColour getColour( final int _index, final int _swivelIndex )
	{
		return shape.getColour( _index, _swivelIndex ) ;
	}

	public MalletColour getColour( final int _index, final int _swivelIndex, final MalletColour _colour )
	{
		return shape.getColour( _index, _swivelIndex, _colour ) ;
	}

	public Shape.Style getStyle()
	{
		return shape.getStyle() ;
	}

	public Swivel[] getSwivel()
	{
		return shape.getSwivel() ;
	}

	public int getIndexSize()
	{
		return shape.getIndexSize() ;
	}
	
	public int getVertexSize()
	{
		return shape.getVertexSize() ;
	}

	/**
		Inform the developer whether the Shape 
		has been correctly populated with data.
	*/
	public boolean isComplete()
	{
		return shape.isComplete() ;
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

	public static Shape constructPlane( final Vector3 _length, final MalletColour _colour )
	{
		final Swivel[] swivel = new Swivel[2] ;
		swivel[0] = Swivel.POINT ;
		swivel[1] = Swivel.COLOUR ;

		final Shape plane = new Shape( Shape.Style.FILL, swivel, 6, 4 ) ;
		plane.addVertex( new Object[] { new Vector3(), _colour } ) ;
		plane.addVertex( new Object[] { new Vector3( _length ), _colour } ) ;
		plane.addVertex( new Object[] { new Vector3( 0.0f, _length.y, 0.0f ), _colour } ) ;
		plane.addVertex( new Object[] { new Vector3( _length.x, 0.0f, 0.0f ), _colour } ) ;

		plane.addIndex( 0 ) ;
		plane.addIndex( 2 ) ;
		plane.addIndex( 1 ) ;

		plane.addIndex( 0 ) ;
		plane.addIndex( 1 ) ;
		plane.addIndex( 3 ) ;

		return plane ;
	}

	public static Shape constructCube( final float _width, final Vector2 _minUV, final Vector2 _maxUV )
	{
		final Swivel[] swivel = new Swivel[3] ;
		swivel[0] = Swivel.POINT ;
		swivel[1] = Swivel.COLOUR ;
		swivel[2] = Swivel.UV ;

		final MalletColour white = MalletColour.white() ;

		final Shape plane = new Shape( Shape.Style.FILL, swivel, 38, 24 ) ;
		plane.addVertex( new Object[] { new Vector3( 0.0f, 0.0f, 0.0f ), white, new Vector2( _minUV ) } ) ;					// 0 Front
		plane.addVertex( new Object[] { new Vector3( _width, _width, 0.0f ), white, new Vector2( _maxUV ) } ) ;				// 1
		plane.addVertex( new Object[] { new Vector3( 0.0f, _width, 0.0f ), white, new Vector2( _minUV.x, _maxUV.y ) } ) ;	// 2
		plane.addVertex( new Object[] { new Vector3( _width, 0.0f, 0.0f ), white, new Vector2( _maxUV.x, _minUV.y ) } ) ;	// 3

		plane.addVertex( new Object[] { new Vector3( 0.0f, 0.0f, _width ), white, new Vector2( _minUV ) } ) ;				// 4 Back
		plane.addVertex( new Object[] { new Vector3( _width, _width, _width ), white, new Vector2( _maxUV ) } ) ;			// 5
		plane.addVertex( new Object[] { new Vector3( 0.0f, _width, _width ), white, new Vector2( _minUV.x, _maxUV.y ) } ) ;	// 6
		plane.addVertex( new Object[] { new Vector3( _width, 0.0f, _width ), white, new Vector2( _maxUV.x, _minUV.y ) } ) ;	// 7

		plane.addVertex( new Object[] { new Vector3( 0.0f, _width, 0.0f ), white, new Vector2( _minUV ) } ) ;				// 8 Top
		plane.addVertex( new Object[] { new Vector3( _width, _width, _width ), white, new Vector2( _maxUV ) } ) ;			// 9
		plane.addVertex( new Object[] { new Vector3( 0.0f, _width, _width ), white, new Vector2( _minUV.x, _maxUV.y ) } ) ;	// 10
		plane.addVertex( new Object[] { new Vector3( _width, _width, 0.0f ), white, new Vector2( _maxUV.x, _minUV.y ) } ) ;	// 11

		plane.addVertex( new Object[] { new Vector3( 0.0f, 0.0f, 0.0f ), white, new Vector2( _minUV ) } ) ;					// 12 Bottom
		plane.addVertex( new Object[] { new Vector3( _width, 0.0f, _width ), white, new Vector2( _maxUV ) } ) ;				// 13
		plane.addVertex( new Object[] { new Vector3( 0.0f, 0.0f, _width ), white, new Vector2( _minUV.x, _maxUV.y ) } ) ;	// 14
		plane.addVertex( new Object[] { new Vector3( _width, 0.0f, 0.0f ), white, new Vector2( _maxUV.x, _minUV.y ) } ) ;	// 15

		plane.addVertex( new Object[] { new Vector3( 0.0f, 0.0f, 0.0f ), white, new Vector2( _minUV ) } ) ;					// 16 Left
		plane.addVertex( new Object[] { new Vector3( 0.0f, _width, _width ), white, new Vector2( _maxUV ) } ) ;				// 17
		plane.addVertex( new Object[] { new Vector3( 0.0f, _width, 0.0f ), white, new Vector2( _minUV.x, _maxUV.y ) } ) ;	// 18
		plane.addVertex( new Object[] { new Vector3( 0.0f, 0.0f, _width ), white, new Vector2( _maxUV.x, _minUV.y ) } ) ;	// 19

		plane.addVertex( new Object[] { new Vector3( _width, 0.0f, 0.0f ), white, new Vector2( _minUV ) } ) ;				// 20 Right
		plane.addVertex( new Object[] { new Vector3( _width, _width, _width ), white, new Vector2( _maxUV ) } ) ;			// 21
		plane.addVertex( new Object[] { new Vector3( _width, _width, 0.0f ), white, new Vector2( _minUV.x, _maxUV.y ) } ) ;	// 22
		plane.addVertex( new Object[] { new Vector3( _width, 0.0f, _width ), white, new Vector2( _maxUV.x, _minUV.y ) } ) ;	// 23

		plane.addIndex( 0 ) ;	// Front Face
		plane.addIndex( 2 ) ;
		plane.addIndex( 1 ) ;

		plane.addIndex( 0 ) ;
		plane.addIndex( 1 ) ;
		plane.addIndex( 3 ) ;

		plane.addIndex( 5 ) ;	// Back Face
		plane.addIndex( 6 ) ;
		plane.addIndex( 4 ) ;

		plane.addIndex( 7 ) ;
		plane.addIndex( 5 ) ;
		plane.addIndex( 4 ) ;

		plane.addIndex( 8 ) ;	// Top Face
		plane.addIndex( 10 ) ;
		plane.addIndex( 9 ) ;

		plane.addIndex( 8 ) ;
		plane.addIndex( 9 ) ;
		plane.addIndex( 11 ) ;

		plane.addIndex( 13 ) ;	// Bottom Face
		plane.addIndex( 14 ) ;
		plane.addIndex( 12 ) ;

		plane.addIndex( 15 ) ;
		plane.addIndex( 13 ) ;
		plane.addIndex( 12 ) ;

		plane.addIndex( 17 ) ;	// Left Face
		plane.addIndex( 18 ) ;
		plane.addIndex( 16 ) ;

		plane.addIndex( 19 ) ;
		plane.addIndex( 17 ) ;
		plane.addIndex( 16 ) ;

		plane.addIndex( 20 ) ;	// Right Face
		plane.addIndex( 22 ) ;
		plane.addIndex( 21 ) ;

		plane.addIndex( 20 ) ;
		plane.addIndex( 21 ) ;
		plane.addIndex( 23 ) ;

		return plane ;
	}

	public static Shape updatePlaneGeometry( final Shape _plane, final Vector3 _length )
	{
		//_plane.getPoint( 0, 0 ).setXYZ() ;
		_plane.setVector3( 1, 0, _length ) ;
		_plane.setVector3( 2, 0, 0.0f, _length.y, 0.0f ) ;
		_plane.setVector3( 3, 0, _length.x, 0.0f, 0.0f ) ;

		return _plane ;
	}

	public static Shape updatePlaneUV( final Shape _plane, final Vector2 _minUV, final Vector2 _maxUV )
	{
		_plane.setVector2( 0, 2, _minUV ) ;
		_plane.setVector2( 1, 2, _maxUV ) ;
		_plane.setVector2( 2, 2, _minUV.x, _maxUV.y ) ;
		_plane.setVector2( 3, 2, _maxUV.x, _minUV.y ) ;

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
			totalIndicies += _shapes[i].getIndexSize() ;
			totalPoints += _shapes[i].getVertexSize() ;
		}

		final Shape combined = new Shape( _shapes[0].getStyle(), _shapes[0].getSwivel(), totalIndicies, totalPoints ) ;
		int indexOffset = 0 ;

		for( int i = 0; i < _shapes.length; i++ )
		{
			final Shape shape = _shapes[i] ;

			final Swivel[] swivel = shape.getSwivel() ;
			final int indexSize = shape.getIndexSize() ;
			for( int j = 0; j < indexSize; j++ )
			{
				combined.addIndex( indexOffset + shape.getIndex( j ) ) ;
			}

			final Object[] vertex = Swivel.construct( swivel ) ;
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
		if( _shape.getIndexSize() <= 3 )
		{
			return new Shape( _shape ) ;
		}

		final Swivel[] swivel = _shape.getSwivel() ;
		final Style style = _shape.getStyle() ;

		final List<Integer> tempIndicies = constructTriangulatedIndex( _shape ) ;
		final int indexSize = tempIndicies.size() ;
		final int vertexSize = _shape.getVertexSize() ;

		final Shape triangulated = new Shape( style, swivel, indexSize, vertexSize ) ;
		for( int i = 0; i < indexSize; i++ )
		{
			triangulated.addIndex( tempIndicies.get( i ) ) ;
		}

		final Object[] vertex = Swivel.construct( swivel ) ;
		for( int i = 0; i < vertexSize; i++ )
		{
			_shape.getVertex( vertex, i ) ;
			triangulated.addVertex( vertex ) ;
		}

		return triangulated ;
	}

	/**
		Construct a new index array that triangulates 
		the points stored in _shape.points.
		The point and colour array are not modified.
	*/
	private static List<Integer> constructTriangulatedIndex( final Shape _shape )
	{
		final Swivel[] swivel = _shape.getSwivel() ;
	
		final int indexSize = _shape.getIndexSize() ;
		final List<Integer> indicies = Utility.<Integer>newArrayList( indexSize ) ;
		for( int i = 0; i < indexSize; i++ )
		{
			indicies.add( _shape.getIndex( i ) ) ;
		}

		final List<Integer> newIndicies = Utility.<Integer>newArrayList()  ;
		final int swivelPointIndex = Swivel.getSwivelPointIndex( swivel ) ;
		int size = indicies.size() ;

		final Vector3 previous = new Vector3() ;
		final Vector3 current = new Vector3() ;
		final Vector3 next = new Vector3() ;

		while( size >= 3 )
		{
			for( int i = 1; i < size - 1; i++ )
			{
				final int previousIndex = indicies.get( i - 1 ) ;
				final int currentIndex = indicies.get( i ) ;
				final int nextIndex = indicies.get( i + 1 ) ;

				_shape.getVector3( previousIndex, swivelPointIndex, previous ) ;
				_shape.getVector3( currentIndex, swivelPointIndex, current ) ;
				_shape.getVector3( nextIndex, swivelPointIndex, next ) ;

				if( isInteriorVertex( current, previous, next ) == true &&
					isTriangleEmpty( currentIndex, previousIndex, nextIndex,
									 current, previous, next, indicies, _shape ) == true )
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
		//final float area1 = ( _previous.x * _current.y ) - ( _previous.y * _current.x ) ;
		//final float area2 = ( _next.x * _current.y ) - ( _next.y * _current.x ) ;
		final Vector3 area1 = Vector3.cross( _previous, _current ) ;
		final Vector3 area2 = Vector3.cross( _next, _current ) ;
		return area1.length() >= 0.0f && area2.length() >= 0.0f ;
	}

	private static boolean isTriangleEmpty( final int _currentIndex,
											final int _previousIndex,
											final int _nextIndex,
											final Vector3 _current,
											final Vector3 _previous,
											final Vector3 _next,
											final List<Integer> _indicies,
											final Shape _shape )
	{
		final Swivel[] swivel = _shape.getSwivel() ;

		final Vector3 point = new Vector3() ;
		final int swivelPointIndex = Swivel.getSwivelPointIndex( swivel ) ;
		final int size = _indicies.size() ; 

		for( int i = 0; i < size; i++ )
		{
			final int index = _indicies.get( i ) ;
			if( index != _currentIndex && index != _previousIndex && index != _nextIndex )
			{
				_shape.getVector3( index, swivelPointIndex, point ) ;
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

	public static class DefaultFactory implements Shape.Factory
	{
		public Shape.Interface create()
		{
			return new DefaultShape() ;
		}
	}

	public static class DefaultShape implements Shape.Interface
	{
		private Swivel[] swivel ;
		private float[] verticies ;
		private int[] indicies ;

		private Style style = Style.LINE_STRIP ;

		private int vertexSize = 0 ;
		private int indexIncrement = 0 ;
		private int vertexIncrement = 0 ;

		public DefaultShape() {}

		public void init( final Style _style, final Swivel[] _swivel, final int _indexSize, final int _pointSize )
		{
			swivel    = _swivel ;
			verticies = new float[Swivel.getSwivelFloatSize( _swivel, _swivel.length ) * _pointSize] ;
			indicies  = new int[_indexSize] ;

			style      = _style ;
			vertexSize = _pointSize ;
		}

		public void init( final Shape.Interface _shape )
		{
			final Style s = _shape.getStyle() ;
			final Swivel[] sw = _shape.getSwivel() ;
			final int indexSize = _shape.getIndexSize() ;
			final int vertexSize = _shape.getVertexSize() ;

			init( s, sw, indexSize, vertexSize ) ;

			final Object[] vertex = Swivel.construct( sw ) ;
			for( int i = 0; i < vertexSize; i++ )
			{
				addVertex( _shape.getVertex( vertex, i ) ) ;
			}

			for( int i = 0; i < indexSize; i++ )
			{
				addIndex( _shape.getIndex( i ) ) ;
			}
		}

		public void addIndex( final int _index )
		{
			indicies[indexIncrement++] = _index ;
		}

		public int getIndex( final int _index )
		{
			return indicies[_index] ;
		}

		public void addVertex( final Object[] _vertex )
		{
			for( int i = 0; i < swivel.length; i++ )
			{
				switch( swivel[i] )
				{
					case POINT  :
					case NORMAL :
					{
						final Vector3 point = ( Vector3 )_vertex[i] ;
						verticies[vertexIncrement++] = point.x ;
						verticies[vertexIncrement++] = point.y ;
						verticies[vertexIncrement++] = point.z ;
						break ;
					}
					case COLOUR :
					{
						final MalletColour colour = ( MalletColour )_vertex[i] ;
						verticies[vertexIncrement++] = colour.toFloat() ;
						break ;
					}
					case UV     :
					{
						final Vector2 uv = ( Vector2 )_vertex[i] ;
						verticies[vertexIncrement++] = uv.x ;
						verticies[vertexIncrement++] = uv.y ;
						break ;
					}
				}
			}
		}

		public Object[] getVertex( final Object[] _vertex, final int _index )
		{
			if( _vertex.length != swivel.length )
			{
				return null ;
			}

			int start = _index * Swivel.getSwivelFloatSize( swivel, swivel.length ) ;
			for( int i = 0; i < swivel.length; i++ )
			{
				switch( swivel[i] )
				{
					case POINT  :
					case NORMAL :
					{
						final Vector3 point = ( Vector3 )_vertex[i] ;
						point.x = verticies[start++] ;
						point.y = verticies[start++] ;
						point.z = verticies[start++] ;
						break ;
					}
					case COLOUR :
					{
						final MalletColour colour = ( MalletColour )_vertex[i] ;
						colour.changeColour( verticies[start++] ) ;
						break ;
					}
					case UV     :
					{
						final Vector2 uv = ( Vector2 )_vertex[i] ;
						uv.x = verticies[start++] ;
						uv.y = verticies[start++] ;
						break ;
					}
				}
			}

			return _vertex ;
		}

		public void setVector3( final int _index, final int _swivelIndex, final Vector3 _point )
		{
			setVector3( _index, _swivelIndex, _point.x, _point.y, _point.z ) ;
		}

		public void setVector3( final int _index, final int _swivelIndex, final float _x, final float _y, final float _z )
		{
			final int size = Swivel.getSwivelFloatSize( swivel, swivel.length ) ;
			final int offset = Swivel.getSwivelFloatSize( swivel, _swivelIndex ) ;
			final int start = ( _index * size ) + offset ;

			verticies[start]     = _x ;
			verticies[start + 1] = _y ;
			verticies[start + 2] = _z ;
		}

		public Vector3 getVector3( final int _index, final int _swivelIndex )
		{
			final Vector3 point = new Vector3() ;
			return getVector3( _index, _swivelIndex, point ) ;
		}

		public Vector3 getVector3( final int _index, final int _swivelIndex, final Vector3 _point )
		{
			final int size = Swivel.getSwivelFloatSize( swivel, swivel.length ) ;
			final int offset = Swivel.getSwivelFloatSize( swivel, _swivelIndex ) ;
			final int start = ( _index * size ) + offset ;

			_point.setXYZ( verticies[start], verticies[start + 1], verticies[start + 2] ) ;
			return _point ;
		}

		public void setVector2( final int _index, final int _swivelIndex, final Vector2 _point )
		{
			setVector2( _index, _swivelIndex, _point.x, _point.y ) ;
		}

		public void setVector2( final int _index, final int _swivelIndex, final float _x, final float _y )
		{
			final int size = Swivel.getSwivelFloatSize( swivel, swivel.length ) ;
			final int offset = Swivel.getSwivelFloatSize( swivel, _swivelIndex ) ;
			final int start = ( _index * size ) + offset ;

			verticies[start]       = _x ;
			verticies[start + 1]   = _y ;
		}

		public Vector2 getVector2( final int _index, final int _swivelIndex )
		{
			final Vector2 uv = new Vector2() ;
			return getVector2( _index, _swivelIndex, uv ) ;
		}

		public Vector2 getVector2( final int _index, final int _swivelIndex, final Vector2 _uv )
		{
			final int size = Swivel.getSwivelFloatSize( swivel, swivel.length ) ;
			final int offset = Swivel.getSwivelFloatSize( swivel, _swivelIndex ) ;
			final int start = ( _index * size ) + offset ;

			_uv.setXY( verticies[start], verticies[start + 1] ) ;
			return _uv ;
		}

		public float getFloat( final int _index, final int _swivelIndex )
		{
			final int size = Swivel.getSwivelFloatSize( swivel, swivel.length ) ;
			final int offset = Swivel.getSwivelFloatSize( swivel, _swivelIndex ) ;
			final int start = ( _index * size ) + offset ;

			return verticies[start] ;
		}
		
		public void setColour( final int _index, final int _swivelIndex, final MalletColour _colour )
		{
			final int size = Swivel.getSwivelFloatSize( swivel, swivel.length ) ;
			final int offset = Swivel.getSwivelFloatSize( swivel, _swivelIndex ) ;
			final int start = ( _index * size ) + offset ;

			verticies[start] = _colour.toFloat() ;
		}

		public MalletColour getColour( final int _index, final int _swivelIndex )
		{
			final MalletColour colour = new MalletColour() ;
			return getColour( _index, _swivelIndex, colour ) ;
		}

		public MalletColour getColour( final int _index, final int _swivelIndex, final MalletColour _colour )
		{
			final int size = Swivel.getSwivelFloatSize( swivel, swivel.length ) ;
			final int offset = Swivel.getSwivelFloatSize( swivel, _swivelIndex ) ;
			final int start = ( _index * size ) + offset ;

			_colour.changeColour( verticies[start] ) ;
			return _colour ;
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
			return indexIncrement == indicies.length && vertexIncrement == verticies.length ;
		}
	}

	public static interface Interface
	{
		public void init( final Style _style, final Swivel[] _swivel, final int _indexSize, final int _pointSize ) ;
		public void init( final Shape.Interface _shape ) ;
	
		public void addIndex( final int _index ) ;
		public int getIndex( final int _index ) ;

		public void addVertex( final Object[] _vertex ) ;
		public Object[] getVertex( final Object[] _vertex, final int _index ) ;

		public void setVector3( final int _index, final int _swivelIndex, final Vector3 _point ) ;
		public void setVector3( final int _index, final int _swivelIndex, final float _x, final float _y, final float _z ) ;

		public Vector3 getVector3( final int _index, final int _swivelIndex ) ;
		public Vector3 getVector3( final int _index, final int _swivelIndex, final Vector3 _point ) ;

		public void setVector2( final int _index, final int _swivelIndex, final Vector2 _point ) ;
		public void setVector2( final int _index, final int _swivelIndex, final float _x, final float _y ) ;

		public Vector2 getVector2( final int _index, final int _swivelIndex ) ;
		public Vector2 getVector2( final int _index, final int _swivelIndex, final Vector2 _uv ) ;

		public float getFloat( final int _index, final int _swivelIndex ) ;
		
		public void setColour( final int _index, final int _swivelIndex, final MalletColour _colour ) ;

		public MalletColour getColour( final int _index, final int _swivelIndex ) ;
		public MalletColour getColour( final int _index, final int _swivelIndex, final MalletColour _colour ) ;

		public Shape.Style getStyle() ;

		public Swivel[] getSwivel() ;

		public int getIndexSize() ;

		public int getVertexSize() ;


		public boolean isComplete() ;
	}

	public interface Factory
	{
		public Shape.Interface create() ;
	}
}
