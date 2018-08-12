package com.linxonline.mallet.util.buffers ;

public class IntegerBuffer
{
	private static IIntegerBuffer cheat ;

	public static void setBase( final IIntegerBuffer _base )
	{
		cheat = ( cheat == null ) ? _base : cheat ;
	}

	public static IIntegerBuffer allocate( final int _size )
	{
		return cheat.allocate( _size ) ;
	}

	public static IIntegerBuffer expand( final IIntegerBuffer _buffer, final int _size )
	{
		return cheat.expand( _buffer, _size ) ;
	}
}
