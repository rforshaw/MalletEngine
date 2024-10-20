package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;
import com.linxonline.mallet.util.buffers.IntegerBuffer ;

public final class Shape implements IShape
{
	private final Attribute[] swivel ;
	private final int[] swivelOffset ;
	
	private final int swivelFloatSize ;
	private final float[] verticies ;
	private final int[] indicies ;

	private final Style style ;

	private final int vertexSize ;
	private int indexIncrement = 0 ;
	private int vertexIncrement = 0 ;

	public Shape( final Style _style, final Attribute[] _swivel, final int _indexSize, final int _pointSize )
	{
		swivel = new Attribute[_swivel.length] ;
		swivelOffset = new int[_swivel.length] ;

		System.arraycopy( _swivel, 0, swivel, 0, _swivel.length ) ;
		for( int i = 0; i < _swivel.length; ++i )
		{
			swivelOffset[i] = Attribute.getAttributeFloatSize( swivel, i ) ;
		}
		swivelFloatSize = Attribute.getAttributeFloatSize( swivel, swivel.length ) ;

		verticies = FloatBuffer.allocate( swivelFloatSize * _pointSize ) ;
		indicies  = IntegerBuffer.allocate( _indexSize ) ;

		style      = _style ;
		vertexSize = _pointSize ;
	}

	public Shape( final Style _style, final int _indexSize, final int _pointSize )
	{
		this( _style, Attribute.constructDefault(), _indexSize, _pointSize ) ;
	}

	public Shape( final Shape _shape )
	{
		this( _shape.getStyle(),
				_shape.getAttribute(),
				_shape.getIndicesSize(),
				_shape.getVerticesSize() ) ;

		System.arraycopy( _shape.indicies, 0, indicies, 0, _shape.indicies.length ) ;
		System.arraycopy( _shape.verticies, 0, verticies, 0, _shape.verticies.length ) ;
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
	public void copyVertex( final Object[] _vertex )
	{
		for( int i = 0; i < swivel.length; i++ )
		{
			switch( swivel[i] )
			{
				case VEC3  :
				{
					final Vector3 point = ( Vector3 )_vertex[i] ;
					FloatBuffer.set( verticies, vertexIncrement, point ) ;
					vertexIncrement += 3 ;
					break ;
				}
				case FLOAT :
				{
					final MalletColour colour = ( MalletColour )_vertex[i] ;
					verticies[vertexIncrement++] = colour.toFloat() ;
					break ;
				}
				case VEC2     :
				{
					final Vector2 uv = ( Vector2 )_vertex[i] ;
					FloatBuffer.set( verticies, vertexIncrement, uv ) ;
					vertexIncrement += 2 ;
					break ;
				}
			}
		}
	}

	public void copyVertices( final Object[] ... _vertices )
	{
		for( final Object[] vertex : _vertices )
		{
			copyVertex( vertex ) ;
		}
	}

	/**
		Copy the Vertex at index location into _vertex.
		Use Attribute.createVert() to build a valid vertex object 
		for the Attribute defined by the shape. 
	*/
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
				case VEC3  :
				{
					FloatBuffer.fill( verticies, ( Vector3 )_vertex[i], start ) ;
					start += 3 ;
					break ;
				}
				case FLOAT :
				{
					final MalletColour colour = ( MalletColour )_vertex[i] ;
					colour.changeColour( verticies[start++] ) ;
					break ;
				}
				case VEC2     :
				{
					FloatBuffer.fill( verticies, ( Vector2 )_vertex[i], start ) ;
					start += 2 ;
					break ;
				}
			}
		}

		return _vertex ;
	}

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
		Should not be modified by external users.
	*/
	@Override
	public Attribute[] getAttribute()
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
	public IShape.IIndexWrite writeIndices( final int _indexOffset, final IShape.IIndexWrite _write )
	{
		for( int i = 0; i < indicies.length; ++i )
		{
			_write.put( _indexOffset + indicies[i] ) ;
		}
		return _write ;
	}

	@Override
	public IShape.IVertWrite writeVertices( final IShape.IVertWrite _write )
	{
		_write.put( verticies ) ;
		return _write ;
	}

	public int[] getRawIndices()
	{
		return indicies ;
	}
	
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
		VEC3 and FLOAT.
	*/
	public static Object[] construct( final float _x, final float _y, final float _z, final MalletColour _colour )
	{
		final Object[] swivel = new Object[2] ;
		swivel[0] = new Vector3( _x, _y, _z ) ;
		swivel[1] = new MalletColour( _colour ) ;

		return swivel ;
	}

	/**
		Determine whether the Attribute and Vertex structure are the same.
		return true if the vertex order is what is expected by the swivel 
		order, return false if the order is incorrect.
		positive may be made.
	*/
	public static boolean isCorrectAttribute( final Attribute[] _swivel, final Object[] _object )
	{
		if( _swivel.length != _object.length )
		{
			return false ;
		}

		for( int i = 0; i < _swivel.length; i++ )
		{
			switch( _swivel[i] )
			{
				case VEC3  :
				{
					if( ( _object[i] instanceof Vector3 ) == false )
					{
						return false ;
					}
					break ;
				}
				case FLOAT :
				{
					if( ( _object[i] instanceof MalletColour ) == false )
					{
						return false ;
					}
					break ;
				}
				case VEC2     :
				{
					if( ( _object[i] instanceof Vector2 ) == false )
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
		final Attribute[] swivel = new Attribute[3] ;
		swivel[0] = Attribute.VEC3 ;
		swivel[1] = Attribute.FLOAT ;
		swivel[2] = Attribute.VEC2 ;

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
		final Attribute[] swivel = new Attribute[2] ;
		swivel[0] = Attribute.VEC3 ;
		swivel[1] = Attribute.FLOAT ;

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
		final Attribute[] swivel = new Attribute[2] ;
		swivel[0] = Attribute.VEC3 ;
		swivel[1] = Attribute.FLOAT ;

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

	public static Shape constructOutlineCircle( final float _radius, final int _segments, final MalletColour _colour )
	{
		final Attribute[] swivel = new Attribute[2] ;
		swivel[0] = Attribute.VEC3 ;
		swivel[1] = Attribute.FLOAT ;

		final Shape circle = new Shape( Shape.Style.LINE_STRIP, swivel, _segments + 1, _segments ) ;
		for( int i = 0; i < _segments; ++i )
		{
			final float theta = 2.0f * ( float )Math.PI * ( float )i / ( float )_segments ;

			final float x = _radius * ( float )Math.cos( theta ) ;
			final float y = _radius * ( float )Math.sin( theta ) ;

			circle.addIndex( i ) ;
			circle.copyVertex( new Object[] { new Vector3( x, y, 0.0f ), _colour } ) ;
		}

		circle.addIndex( 0 ) ;
		return circle ;
	}

	/**
		Construct a basic 3D cube of dimension _width.
	*/
	public static Shape constructCube( final float _width, final Vector2 _minUV, final Vector2 _maxUV )
	{
		final Attribute[] swivel = new Attribute[3] ;
		swivel[0] = Attribute.VEC3 ;
		swivel[1] = Attribute.FLOAT ;
		swivel[2] = Attribute.VEC2 ;

		final MalletColour white = MalletColour.white() ;

		final Shape plane = new Shape( Shape.Style.FILL, swivel, 36, 24 ) ;
		plane.copyVertex( Attribute.createVert( new Vector3( 0.0f, 0.0f, 0.0f ), white, new Vector2( _minUV ) ) ) ;					// 0 Front
		plane.copyVertex( Attribute.createVert( new Vector3( _width, _width, 0.0f ), white, new Vector2( _maxUV ) ) ) ;				// 1
		plane.copyVertex( Attribute.createVert( new Vector3( 0.0f, _width, 0.0f ), white, new Vector2( _minUV.x, _maxUV.y ) ) ) ;	// 2
		plane.copyVertex( Attribute.createVert( new Vector3( _width, 0.0f, 0.0f ), white, new Vector2( _maxUV.x, _minUV.y ) ) ) ;	// 3

		plane.copyVertex( Attribute.createVert( new Vector3( 0.0f, 0.0f, _width ), white, new Vector2( _minUV ) ) ) ;				// 4 Back
		plane.copyVertex( Attribute.createVert( new Vector3( _width, _width, _width ), white, new Vector2( _maxUV ) ) ) ;			// 5
		plane.copyVertex( Attribute.createVert( new Vector3( 0.0f, _width, _width ), white, new Vector2( _minUV.x, _maxUV.y ) ) ) ;	// 6
		plane.copyVertex( Attribute.createVert( new Vector3( _width, 0.0f, _width ), white, new Vector2( _maxUV.x, _minUV.y ) ) ) ;	// 7

		plane.copyVertex( Attribute.createVert( new Vector3( 0.0f, _width, 0.0f ), white, new Vector2( _minUV ) ) ) ;				// 8 Top
		plane.copyVertex( Attribute.createVert( new Vector3( _width, _width, _width ), white, new Vector2( _maxUV ) ) ) ;			// 9
		plane.copyVertex( Attribute.createVert( new Vector3( 0.0f, _width, _width ), white, new Vector2( _minUV.x, _maxUV.y ) ) ) ;	// 10
		plane.copyVertex( Attribute.createVert( new Vector3( _width, _width, 0.0f ), white, new Vector2( _maxUV.x, _minUV.y ) ) ) ;	// 11

		plane.copyVertex( Attribute.createVert( new Vector3( 0.0f, 0.0f, 0.0f ), white, new Vector2( _minUV ) ) ) ;					// 12 Bottom
		plane.copyVertex( Attribute.createVert( new Vector3( _width, 0.0f, _width ), white, new Vector2( _maxUV ) ) ) ;				// 13
		plane.copyVertex( Attribute.createVert( new Vector3( 0.0f, 0.0f, _width ), white, new Vector2( _minUV.x, _maxUV.y ) ) ) ;	// 14
		plane.copyVertex( Attribute.createVert( new Vector3( _width, 0.0f, 0.0f ), white, new Vector2( _maxUV.x, _minUV.y ) ) ) ;	// 15

		plane.copyVertex( Attribute.createVert( new Vector3( 0.0f, 0.0f, 0.0f ), white, new Vector2( _minUV ) ) ) ;					// 16 Left
		plane.copyVertex( Attribute.createVert( new Vector3( 0.0f, _width, _width ), white, new Vector2( _maxUV ) ) ) ;				// 17
		plane.copyVertex( Attribute.createVert( new Vector3( 0.0f, _width, 0.0f ), white, new Vector2( _minUV.x, _maxUV.y ) ) ) ;	// 18
		plane.copyVertex( Attribute.createVert( new Vector3( 0.0f, 0.0f, _width ), white, new Vector2( _maxUV.x, _minUV.y ) ) ) ;	// 19

		plane.copyVertex( Attribute.createVert( new Vector3( _width, 0.0f, 0.0f ), white, new Vector2( _minUV ) ) ) ;				// 20 Right
		plane.copyVertex( Attribute.createVert( new Vector3( _width, _width, _width ), white, new Vector2( _maxUV ) ) ) ;			// 21
		plane.copyVertex( Attribute.createVert( new Vector3( _width, _width, 0.0f ), white, new Vector2( _minUV.x, _maxUV.y ) ) ) ;	// 22
		plane.copyVertex( Attribute.createVert( new Vector3( _width, 0.0f, _width ), white, new Vector2( _maxUV.x, _minUV.y ) ) ) ;	// 23

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

	public static Shape updateCircle( final Shape _circle, final float _radius )
	{
		final int segments = _circle.getVerticesSize() ;
		for( int i = 0; i < segments; ++i )
		{
			final float theta = 2.0f * ( float )Math.PI * ( float )i / ( float )segments ;

			final float x = _radius * ( float )Math.cos( theta ) ;
			final float y = _radius * ( float )Math.sin( theta ) ;

			_circle.setVector3( i, 0, x, y, 0.0f ) ;
		}

		return _circle ;
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
}
