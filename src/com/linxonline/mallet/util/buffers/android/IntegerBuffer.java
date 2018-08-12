package com.linxonline.mallet.util.buffers.android ;

import com.linxonline.mallet.util.buffers.IIntegerBuffer ;

public class IntegerBuffer implements IIntegerBuffer
{
	private int[] array ;

	public IntegerBuffer( final int _size )
	{
		array = new int[_size] ;
	}

	public IntegerBuffer( final IIntegerBuffer _buffer, final int _size )
	{
		final int length = _buffer.size() + _size ;
		array = new int[length] ;

		for( int i = 0; i < _buffer.size(); i++ )
		{
			array[i] = _buffer.get( i ) ;
		}
	}

	@Override
	public IIntegerBuffer allocate( final int _size )
	{
		return new IntegerBuffer( _size ) ;
	}

	@Override
	public IIntegerBuffer expand( final IIntegerBuffer _buffer, final int _size )
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
