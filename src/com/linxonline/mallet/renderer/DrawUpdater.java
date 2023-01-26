package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.util.Parallel ;

/**
	Update the Draw object state stored within the GeometryBuffers
	attached to a particular DrawBuffer.

	NOTE: Don't use this DrawUpdater if you share GeometryBuffers and 
	Draw objects within multiple DrawBuffers, updating will be incorrect.
*/
public class DrawUpdater implements IUpdater<GeometryBuffer>
{
	protected final DrawBuffer drawBuffer ;

	private final ParallelUpdater<Draw> parallelUpdater = new ParallelUpdater<Draw>() ;

	private Interpolation mode ;
	private boolean forceUpdate = false ;
	private boolean dirty = true ;

	public DrawUpdater( final DrawBuffer _draw )
	{
		this( Interpolation.LINEAR, _draw ) ;
	}

	public DrawUpdater( Interpolation _mode, final DrawBuffer _draw )
	{
		mode = ( _mode != null ) ? _mode : Interpolation.LINEAR ;
		drawBuffer = _draw ;
	}

	/**
		Force all GeometryBuffers assigned to this updater
		to be re/uploaded to the GPU.
		This should only be called if the Draw objects shapes
		have changed. 
	*/
	public void forceUpdate()
	{
		forceUpdate = true ;
	}

	/**
		Update the draw objects position, scale, and rotation
		using the interpolation mode specified.
		NOTE: This does not trigger an upload to the GPU. 
	*/
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

	public Program getProgram()
	{
		return drawBuffer.getProgram() ;
	}

	public Shape.Attribute[] getAttribute()
	{
		return drawBuffer.getAttribute() ;
	}

	public Shape.Style getStyle()
	{
		return drawBuffer.getStyle() ;
	}

	public boolean isUI()
	{
		return drawBuffer.isUI() ;
	}

	public int getOrder()
	{
		return drawBuffer.getOrder() ;
	}

	public static boolean isCompatible( final DrawUpdater _lhs, final DrawUpdater _rhs )
	{
		return DrawBuffer.isCompatible( _lhs.drawBuffer, _rhs.drawBuffer ) ;
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
		for( final GeometryBuffer buffer : buffers )
		{
			parallelUpdater.set( mode, _diff, _iteration, forceUpdate ) ;

			final List<Draw> draws = buffer.getDraws() ;
			Parallel.forEach( draws, parallelUpdater ) ;

			final boolean stateHasChanged = parallelUpdater.hasStateChanged() ;
			dirty = parallelUpdater.isDirty() ;

			if( stateHasChanged == true )
			{
				// The Geometry Buffer will need to be updated if a 
				// draw object state has changed, or if it's been forced.
				_updated.add( buffer ) ;
			}
		}

		forceUpdate = false ;
	}

	public void setInterpolation( Interpolation _mode )
	{
		mode = ( _mode != null ) ? _mode : mode ;
	}
}
