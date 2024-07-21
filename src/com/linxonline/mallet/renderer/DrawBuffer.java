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

			buffers.add( buffer ) ;
		}
	}

	public void removeBuffers( final GeometryBuffer ... _buffers )
	{
		final int size = _buffers.length ;
		for( int i = 0; i < size; ++i )
		{
			final GeometryBuffer buffer = _buffers[i] ;
			buffers.remove( buffer ) ;
		}
	}

	public GeometryBuffer getBuffer( final int _index )
	{
		return buffers.get( _index ) ;
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
