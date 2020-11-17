package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.MalletList ;

public class DrawBuffer extends ABuffer
{
	private final ArrayList<GeometryBuffer> buffers = new ArrayList<GeometryBuffer>() ;
	private final Program program ;
	private final Shape.Swivel[] swivel ;
	private final Shape.Style style ;
	private final boolean ui ;
	private final int order ;

	public DrawBuffer( final Program _program,
					   final Shape.Swivel[] _swivel,
					   final Shape.Style _style,
					   final boolean _ui,
					   final int _order )
	{
		program = _program ;
		swivel = _swivel ;
		style = _style ;
		ui = _ui ;
		order = _order ;
	}

	public void addBuffers( final GeometryBuffer ... _buffers )
	{
		buffers.ensureCapacity( buffers.size() + _buffers.length ) ;
		for( final GeometryBuffer buffer : _buffers )
		{
			buffers.add( buffer ) ;
		}
	}

	public void removeBuffers( final GeometryBuffer ... _buffers )
	{
		for( final GeometryBuffer buffer : _buffers )
		{
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

	public Shape.Swivel[] getSwivel()
	{
		return swivel ;
	}

	public Shape.Style getStyle()
	{
		return style ;
	}

	public boolean isUI()
	{
		return ui ;
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
