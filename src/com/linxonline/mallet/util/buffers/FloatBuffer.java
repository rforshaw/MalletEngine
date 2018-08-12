package com.linxonline.mallet.util.buffers ;

public class FloatBuffer
{
	public static IFloatBuffer cheat ;

	public static void setBase( final IFloatBuffer _base )
	{
		cheat = ( cheat == null ) ? _base : cheat ;
	}

	public static IFloatBuffer allocate( final int _size )
	{
		return cheat.allocate( _size ) ;
	}

	public static IFloatBuffer expand( final IFloatBuffer _buffer, final int _size )
	{
		return cheat.expand( _buffer, _size ) ;
	}

	public static void copy( final IFloatBuffer _from, final IFloatBuffer _to )
	{
		final int size = _from.size() ;
		for( int i = 0; i < size; ++i )
		{
			_to.set( i, _from.get( i ) ) ;
		}
	}
}
