package com.linxonline.mallet.renderer.web ;

import org.teavm.jso.typedarrays.ArrayBuffer ;
import org.teavm.jso.typedarrays.Int16Array ;
import org.teavm.jso.typedarrays.Float32Array ;
import org.teavm.jso.typedarrays.Uint8Array ;

import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.maths.* ;

/**
	Using DefaultShape returned unpredictable results when 
	uploading to a VBO.

	To mitigate this issue and improve performance we ensure 
	that the Shape structure is already in a viable format 
	for uploading. 
*/
public class WebShape extends Shape
{
	private Shape.Style style ;
	private Shape.Swivel[] swivel ;
	private Int16Array indicies ;
	private Float32Array verticies ;
	private Uint8Array facadeVerticies ;

	private int vertexSize = 0 ;
	private int indexIncrement = 0 ;
	private int vertexIncrement = 0 ;

	public WebShape( final Shape.Style _style, final Shape.Swivel[] _swivel, final int _indexSize, final int _pointSize )
	{
		swivel = _swivel ;

		indicies = Int16Array.create( _indexSize ) ;
		verticies = Float32Array.create( Shape.Swivel.getSwivelFloatSize( _swivel, _swivel.length ) * _pointSize ) ;
		facadeVerticies = Uint8Array.create( verticies.getBuffer() ) ;

		style = _style ;
		vertexSize = _pointSize ;
	}

	public WebShape( final Shape _shape )
	{
		this( _shape.getStyle(),
			  _shape.getSwivel(),
			  _shape.getIndexSize(),
			  _shape.getVertexSize() ) ;

		// We've got all the information we need to make this a clean copy.
		// This should work even if the implementation was changed 
		// halfway through.
		final Swivel[] sw = _shape.getSwivel() ;
		final int indexSize = _shape.getIndexSize() ;
		final int vertexSize = _shape.getVertexSize() ;

		final Object[] vertex = Shape.Swivel.constructSwivel( sw ) ;
		for( int i = 0; i < vertexSize; i++ )
		{
			addVertex( _shape.getVertex( vertex, i ) ) ;
		}

		for( int i = 0; i < indexSize; i++ )
		{
			addIndex( _shape.getIndex( i ) ) ;
		}
	}

	@Override
	public void addIndex( final int _index )
	{
		indicies.set( indexIncrement++, ( short )_index ) ;
	}

	@Override
	public int getIndex( final int _index )
	{
		return indicies.get( _index ) ;
	}

	@Override
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
					verticies.set( vertexIncrement++, point.x ) ;
					verticies.set( vertexIncrement++, point.y ) ;
					verticies.set( vertexIncrement++, point.z ) ;
					break ;
				}
				case COLOUR :
				{
					final MalletColour colour = ( MalletColour )_vertex[i] ;
					int byteIndexIncrement = vertexIncrement++ * 4 ;
					facadeVerticies.set( byteIndexIncrement++, ( short )colour.colours[MalletColour.RED] ) ;
					facadeVerticies.set( byteIndexIncrement++, ( short )colour.colours[MalletColour.GREEN] ) ;
					facadeVerticies.set( byteIndexIncrement++, ( short )colour.colours[MalletColour.BLUE] ) ;
					facadeVerticies.set( byteIndexIncrement++, ( short )colour.colours[MalletColour.ALPHA] ) ;
					break ;
				}
				case UV     :
				{
					final Vector2 uv = ( Vector2 )_vertex[i] ;
					verticies.set( vertexIncrement++, uv.x ) ;
					verticies.set( vertexIncrement++, uv.y ) ;
					break ;
				}
			}
		}
	}

	@Override
	public Object[] getVertex( final Object[] _vertex, final int _index )
	{
		if( _vertex.length != swivel.length )
		{
			return null ;
		}

		int start = _index * Shape.Swivel.getSwivelFloatSize( swivel, swivel.length ) ;
		for( int i = 0; i < swivel.length; i++ )
		{
			switch( swivel[i] )
			{
				case POINT  :
				case NORMAL :
				{
					final Vector3 point = ( Vector3 )_vertex[i] ;
					point.x = verticies.get( start++ ) ;
					point.y = verticies.get( start++ ) ;
					point.z = verticies.get( start++ ) ;
					break ;
				}
				case COLOUR :
				{
					final MalletColour colour = ( MalletColour )_vertex[i] ;
					int byteIndexIncrement = start++ * 4 ;
					colour.changeColour( ( byte )facadeVerticies.get( byteIndexIncrement++ ),
										 ( byte )facadeVerticies.get( byteIndexIncrement++ ),
										 ( byte )facadeVerticies.get( byteIndexIncrement++ ),
										 ( byte )facadeVerticies.get( byteIndexIncrement++ ) ) ;
					break ;
				}
				case UV     :
				{
					final Vector2 uv = ( Vector2 )_vertex[i] ;
					uv.x = verticies.get( start++ ) ;
					uv.y = verticies.get( start++ ) ;
					break ;
				}
			}
		}

		return _vertex ;
	}

	@Override
	public void setVector3( final int _index, final int _swivelIndex, final Vector3 _point )
	{
		setVector3( _index, _swivelIndex, _point.x, _point.y, _point.z ) ;
	}

	@Override
	public void setVector3( final int _index, final int _swivelIndex, final float _x, final float _y, final float _z )
	{
		final int size = Shape.Swivel.getSwivelFloatSize( swivel, swivel.length ) ;
		final int offset = Shape.Swivel.getSwivelFloatSize( swivel, _swivelIndex ) ;
		final int start = ( _index * size ) + offset ;

		verticies.set( start,     _x ) ;
		verticies.set( start + 1, _y ) ;
		verticies.set( start + 2, _z ) ;
	}

	@Override
	public Vector3 getVector3( final int _index, final int _swivelIndex )
	{
		final Vector3 point = new Vector3() ;
		return getVector3( _index, _swivelIndex, point ) ;
	}

	@Override
	public Vector3 getVector3( final int _index, final int _swivelIndex, final Vector3 _point )
	{
		final int size = Shape.Swivel.getSwivelFloatSize( swivel, swivel.length ) ;
		final int offset = Shape.Swivel.getSwivelFloatSize( swivel, _swivelIndex ) ;
		final int start = ( _index * size ) + offset ;

		_point.setXYZ( verticies.get( start ), verticies.get( start + 1 ), verticies.get( start + 2 ) ) ;
		return _point ;
	}

	@Override
	public void setVector2( final int _index, final int _swivelIndex, final Vector2 _point )
	{
		setVector2( _index, _swivelIndex, _point.x, _point.y ) ;
	}

	@Override
	public void setVector2( final int _index, final int _swivelIndex, final float _x, final float _y )
	{
		final int size = Shape.Swivel.getSwivelFloatSize( swivel, swivel.length ) ;
		final int offset = Shape.Swivel.getSwivelFloatSize( swivel, _swivelIndex ) ;
		final int start = ( _index * size ) + offset ;

		verticies.set( start, _x ) ;
		verticies.set( start + 1, _y ) ;
	}

	@Override
	public Vector2 getVector2( final int _index, final int _swivelIndex )
	{
		final Vector2 uv = new Vector2() ;
		return getVector2( _index, _swivelIndex, uv ) ;
	}

	@Override
	public Vector2 getVector2( final int _index, final int _swivelIndex, final Vector2 _uv )
	{
		final int size = Shape.Swivel.getSwivelFloatSize( swivel, swivel.length ) ;
		final int offset = Shape.Swivel.getSwivelFloatSize( swivel, _swivelIndex ) ;
		final int start = ( _index * size ) + offset ;

		_uv.setXY( verticies.get( start ), verticies.get( start + 1 ) ) ;
		return _uv ;
	}

	@Override
	public float getFloat( final int _index, final int _swivelIndex )
	{
		final int size = Shape.Swivel.getSwivelFloatSize( swivel, swivel.length ) ;
		final int offset = Shape.Swivel.getSwivelFloatSize( swivel, _swivelIndex ) ;
		final int start = ( _index * size ) + offset ;

		return verticies.get( start ) ;
	}

	@Override
	public void setColour( final int _index, final int _swivelIndex, final MalletColour _colour )
	{
		final int size = Shape.Swivel.getSwivelFloatSize( swivel, swivel.length ) ;
		final int offset = Shape.Swivel.getSwivelFloatSize( swivel, _swivelIndex ) ;
		final int start = ( ( _index * size ) + offset ) * 4 ;

		facadeVerticies.set( start,     _colour.colours[MalletColour.RED] ) ;
		facadeVerticies.set( start + 1, _colour.colours[MalletColour.GREEN] ) ;
		facadeVerticies.set( start + 2, _colour.colours[MalletColour.BLUE] ) ;
		facadeVerticies.set( start + 3, _colour.colours[MalletColour.ALPHA] ) ;
	}

	@Override
	public MalletColour getColour( final int _index, final int _swivelIndex )
	{
		final MalletColour colour = new MalletColour() ;
		return getColour( _index, _swivelIndex, colour ) ;
	}

	@Override
	public MalletColour getColour( final int _index, final int _swivelIndex, final MalletColour _colour )
	{
		final int size = Shape.Swivel.getSwivelFloatSize( swivel, swivel.length ) ;
		final int offset = Shape.Swivel.getSwivelFloatSize( swivel, _swivelIndex ) ;
		final int start = ( ( _index * size ) + offset ) * 4 ;

		_colour.changeColour( ( byte )facadeVerticies.get( start ),
							  ( byte )facadeVerticies.get( start + 1 ),
							  ( byte )facadeVerticies.get( start + 2 ),
							  ( byte )facadeVerticies.get( start + 3 ) ) ;
		return _colour ;
	}

	@Override
	public Shape.Style getStyle()
	{
		return style ;
	}

	@Override
	public Shape.Swivel[] getSwivel()
	{
		return swivel ;
	}

	@Override
	public int getIndexSize()
	{
		return indicies.getLength() ;
	}

	@Override
	public int getVertexSize()
	{
		return vertexSize ;
	}

	@Override
	public boolean isComplete()
	{
		return indexIncrement == getIndexSize() && vertexIncrement == getVertexSize() ;
	}

	public static class Factory implements Shape.Factory
	{
		@Override
		public Shape create( final Style _style, final Swivel[] _swivel, final int _indexSize, final int _pointSize )
		{
			System.out.println( "Create Web Shape" ) ;
			return new WebShape( _style, _swivel, _indexSize, _pointSize ) ;
		}

		@Override
		public Shape create( final Shape _shape )
		{
			System.out.println( "Create Web Shape" ) ;
			return new WebShape( _shape ) ;
		}
	}
}
