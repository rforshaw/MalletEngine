package com.linxonline.mallet.renderer ;

import java.util.List ;

/**
	Update the Draw object state stored within the GeometryBuffers
	attached to a particular DrawBuffer.

	NOTE: Don't use this DrawUpdater if you share GeometryBuffers and 
	Draw objects within multiple DrawBuffers, updating will be incorrect.
*/
public class DrawUpdater implements IUpdater<GeometryBuffer>
{
	protected final DrawBuffer drawBuffer ;

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
			boolean stateHasChanged = forceUpdate ;

			final List<Draw> draws = buffer.getDraws() ;
			for( final Draw draw : draws )
			{
				if( draw.update( mode, _diff, _iteration ) == true )
				{
					dirty = true ;
					stateHasChanged = true ;
				}
			}

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
