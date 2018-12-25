package com.linxonline.mallet.renderer.opengl ;

import java.util.List ;

import com.linxonline.mallet.maths.Matrix4 ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.OrderedInsert ;

public class Buffers<T, U>
{
	private final int maxIndex ;
	private final int maxVertex ;

	private final List<LocationBuffer<T, U>> locations = MalletList.<LocationBuffer<T, U>>newList() ;
	private final Listener<T, U> listener ;

	private boolean breakOut = false ;

	public Buffers( final int _maxByteIndex,
					final int _maxByteVertex,
					final Listener<T, U> _listener )
	{
		maxIndex = _maxByteIndex ;
		maxVertex = _maxByteVertex ;

		listener = _listener ;
	}

	public int getMaximumByteIndex()
	{
		return maxIndex ;
	}
	
	public int getMaximumByteVertex()
	{
		return maxVertex ;
	}

	/**
		Using the index and vertex buffer passed in allocate 
		a location within the buffer that is suitable.
	*/
	public Location<T, U> allocate( final U _user )
	{
		final int indexBytesSize = listener.calculateIndexByteSize( _user ) ;
		final int vertexByteSize = listener.calculateVertexByteSize( _user ) ;
	
		if( indexBytesSize >= maxIndex ||
			vertexByteSize >= maxVertex )
		{
			Logger.println( "Exceeds maximum buffer range.", Logger.Verbosity.MINOR ) ;
			return null ;
		}

		// Find a buffer that can satisfy the index and vertex allocation needs.
		for( final LocationBuffer<T, U> buffer : locations )
		{
			final Location<T, U> location = buffer.allocate( indexBytesSize, vertexByteSize, _user ) ;
			if( location != null )
			{
				breakOut = false ;
				return location ;
			}
		}

		if( breakOut == true )
		{
			Logger.println( "Caught in a loop, break out.", Logger.Verbosity.MINOR ) ;
			breakOut = false ;
			return null ;
		}
		breakOut = true ;

		// There is no existing buffer that can meet the requested allocations
		// so we need to create a new buffer!
		//System.out.println( "Create new Location Buffer" ) ;
		final LocationBuffer<T, U> buffer = new LocationBuffer<T, U>( maxIndex, maxVertex ) ;
		listener.allocated( buffer, _user ) ;

		OrderedInsert.insert( buffer, locations ) ;
		return allocate( _user ) ;		// repeat the allocation and use the new LocationBuffer.
	}

	public void draw( final Matrix4 _world, final Matrix4 _ui )
	{
		for( final LocationBuffer<T, U> buffer : locations )
		{
			listener.draw( _world, _ui, buffer ) ;
		}
	}

	public interface Listener<T, U>
	{
		public void draw( final Matrix4 _world, final Matrix4 _ui, final LocationBuffer<T, U> _buffer ) ;

		/**
			Calculate the index bytes size using the user data.
		*/
		public int calculateIndexByteSize( final U _user ) ;

		/**
			Calculate the vertex byte size using the user data.
		*/
		public int calculateVertexByteSize( final U _user ) ;

		/**
			A new location buffer has been initialised.
			You must set the data and order manually.
		*/
		public void allocated( final LocationBuffer<T, U> _allocated, final U _user ) ;

		/**
			A location buffer has been destroyed.
			Which shouldn't be an issue but we'll pass the 
			data back to the user in case it has resource that 
			need to be cleaned.
		*/
		public void deallocated( T _data ) ;
	}
}
