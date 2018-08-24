package com.linxonline.mallet.renderer.opengl ;

import java.lang.ref.WeakReference ;

public class Location<T, U>
{
	private final WeakReference<LocationBuffer<T, U>> buffer ;
	private U data = null ;			// Developer data associated with this location

	private Range index ;
	private Range vertex ;

	public Location( final LocationBuffer<T, U> _buffer,
					 final int _indexStart, final int _indexEnd,
					 final int _vertexStart, final int _vertexEnd )
	{
		buffer = new WeakReference<LocationBuffer<T, U>>( _buffer ) ;
		index = new Range( _indexStart, _indexEnd ) ;
		vertex = new Range( _vertexStart, _vertexEnd ) ;
	}

	public U setLocationData( final U _data )
	{
		data = _data ;
		return data ;
	}

	public U getLocationData()
	{
		return data ;
	}

	public T getBufferData()
	{
		final LocationBuffer<T, U> b = buffer.get() ;
		return ( b != null ) ? b.getData() : null ;
	}

	/**
		Remove this location from the buffer it is assigned to.
	*/
	public void deallocate()
	{
		final LocationBuffer<T, U> b = buffer.get() ;
		if( b != null )
		{
			b.deallocate( this ) ;
			// Once the location has been removed from the 
			// buffer we should clear the reference so no silly 
			// business with the buffers data.
			buffer.clear() ;
		}
	}

	/**
		Return the index range in bytes
	*/
	public Range getIndex()
	{
		return index ;
	}

	/**
		Return the vertex range in bytes
	*/
	public Range getVertex()
	{
		return vertex ;
	}

	public class Range
	{
		private int start ;
		private int end ;

		public Range( final int _start, final int _end )
		{
			start = _start ;
			end = _end ;
		}

		public void set( final int _start, final int _end )
		{
			start = _start ;
			end = _end ;
		}

		public int size()
		{
			return getEnd() - getStart() ;
		}

		public int getStart()
		{
			return start ;
		}

		public int getEnd()
		{
			return end ;
		}
	}
}
