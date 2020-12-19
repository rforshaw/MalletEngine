package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;
import com.linxonline.mallet.util.buffers.IntegerBuffer ;

public class Shape implements IShape
{
	private final Swivel[] swivel ;
	private final int[] swivelOffset ;
	
	private final int swivelFloatSize ;
	private final float[] verticies ;
	private final int[] indicies ;

	private Style style = Style.LINE_STRIP ;

	private int vertexSize = 0 ;
	private int indexIncrement = 0 ;
	private int vertexIncrement = 0 ;

	public Shape( final Style _style, final Swivel[] _swivel, final int _indexSize, final int _pointSize )
	{
		swivel = new Swivel[_swivel.length] ;
		swivelOffset = new int[_swivel.length] ;

		System.arraycopy( _swivel, 0, swivel, 0, _swivel.length ) ;
		for( int i = 0; i < _swivel.length; ++i )
		{
			swivelOffset[i] = Swivel.getSwivelFloatSize( swivel, i ) ;
		}
		swivelFloatSize = Swivel.getSwivelFloatSize( swivel, swivel.length ) ;

		verticies = FloatBuffer.allocate( swivelFloatSize * _pointSize ) ;
		indicies  = IntegerBuffer.allocate( _indexSize ) ;

		style      = _style ;
		vertexSize = _pointSize ;
	}

	public Shape( final Style _style, final int _indexSize, final int _pointSize )
	{
		this( _style, Swivel.constructDefault(), _indexSize, _pointSize ) ;
	}

	public Shape( final Shape _shape )
	{
		this( _shape.getStyle(),
				_shape.getSwivel(),
				_shape.getIndicesSize(),
				_shape.getVerticesSize() ) ;

		// We've got all the information we need to make this a clean copy.
		// This should work even if the implementation was changed 
		// halfway through.
		final Swivel[] sw = _shape.getSwivel() ;
		final int indexSize = _shape.getIndicesSize() ;
		final int vertexSize = _shape.getVerticesSize() ;

		final Object[] vertex = Swivel.createVert( sw ) ;
		for( int i = 0; i < vertexSize; i++ )
		{
			copyVertex( _shape.copyVertexTo( i, vertex ) ) ;
		}

		for( int i = 0; i < indexSize; i++ )
		{
			addIndex( _shape.getIndex( i ) ) ;
		}
	}

	/**
		The index allows the user to refer to the same vertex 
		multiple types without duplicating vertex data.

		_index defines the what vertex should be used next.
		
		Adding more indices than defined by getIndicesSize() 
		will result in undefined behaviour.
	*/
	public void addIndex( final int _index )
	{
		indicies[indexIncrement++] = _index ;
	}

	public void addIndices( final int ... _indicies )
	{
		for( final int index : _indicies )
		{
			indicies[indexIncrement++] = index ;
		}
	}

	public int getIndex( final int _index )
	{
		return indicies[_index] ;
	}

	/**
		Add a vertex defined by the Shapes swivel.

		Undefined errors may arise from using an incorrect 
		vertex that does not align with the shapes swivel.

		Adding more vertices than getVerticesSize() will result in 
		undefined behaviour.
	*/
	@Override
	public void copyVertex( final Object[] _vertex )
	{
		for( int i = 0; i < swivel.length; i++ )
		{
			switch( swivel[i] )
			{
				case POINT  :
				case NORMAL :
				{
					final Vector3 point = ( Vector3 )_vertex[i] ;
					FloatBuffer.set( verticies, vertexIncrement, point ) ;
					vertexIncrement += 3 ;
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
					FloatBuffer.set( verticies, vertexIncrement, uv ) ;
					vertexIncrement += 2 ;
					break ;
				}
			}
		}
	}

	@Override
	public void copyVertices( final Object[] ... _vertices )
	{
		for( final Object[] vertex : _vertices )
		{
			copyVertex( vertex ) ;
		}
	}

	/**
		Copy the Vertex at index location into _vertex.
		Use Swivel.createVert() to build a valid vertex object 
		for the Swivel defined by the shape. 
	*/
	@Override
	public Object[] copyVertexTo( final int _index, final Object[] _vertex )
	{
		if( _vertex.length != swivel.length )
		{
			return null ;
		}

		int start = _index * swivelFloatSize ;
		for( int i = 0; i < swivel.length; i++ )
		{
			switch( swivel[i] )
			{
				case POINT  :
				case NORMAL :
				{
					FloatBuffer.fill( verticies, ( Vector3 )_vertex[i], start ) ;
					start += 3 ;
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
					FloatBuffer.fill( verticies, ( Vector2 )_vertex[i], start ) ;
					start += 2 ;
					break ;
				}
			}
		}

		return _vertex ;
	}

	@Override
	public float[] copyVertexTo( final int _index, final float[] _to )
	{
		int start = _index * swivelFloatSize ;
		System.arraycopy( verticies, start, _to, 0, swivelFloatSize ) ;
		return _to ;
	}

	/**
		_index defines the vertex location.
		_swivelIndex defines the Vector3 location within the vertex.
		_point defines the new values of the Vector3.

		Assumes the caller knows that a Vector3 resides at the location.
	*/
	public void setVector3( final int _index, final int _swivelIndex, final Vector3 _point )
	{
		setVector3( _index, _swivelIndex, _point.x, _point.y, _point.z ) ;
	}

	/**
		_index defines the vertex location.
		_swivelIndex defines the Vector3 location within the vertex.
		_x, _y, and _z defines the new values of the Vector3.

		Assumes the caller knows that a Vector3 resides at the location.
	*/
	public void setVector3( final int _index, final int _swivelIndex, final float _x, final float _y, final float _z )
	{
		final int start = ( _index * swivelFloatSize ) + swivelOffset[_swivelIndex] ;
		FloatBuffer.set( verticies, start, _x, _y, _z ) ;
	}

	/**
		_index defines the vertex location.
		_swivelIndex defines the Vector3 location within the vertex.
		Return a copy of the Vector3.

		Assumes the caller knows that a Vector3 resides at the location.
	*/
	public Vector3 getVector3( final int _index, final int _swivelIndex )
	{
		final Vector3 point = new Vector3() ;
		return getVector3( _index, _swivelIndex, point ) ;
	}

	/**
		_index defines the vertex location.
		_swivelIndex defines the Vector3 location within the vertex.
		Modify _point to reflect the Vector3 at the defined location.

		Assumes the caller knows that a Vector3 resides at the location.
	*/
	public Vector3 getVector3( final int _index, final int _swivelIndex, final Vector3 _point )
	{
		final int start = ( _index * swivelFloatSize ) + swivelOffset[_swivelIndex] ;
		return FloatBuffer.fill( verticies, _point, start ) ;
	}

	/**
		_index defines the vertex location.
		_swivelIndex defines the Vector2 location within the vertex.
		_point defines the new values of the Vector2.

		Assumes the caller knows that a Vector2 resides at the location.
	*/
	public void setVector2( final int _index, final int _swivelIndex, final Vector2 _point )
	{
		setVector2( _index, _swivelIndex, _point.x, _point.y ) ;
	}

	/**
		_index defines the vertex location.
		_swivelIndex defines the Vector2 location within the vertex.
		_x, and _y defines the new values of the Vector2.

		Assumes the caller knows that a Vector2 resides at the location.
	*/
	public void setVector2( final int _index, final int _swivelIndex, final float _x, final float _y )
	{
		final int start = ( _index * swivelFloatSize ) + swivelOffset[_swivelIndex] ;
		FloatBuffer.set( verticies, start, _x, _y ) ;
	}

	/**
		_index defines the vertex location.
		_swivelIndex defines the Vector2 location within the vertex.
		Return a copy of the Vector2.

		Assumes the caller knows that a Vector2 resides at the location.
	*/
	public Vector2 getVector2( final int _index, final int _swivelIndex )
	{
		final Vector2 uv = new Vector2() ;
		return getVector2( _index, _swivelIndex, uv ) ;
	}

	/**
		_index defines the vertex location.
		_swivelIndex defines the Vector3 location within the vertex.
		Modify _point to reflect the Vector3 at the defined location.

		Assumes the caller knows that a Vector3 resides at the location.
	*/
	public Vector2 getVector2( final int _index, final int _swivelIndex, final Vector2 _uv )
	{
		final int start = ( _index * swivelFloatSize ) + swivelOffset[_swivelIndex] ;
		return FloatBuffer.fill( verticies, _uv, start ) ;
	}

	/**
		_index defines the vertex location.
		_swivelIndex defines the float location within the vertex.
		Return the float at the defined location.

		Assumes the caller knows that a float resides there.
	*/
	public float getFloat( final int _index, final int _swivelIndex )
	{
		final int start = ( _index * swivelFloatSize ) + swivelOffset[_swivelIndex] ;
		return verticies[start] ;
	}

	/**
		_index defines the vertex location.
		_swivelIndex defines the MalletCOlour location within the vertex.
		_colour defines the new value of the float.

		Assumes the caller knows that a MalletColour resides at the location.
	*/
	public void setColour( final int _index, final int _swivelIndex, final MalletColour _colour )
	{
		final int start = ( _index * swivelFloatSize ) + swivelOffset[_swivelIndex] ;
		verticies[start] = _colour.toFloat() ;
	}

	/**
		_index defines the vertex location.
		_swivelIndex defines the MalletColour location within the vertex.
		Return a copy of the MalletColour.

		Assumes the caller knows that a MalletColour resides there.
	*/
	public MalletColour getColour( final int _index, final int _swivelIndex )
	{
		final MalletColour colour = new MalletColour() ;
		return getColour( _index, _swivelIndex, colour ) ;
	}

	/**
		_index defines the vertex location.
		_swivelIndex defines the MalletColour location within the vertex.
		Modify _colour to reflect the MalletColour at the defined location.

		Assumes the caller knows that a MalletColour resides at the location.
	*/
	public MalletColour getColour( final int _index, final int _swivelIndex, final MalletColour _colour )
	{
		final int start = ( _index * swivelFloatSize ) + swivelOffset[_swivelIndex] ;
		_colour.changeColour( verticies[start] ) ;
		return _colour ;
	}

	/**
		Returns how the geometry should be interpreted.
		LINES:		Requires a start and an end point to be defined for each line
		LINE_STRIP: Will continue the line from the last point added
		FILL: 		Fill the geometry shape, requires the shape to be defined in polygons, will eventually be auto generated.
	*/
	@Override
	public Shape.Style getStyle()
	{
		return style ;
	}

	/**
		Defines what a Vertex within the Shape is made from.
	*/
	@Override
	public Swivel[] getSwivel()
	{
		return swivel ;
	}

	@Override
	public int getIndicesSize()
	{
		return indicies.length ;
	}

	@Override
	public int getVerticesSize()
	{
		return vertexSize ;
	}

	@Override
	public int[] getRawIndices()
	{
		return indicies ;
	}

	@Override
	public float[] getRawVertices()
	{
		return verticies ;
	}

	/**
		Inform the developer whether the Shape 
		has been correctly populated with data.
	*/
	public boolean isComplete()
	{
		return indexIncrement == indicies.length && vertexIncrement == verticies.length ;
	}

	/**
		Construct a vertex with the default swivel format.
		POINT and COLOUR.
	*/
	public static Object[] construct( final float _x, final float _y, final float _z, final MalletColour _colour )
	{
		final Object[] swivel = new Object[2] ;
		swivel[0] = new Vector3( _x, _y, _z ) ;
		swivel[1] = new MalletColour( _colour ) ;

		return swivel ;
	}

	/**
		Determine whether the Swivel and Vertex structure are the same.
		return true if the vertex order is what is expected by the swivel 
		order, return false if the order is incorrect.
		Because NORMAL and POINT use Vector3 there is a chance that a false 
		positive may be made.
	*/
	public static boolean isCorrectSwivel( final Swivel[] _swivel, final Object[] _object )
	{
		if( _swivel.length != _object.length )
		{
			return false ;
		}

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

		final Vector3 position = new Vector3() ;
		final MalletColour white = MalletColour.white() ;
		final Vector2 uv = new Vector2( _minUV ) ;
		final Object[] vertex = new Object[] { position, white, uv } ; 

		final Shape plane = new Shape( Shape.Style.FILL, swivel, 6, 4 ) ;
		plane.copyVertex( vertex ) ;

		position.setXYZ( _length ) ;
		uv.setXY( _maxUV ) ;
		plane.copyVertex( vertex ) ;

		position.setXYZ( 0.0f, _length.y, _length.z ) ;
		uv.setXY( _minUV.x, _maxUV.y ) ;
		plane.copyVertex( vertex ) ;

		position.setXYZ( _length.x, 0.0f, _length.z ) ;
		uv.setXY( _maxUV.x, _minUV.y ) ;
		plane.copyVertex( vertex ) ;

		plane.addIndex( 0 ) ;
		plane.addIndex( 2 ) ;
		plane.addIndex( 1 ) ;

		plane.addIndex( 0 ) ;
		plane.addIndex( 1 ) ;
		plane.addIndex( 3 ) ;

		return plane ;
	}

	/**
		Construct a basic 2-dimensional quad.
		Se also updatePlaneUV, and updatePlaneGeometry.
	*/
	public static Shape constructPlane( final Vector3 _length, final MalletColour _colour )
	{
		final Swivel[] swivel = new Swivel[2] ;
		swivel[0] = Swivel.POINT ;
		swivel[1] = Swivel.COLOUR ;

		final Shape plane = new Shape( Shape.Style.FILL, swivel, 6, 4 ) ;
		plane.copyVertex( new Object[] { new Vector3(), _colour } ) ;
		plane.copyVertex( new Object[] { new Vector3( _length ), _colour } ) ;
		plane.copyVertex( new Object[] { new Vector3( 0.0f, _length.y, 0.0f ), _colour } ) ;
		plane.copyVertex( new Object[] { new Vector3( _length.x, 0.0f, 0.0f ), _colour } ) ;

		plane.addIndex( 0 ) ;
		plane.addIndex( 2 ) ;
		plane.addIndex( 1 ) ;

		plane.addIndex( 0 ) ;
		plane.addIndex( 1 ) ;
		plane.addIndex( 3 ) ;

		return plane ;
	}

	public static Shape constructOutlinePlane( final Vector3 _length, final MalletColour _colour )
	{
		final Swivel[] swivel = new Swivel[2] ;
		swivel[0] = Swivel.POINT ;
		swivel[1] = Swivel.COLOUR ;

		final Shape plane = new Shape( Shape.Style.LINE_STRIP, swivel, 5, 4 ) ;
		plane.copyVertex( new Object[] { new Vector3(), _colour } ) ;
		plane.copyVertex( new Object[] { new Vector3( _length ), _colour } ) ;
		plane.copyVertex( new Object[] { new Vector3( 0.0f, _length.y, 0.0f ), _colour } ) ;
		plane.copyVertex( new Object[] { new Vector3( _length.x, 0.0f, 0.0f ), _colour } ) ;

		plane.addIndex( 0 ) ;
		plane.addIndex( 2 ) ;
		plane.addIndex( 1 ) ;
		plane.addIndex( 3 ) ;
		plane.addIndex( 0 ) ;

		return plane ;
	}
	
	/**
		Construct a basic 3D cube of dimension _width.
	*/
	public static Shape constructCube( final float _width, final Vector2 _minUV, final Vector2 _maxUV )
	{
		final Swivel[] swivel = new Swivel[3] ;
		swivel[0] = Swivel.POINT ;
		swivel[1] = Swivel.COLOUR ;
		swivel[2] = Swivel.UV ;

		final MalletColour white = MalletColour.white() ;

		final Shape plane = new Shape( Shape.Style.FILL, swivel, 36, 24 ) ;
		plane.copyVertex( Swivel.createVert( new Vector3( 0.0f, 0.0f, 0.0f ), white, new Vector2( _minUV ) ) ) ;					// 0 Front
		plane.copyVertex( Swivel.createVert( new Vector3( _width, _width, 0.0f ), white, new Vector2( _maxUV ) ) ) ;				// 1
		plane.copyVertex( Swivel.createVert( new Vector3( 0.0f, _width, 0.0f ), white, new Vector2( _minUV.x, _maxUV.y ) ) ) ;	// 2
		plane.copyVertex( Swivel.createVert( new Vector3( _width, 0.0f, 0.0f ), white, new Vector2( _maxUV.x, _minUV.y ) ) ) ;	// 3

		plane.copyVertex( Swivel.createVert( new Vector3( 0.0f, 0.0f, _width ), white, new Vector2( _minUV ) ) ) ;				// 4 Back
		plane.copyVertex( Swivel.createVert( new Vector3( _width, _width, _width ), white, new Vector2( _maxUV ) ) ) ;			// 5
		plane.copyVertex( Swivel.createVert( new Vector3( 0.0f, _width, _width ), white, new Vector2( _minUV.x, _maxUV.y ) ) ) ;	// 6
		plane.copyVertex( Swivel.createVert( new Vector3( _width, 0.0f, _width ), white, new Vector2( _maxUV.x, _minUV.y ) ) ) ;	// 7

		plane.copyVertex( Swivel.createVert( new Vector3( 0.0f, _width, 0.0f ), white, new Vector2( _minUV ) ) ) ;				// 8 Top
		plane.copyVertex( Swivel.createVert( new Vector3( _width, _width, _width ), white, new Vector2( _maxUV ) ) ) ;			// 9
		plane.copyVertex( Swivel.createVert( new Vector3( 0.0f, _width, _width ), white, new Vector2( _minUV.x, _maxUV.y ) ) ) ;	// 10
		plane.copyVertex( Swivel.createVert( new Vector3( _width, _width, 0.0f ), white, new Vector2( _maxUV.x, _minUV.y ) ) ) ;	// 11

		plane.copyVertex( Swivel.createVert( new Vector3( 0.0f, 0.0f, 0.0f ), white, new Vector2( _minUV ) ) ) ;					// 12 Bottom
		plane.copyVertex( Swivel.createVert( new Vector3( _width, 0.0f, _width ), white, new Vector2( _maxUV ) ) ) ;				// 13
		plane.copyVertex( Swivel.createVert( new Vector3( 0.0f, 0.0f, _width ), white, new Vector2( _minUV.x, _maxUV.y ) ) ) ;	// 14
		plane.copyVertex( Swivel.createVert( new Vector3( _width, 0.0f, 0.0f ), white, new Vector2( _maxUV.x, _minUV.y ) ) ) ;	// 15

		plane.copyVertex( Swivel.createVert( new Vector3( 0.0f, 0.0f, 0.0f ), white, new Vector2( _minUV ) ) ) ;					// 16 Left
		plane.copyVertex( Swivel.createVert( new Vector3( 0.0f, _width, _width ), white, new Vector2( _maxUV ) ) ) ;				// 17
		plane.copyVertex( Swivel.createVert( new Vector3( 0.0f, _width, 0.0f ), white, new Vector2( _minUV.x, _maxUV.y ) ) ) ;	// 18
		plane.copyVertex( Swivel.createVert( new Vector3( 0.0f, 0.0f, _width ), white, new Vector2( _maxUV.x, _minUV.y ) ) ) ;	// 19

		plane.copyVertex( Swivel.createVert( new Vector3( _width, 0.0f, 0.0f ), white, new Vector2( _minUV ) ) ) ;				// 20 Right
		plane.copyVertex( Swivel.createVert( new Vector3( _width, _width, _width ), white, new Vector2( _maxUV ) ) ) ;			// 21
		plane.copyVertex( Swivel.createVert( new Vector3( _width, _width, 0.0f ), white, new Vector2( _minUV.x, _maxUV.y ) ) ) ;	// 22
		plane.copyVertex( Swivel.createVert( new Vector3( _width, 0.0f, _width ), white, new Vector2( _maxUV.x, _minUV.y ) ) ) ;	// 23

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

	/**
		Update the geometry of a 2-dimensional quad.
		Length X defines the quads width.
		Length Y defines the quads height.
	*/
	public static Shape updatePlaneGeometry( final Shape _plane, final Vector3 _length )
	{
		return updatePlaneGeometry( _plane, _length.x, _length.y, _length.z ) ;
	}

	/**
		Update the geometry of a 2-dimensional quad.
		Length X defines the quads width.
		Length Y defines the quads height.
	*/
	public static Shape updatePlaneGeometry( final Shape _plane, final float _x, final float _y, final float _z )
	{
		//_plane.getPoint( 0, 0 ).setXYZ() ;
		_plane.setVector3( 1, 0, _x, _y, _z ) ;
		_plane.setVector3( 2, 0, 0.0f, _y, 0.0f ) ;
		_plane.setVector3( 3, 0, _x, 0.0f, 0.0f ) ;

		return _plane ;
	}

	/**
		Update the UV co-ordinates of a 2-dimensional quad.
	*/
	public static Shape updatePlaneUV( final Shape _plane, final Vector2 _minUV, final Vector2 _maxUV )
	{
		_plane.setVector2( 0, 2, _minUV ) ;
		_plane.setVector2( 1, 2, _maxUV ) ;
		_plane.setVector2( 2, 2, _minUV.x, _maxUV.y ) ;
		_plane.setVector2( 3, 2, _maxUV.x, _minUV.y ) ;

		return _plane ;
	}

	public static Shape updatePlaneUV( final Shape _plane, final float[] _uv )
	{
		_plane.setVector2( 0, 2, _uv[0], _uv[1] ) ;
		_plane.setVector2( 1, 2, _uv[2], _uv[3] ) ;
		_plane.setVector2( 2, 2, _uv[0], _uv[3] ) ;
		_plane.setVector2( 3, 2, _uv[2], _uv[1] ) ;

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
			totalIndicies += _shapes[i].getIndicesSize() ;
			totalPoints += _shapes[i].getVerticesSize() ;
		}

		final Shape combined = new Shape( _shapes[0].getStyle(), _shapes[0].getSwivel(), totalIndicies, totalPoints ) ;
		int indexOffset = 0 ;

		for( int i = 0; i < _shapes.length; i++ )
		{
			final Shape shape = _shapes[i] ;

			final Swivel[] swivel = shape.getSwivel() ;
			final int indexSize = shape.getIndicesSize() ;
			for( int j = 0; j < indexSize; j++ )
			{
				combined.addIndex( indexOffset + shape.getIndex( j ) ) ;
			}

			final Object[] vertex = Swivel.createVert( swivel ) ;
			final int size = shape.getVerticesSize() ;

			for( int j = 0; j < size; j++ )
			{
				combined.copyVertex( shape.copyVertexTo( j, vertex ) ) ;
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
		if( _shape.getIndicesSize() <= 3 )
		{
			return new Shape( _shape ) ;
		}

		final Swivel[] swivel = _shape.getSwivel() ;
		final Style style = _shape.getStyle() ;

		final List<Integer> tempIndicies = constructTriangulatedIndex( _shape ) ;
		final int indexSize = tempIndicies.size() ;
		final int vertexSize = _shape.getVerticesSize() ;

		final Shape triangulated = new Shape( style, swivel, indexSize, vertexSize ) ;
		for( int i = 0; i < indexSize; i++ )
		{
			triangulated.addIndex( tempIndicies.get( i ) ) ;
		}

		final Object[] vertex = Swivel.createVert( swivel ) ;
		for( int i = 0; i < vertexSize; i++ )
		{
			triangulated.copyVertex( _shape.copyVertexTo( i, vertex ) ) ;
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
	
		final int indexSize = _shape.getIndicesSize() ;
		final List<Integer> indicies = MalletList.<Integer>newList( indexSize ) ;
		for( int i = 0; i < indexSize; i++ )
		{
			indicies.add( _shape.getIndex( i ) ) ;
		}

		final List<Integer> newIndicies = MalletList.<Integer>newList()  ;
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
}
