package com.linxonline.mallet.util.buffers ;

public interface IIntegerBuffer
{
	public IIntegerBuffer allocate( final int _size ) ;
	public IIntegerBuffer expand( final IIntegerBuffer _buffer, final int _size ) ;

	public int size() ;

	public int set( final int _index, final int _val ) ;
	public int get( final int _index ) ;
} 
