package com.linxonline.mallet.renderer.opengl ;

import java.util.List ;

import com.linxonline.mallet.util.buffers.IFloatBuffer ;
import com.linxonline.mallet.util.buffers.IIntegerBuffer ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletList ;

public class Buffers<T, U>
{
	private final int maxIndex ;
	private final int maxVertex ;

	private final List<LocationBuffer<T, U>> locations = MalletList.<LocationBuffer<T, U>>newList() ;
	private final Listener<T, U> listener ;

	public Buffers( final int _maxIndex,
					final int _maxVertex,
					final Listener<T, U> _listener )
	{
		maxIndex = _maxIndex ;
		maxVertex = _maxVertex ;

		listener = _listener ;
	}

	/**
		Using the index and vertex buffer passed in allocate 
		a location within the buffer that is suitable.
	*/
	public Location<T, U> allocate( final int _indexBytesSize, final int _vertexByteSize )
	{
		if( _indexBytesSize >= maxIndex ||
			_vertexByteSize >= maxVertex )
		{
			Logger.println( "Exceeds maximum buffer range.", Logger.Verbosity.MINOR ) ;
			return null ;
		}

		// Find a buffer that can satisfy the index and vertex allocation needs.
		for( final LocationBuffer<T, U> buffer : locations )
		{
			final Location<T, U> location = buffer.allocate( _indexBytesSize, _vertexByteSize ) ;
			if( location != null )
			{
				return location ;
			}
		}

		// There is no existing buffer that can meet the requested allocations
		// so we need to create a new buffer!
		final LocationBuffer<T, U> buffer = new LocationBuffer<T, U>( maxIndex, maxVertex ) ;
		listener.allocated( buffer ) ;

		locations.add( buffer ) ;
		return allocate( _indexBytesSize, _vertexByteSize ) ;
	}

	public interface Listener<T, U>
	{
		public void allocated( final LocationBuffer<T, U> _allocated ) ;

		public void deallocated( final LocationBuffer<T, U> _allocated ) ;
	}
}
