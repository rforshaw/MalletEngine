package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;

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
	protected final DrawBuffer drawBuffer ;
	private final ArrayList<GeometryBuffer> buffers = new ArrayList<GeometryBuffer>() ;

	private boolean forceUpdate = false ;
	private boolean dirty = true ;

	public DrawInstancedUpdater( final DrawBuffer _draw, final GeometryBuffer _geometry )
	{
		this( Interpolation.LINEAR, _draw, _geometry ) ;
	}

	public DrawInstancedUpdater( Interpolation _mode, final DrawBuffer _draw, final GeometryBuffer _geometry )
	{
		mode = ( _mode != null ) ? _mode : Interpolation.LINEAR ;
		drawBuffer = _draw ;
		buffers.add( _geometry ) ;
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
		boolean update = false ;

		for( final GeometryBuffer buffer : buffers )
		{
			final List<Draw> draws = buffer.getDraws() ;
			for( final Draw draw : draws )
			{
				if( draw.update( mode, _diff, _iteration ) == true )
				{
					update = true ;
				}
			}
		}

		if( update == true || forceUpdate == true )
		{
			_updated.add( drawBuffer ) ;
		}

		forceUpdate = false ;
		dirty = update ;
	}
}
