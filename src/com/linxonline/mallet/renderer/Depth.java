package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

public class Depth extends ABuffer implements IManageBuffers
{
	private final int order ;

	private final Operation op ;
	private final boolean mask ;

	private boolean clear = false ;
	private boolean enable = true ;

	/**
		It's likely that whatever is being rendered as the stencil
		we don't actually want to render to the colour buffer.
		Assume by default this is the case, and prevent rendering
		to it.
	*/
	private final boolean[] colourMask = new boolean[] { false, false, false, false } ;

	private final List<ABuffer> buffers ;

	public Depth( final int _order, final Operation _op, final boolean _mask )
	{
		buffers = MalletList.<ABuffer>newList() ;
		order = _order ;

		op = _op ;
		mask = _mask ;
	}

	/**
		Determine whether the stencilbuffer should cleared
		before enacting the specified depth operations.
	*/
	public void setClear( final boolean _clear )
	{
		clear = clear ;
	}

	/**
		Determine whether depth testing should be enabled.
		If set to true, this will trigger enabling the depth tests.
		If set to false, this will disable the depth tests.
		NOTE: Depth tests will be disabled by the World at the end
		of the draw call we do not want depth operations from one world
		impacting another.
	*/
	public void setEnable( final boolean _enable )
	{
		enable = _enable ;
	}

	public void setColourMask( final boolean _red, final boolean _green, final boolean _blue, final boolean _alpha )
	{
		colourMask[0] = _red ;
		colourMask[1] = _green ;
		colourMask[2] = _blue ;
		colourMask[3] = _alpha ;
	}

	@Override
	public ABuffer[] addBuffers( final ABuffer ... _buffers )
	{
		for( final ABuffer buffer : _buffers )
		{
			insert( buffer, buffers ) ;
		}
		return _buffers ;
	}

	private static void insert( final ABuffer _insert, final List<ABuffer> _list )
	{
		final int size = _list.size() ;
		for( int i = 0; i < size; i++ )
		{
			final ABuffer toCompare = _list.get( i ) ;
			if( _insert.getOrder() <= toCompare.getOrder() )
			{
				_list.add( i, _insert ) ;		// Insert at index location
				return ;
			}
		}

		_list.add( _insert ) ;
	}

	@Override
	public void removeBuffers( final ABuffer ... _buffers )
	{
		for( final ABuffer buffer : _buffers )
		{
			buffers.remove( buffer ) ;
		}
	}

	public boolean isEnabled()
	{
		return enable ;
	}

	public boolean shouldClear()
	{
		return clear ;
	}

	@Override
	public List<ABuffer> getBuffers()
	{
		return buffers ;
	}

	public Operation getOperation()
	{
		return op ;
	}

	public boolean getMask()
	{
		return mask ;
	}

	public boolean[] getColourMask()
	{
		return colourMask ;
	}

	@Override
	public BufferType getBufferType()
	{
		return BufferType.DEPTH ;
	}

	@Override
	public int getOrder()
	{
		return order ;
	}

	@Override
	public void requestUpdate()
	{
		DrawAssist.update( this ) ;
	}
}
