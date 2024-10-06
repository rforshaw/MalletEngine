package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;

public class DrawBuffer extends ABuffer
{
	public static final IOcclude OCCLUDER_FALLBACK = new IOcclude()
	{
		@Override
		public boolean occlude( final Camera _camera, final Draw _draw )
		{
			return false ;
		}
	} ;

	// Ranges and buffers are tightly coupled.
	// Ranges denotes what draw objects within a GeometryBuffer
	// and their order are to be drawn out.
	// A null array represents all draw objects are to be drawn
	// and in the order the draw objects are within the GeometryBuffer.
	// We use the DrawBuffer to define this range and order as different
	// Drawbuffers may wish to use the same GeometryBuffer but render out
	// a subset, or order in a different way.
	private final ArrayList<int[]> ranges = new ArrayList<int[]>() ;
	private final ArrayList<GeometryBuffer> buffers = new ArrayList<GeometryBuffer>() ;
	private final Program program ;
	private final boolean ui ;
	private final int order ;

	private IOcclude occluder = OCCLUDER_FALLBACK ;

	public DrawBuffer( final Program _program,
					   final boolean _ui,
					   final int _order )
	{
		program = _program ;
		ui = _ui ;
		order = _order ;
	}

	/**
		Allow the developer to specify their own occluder mechanisim.
		Before the buffer is drawn determine whether the specific
		draw object should actually be rendered.
	*/
	public void setOccluder( final IOcclude _occluder )
	{
		occluder = ( _occluder != null ) ? _occluder : OCCLUDER_FALLBACK ;
	}

	public void addBuffers( final GeometryBuffer ... _buffers )
	{
		final int size = _buffers.length ;

		buffers.ensureCapacity( buffers.size() + size ) ;
		for( int i = 0; i < size; ++i )
		{
			final GeometryBuffer buffer = _buffers[i] ;
			if( program.isCompatible( buffer.getAttribute() ) == false )
			{
				throw new RuntimeException( String.format( "Incompatible GeometryBuffer to DrawBuffer with program: %s.", program.getID() ) ) ;
			}

			ranges.add( null ) ;
			buffers.add( buffer ) ;
		}
	}

	public void removeBuffers( final GeometryBuffer ... _buffers )
	{
		final int size = _buffers.length ;
		for( int i = 0; i < size; ++i )
		{
			final GeometryBuffer buffer = _buffers[i] ;

			final int index = buffers.indexOf( buffer ) ;
			if( index >= 0 )
			{
				ranges.remove( index ) ;
				buffers.remove( index ) ;
			}
		}
	}

	/**
		Currently a range requires 3 values.
		The index of the draw object, the start shape index, and the shape count.
	*/
	public void setRange( final int _index, final int[] _range )
	{
		ranges.set( _index, _range ) ;
	}

	public int[] getRange( final int _index )
	{
		return ranges.get( _index ) ;
	}

	public GeometryBuffer getBuffer( final int _index )
	{
		return buffers.get( _index ) ;
	}

	public List<int[]> getRanges()
	{
		return ranges ;
	}
	
	public List<GeometryBuffer> getBuffers()
	{
		return buffers ;
	}

	public Program getProgram()
	{
		return program ;
	}

	public boolean isUI()
	{
		return ui ;
	}

	@Override
	public void requestUpdate()
	{
		DrawAssist.update( this ) ;
	}

	public static boolean isCompatible( final DrawBuffer _lhs, final DrawBuffer _rhs )
	{
		if( _lhs.ui != _rhs.ui )
		{
			return false ;
		}

		if( _lhs.order != _rhs.order )
		{
			return false ;
		}

		if( _lhs.occluder != _rhs.occluder )
		{
			return false ;
		}

		if( _lhs.program.equals( _rhs.program ) == false )
		{
			return false ;
		}

		return true ;
	}

	public IOcclude getOccluder()
	{
		return occluder ;
	}

	@Override
	public BufferType getBufferType()
	{
		return BufferType.DRAW_BUFFER ;
	}

	@Override
	public int getOrder()
	{
		return order ;
	}
}
