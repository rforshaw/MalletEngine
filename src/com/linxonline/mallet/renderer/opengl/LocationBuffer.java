package com.linxonline.mallet.renderer.opengl ;

import java.util.List ;

import com.linxonline.mallet.util.buffers.IFloatBuffer ;
import com.linxonline.mallet.util.buffers.IIntegerBuffer ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletList ;

/**
	Keep track of allocations made into this buffer.
	Prevent allocations from being made that exceed the 
	maximum requirements.
	
	T : Developer data-type associated with this LocationBuffer.
	U : Developer data-type associated with a Location.
*/
public class LocationBuffer<T, U>
{
	private static Listener FALLBACK = new Listener()
	{
		@Override
		public void allocated( final Location _location )
		{
			Logger.println( "Location allocated to buffer without listener set.", Logger.Verbosity.MINOR ) ;
		}

		@Override
		public void deallocated( final Location _location )
		{
			Logger.println( "Location deallocated from buffer without listener set.", Logger.Verbosity.MINOR ) ;
		}

		@Override
		public void shifted( final Location _location ) {}

		@Override
		public void shiftEnded( final LocationBuffer _buffer )
		{
			Logger.println( "Location/s shifted in buffer without listener set.", Logger.Verbosity.MINOR ) ;
		}
	} ;

	private final int maxByteIndex ;
	private final int maxByteVertex ;

	private final List<Location<T, U>> locations = MalletList.<Location<T, U>>newList() ;

	private T data = null ;							// Developer data associated with this buffer
	private Listener<T, U> listener = FALLBACK ;

	private int currentByteIndex = 0 ;
	private int currentByteVertex = 0 ;

	private int allocatedByteIndex = 0 ;
	private int allocatedByteVertex = 0 ;

	public LocationBuffer( final int _maxByteIndex,
						   final int _maxByteVertex )
	{
		maxByteIndex = _maxByteIndex ;
		maxByteVertex = _maxByteVertex ;
	}

	/**
		When a Location Buffer is allocated you can set 
		this listener to be informed when location state changes.
	*/
	public void setListener( final Listener<T, U> _listener )
	{
		listener = ( _listener != null ) ? _listener : FALLBACK ;
	}

	public T setData( final T _data )
	{
		data = _data ;
		return data ;
	}

	public T getData()
	{
		return data ;
	}
	
	/**
		Using the index and vertex buffer passed in allocate 
		a location within the buffer that is suitable.
	*/
	public Location<T, U> allocate( final int _indexBytesSize, final int _vertexByteSize )
	{
		final int endByteIndex = currentByteIndex + _indexBytesSize ;
		final int endByteVertex = currentByteVertex = _vertexByteSize ;
		if( endByteIndex >= maxByteIndex ||
			endByteVertex >= maxByteVertex )
		{
			return null ;
		}

		final Location<T, U> location = new Location<T, U>( this,
															currentByteIndex, endByteIndex,
															currentByteVertex, endByteVertex ) ;
		locations.add( location ) ;

		currentByteIndex = endByteIndex ;
		currentByteVertex = endByteVertex ;

		allocatedByteIndex = currentByteIndex ;
		allocatedByteIndex = currentByteVertex ;

		listener.allocated( location ) ;
		return location ;
	}

	public void deallocate( final Location<T, U> _location )
	{
		final int index = locations.indexOf( _location ) ;
		if( index == -1 )
		{
			Logger.println( "Attempting to remove location from incorrect buffer.", Logger.Verbosity.MINOR ) ;
			return ;
		}

		final int shiftByteIndex = _location.getIndex().size() ;
		final int shiftByteVertex = _location.getVertex().size() ;

		// Shift the locations
		final int size = locations.size() ;
		for( int i = index + 1; i < size; ++i )
		{
			final Location<T, U> location = locations.get( i ) ;
			final Location.Range indexRange = location.getIndex() ;
			indexRange.set( indexRange.getStart() - shiftByteIndex, indexRange.getEnd() - shiftByteIndex ) ;

			final Location.Range vertexRange = location.getVertex() ;
			vertexRange.set( vertexRange.getStart() - shiftByteVertex, vertexRange.getEnd() - shiftByteVertex ) ;

			listener.shifted( location ) ;
		}

		listener.deallocated( _location ) ;
		listener.shiftEnded( this ) ;

		currentByteIndex -= shiftByteIndex ;
		currentByteVertex -= shiftByteVertex ;
	}

	public interface Listener<T, U>
	{
		public void allocated( final Location<T, U> _location ) ;

		public void deallocated( final Location<T, U> _location ) ;

		public void shifted( final Location<T, U> _location ) ;

		public void shiftEnded( final LocationBuffer<T, U> _buffer ) ;
	}
}
