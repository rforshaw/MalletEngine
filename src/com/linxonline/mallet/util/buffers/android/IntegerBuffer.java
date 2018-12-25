package com.linxonline.mallet.util.buffers ;

import com.linxonline.mallet.util.buffers.IIntegerBuffer ;

public class IntegerBuffer implements IIntegerBuffer
{
	private int[] array ;

	public IntegerBuffer( final int _size )
	{
		array = new int[_size] ;
	}

	public IntegerBuffer( final IntegerBuffer _buffer, final int _size )
	{
		final int length = _buffer.size() + _size ;
		array = new int[length] ;

		for( int i = 0; i < _buffer.size(); i++ )
		{
			array[i] = _buffer.get( i ) ;
		}
	}

	public static IntegerBuffer allocate( final int _size )
	{
		return new IntegerBuffer( _size ) ;
	}

	public static IntegerBuffer expand( final IntegerBuffer _buffer, final int _size )
	{
		return new IntegerBuffer( _buffer, _size ) ;
	}

	@Override
	public int size()
	{
		return array.length ;
	}

	@Override
	public int set( final int _index, final int _val )
	{
		return array[_index] = _val ;
	}

	@Override
	public int get( final int _index )
	{
		return array[_index] ;
	}
}
