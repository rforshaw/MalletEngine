package com.linxonline.mallet.util.buffers.android ;

import com.linxonline.mallet.util.buffers.IFloatBuffer ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class FloatBuffer implements IFloatBuffer
{
	private float[] array ;

	public FloatBuffer( final int _size )
	{
		array = new float[_size] ;
	}

	public FloatBuffer( final IFloatBuffer _buffer, final int _size )
	{
		final int length = _buffer.size() + _size ;
		array = new float[length] ;

		for( int i = 0; i < _buffer.size(); i++ )
		{
			array[i] = _buffer.get( i ) ;
		}
	}

	@Override
	public IFloatBuffer allocate( final int _size )
	{
		return new FloatBuffer( _size ) ;
	}

	@Override
	public IFloatBuffer expand( final IFloatBuffer _buffer, final int _size )
	{
		return new FloatBuffer( _buffer, _size ) ;
	}

	@Override
	public int size()
	{
		return array.length ;
	}

	@Override
	public float set( final int _index, final float _val )
	{
		return array[_index] = _val ;
	}

	@Override
	public Vector2 set( final int _index, final Vector2 _val )
	{
		array[_index + 0] = _val.x ;
		array[_index + 1] = _val.y ;
		return _val ;
	}

	@Override
	public Vector3 set( final int _index, final Vector3 _val )
	{
		array[_index + 0] = _val.x ;
		array[_index + 1] = _val.y ;
		array[_index + 2] = _val.z ;
		return _val ;
	}

	@Override
	public void set( final int _index, final float _x, final float _y )
	{
		array[_index + 0] = _x ;
		array[_index + 1] = _y ;
	}

	@Override
	public void set( final int _index, final float _x, final float _y, final float _z )
	{
		array[_index + 0] = _x ;
		array[_index + 1] = _y ;
		array[_index + 2] = _z ;
	}

	@Override
	public float get( final int _index )
	{
		return array[_index] ;
	}

	@Override
	public Vector3 fill( final Vector3 _fill, final int _at )
	{
		_fill.setXYZ( array[_at], array[_at + 1], array[_at + 2] ) ;
		return _fill ;
	}

	@Override
	public Vector2 fill( final Vector2 _fill, final int _at )
	{
		_fill.setXY( array[_at], array[_at + 1] ) ;
		return _fill ;
	}

	@Override
	public void swap( final int _lhs, final int _rhs )
	{
		final float t = array[_lhs] ;
		array[_lhs] = array[_rhs] ;
		array[_rhs] = t ;
	}

	@Override
	public float multiply( final int _lhs, final float _val )
	{
		return array[_lhs] * _val ;
	}

	@Override
	public float multiply( final int _lhs, final int _rhs )
	{
		return array[_lhs] * array[_rhs] ;
	}

	@Override
	public float multiply( final int _lhs, final int _mhs, final int _rhs )
	{
		return array[_lhs] * array[_mhs] * array[_rhs] ;
	}

	@Override
	public float multiply( final int _lhs, final IFloatBuffer _buffer, final int _rhs )
	{
		return array[_lhs] * _buffer.get( _rhs ) ;
	}

	@Override
	public void divide( final int _lhs, final float _val )
	{
		array[_lhs] /= _val ;
	}

	public float[] getArray()
	{
		return array ;
	}
}
