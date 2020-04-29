package com.linxonline.mallet.util.buffers ;

public class IntegerBuffer
{
	private IntegerBuffer() {}

	public static int[] allocate( final int _size )
	{
		return new int[_size] ;
	}

	public static int[] expand( final int[] _from, final int _extra )
	{
		final int length = _from.length + _extra ;
		final int[] to = new int[length] ;
		System.arraycopy( _from, 0, to, 0, _from.length ) ;
		return to ;
	}

	public static int set( final int[] _set, final int _index, final int _val )
	{
		return _set[_index] = _val ;
	}
}
