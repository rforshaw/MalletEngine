package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.util.Parallel ;

/**
	A draw object can be added to multiple different 
	buffers with each buffer doing a different task.

	This DrawInstancedUpdater is designed to trigger 
	the update of DrawInstancedBuffer when the Draw 
	object state is still influx.
*/
public class DrawInstancedUpdater implements IUpdater<GeometryBuffer>
{
	private final Interpolation mode ;
	protected final DrawInstancedBuffer drawBuffer ;

	private final ParallelUpdater<Draw> parallelUpdater = new ParallelUpdater<Draw>() ;

	private boolean forceUpdate = false ;
	private boolean dirty = true ;

	public DrawInstancedUpdater( final DrawInstancedBuffer _draw )
	{
		this( Interpolation.LINEAR, _draw ) ;
	}

	public DrawInstancedUpdater( Interpolation _mode, final DrawInstancedBuffer _draw )
	{
		mode = ( _mode != null ) ? _mode : Interpolation.LINEAR ;
		drawBuffer = _draw ;
	}

	public void forceUpdate()
	{
		forceUpdate = true ;
	}

	public void makeDirty()
	{
		dirty = true ;
	}

	public boolean isDirty()
	{
		return dirty || forceUpdate ;
	}

	public boolean isStatic()
	{
		return drawBuffer.isStatic() ;
	}

	public void addBuffers( final GeometryBuffer ... _buffers )
	{
		makeDirty() ;
		drawBuffer.addBuffers( _buffers ) ;
	}

	public void removeBuffers( final GeometryBuffer ... _buffers )
	{
		makeDirty() ;
		drawBuffer.removeBuffers( _buffers ) ;
	}

	public GeometryBuffer getBuffer( final int _index )
	{
		return drawBuffer.getBuffer( _index ) ;
	}

	public List<GeometryBuffer> getBuffers()
	{
		return drawBuffer.getBuffers() ;
	}

	@Override
	public void update( final List<ABuffer> _updated, final int _diff, final int _iteration )
	{
		if( forceUpdate == false && dirty == false )
		{
			return ;
		}

		dirty = false ;

		final List<GeometryBuffer> buffers = drawBuffer.getBuffers() ;
		final int size = buffers.size() ;
		for( int i = 0; i < size; ++i )
		{
			final GeometryBuffer buffer = buffers.get( i ) ;
			parallelUpdater.set( mode, _diff, _iteration ) ;

			final List<Draw> draws = buffer.getDraws() ;
			Parallel.forBatch( draws, 1000, parallelUpdater ) ;

			dirty |= parallelUpdater.isDirty() ;

			if( forceUpdate == true )
			{
				// The Geometry Buffer will need to be updated if a 
				// draw object state has changed, or if it's been forced.
				_updated.add( drawBuffer ) ;
			}
		}

		forceUpdate = false ;
	}
}
