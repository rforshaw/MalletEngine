package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

public class Stencil extends ABuffer
{
	private final int order ;

	private final Action stencilFail ;
	private final Action depthFail ;
	private final Action depthPass ;

	private final Operation op ;
	private final int reference ;
	private final int mask ;

	private boolean clear = false ;
	private boolean enable = true ;

	private final List<ABuffer> buffers ;

	public Stencil( final int _order,
					final Action _stencilFail, final Action _depthFail, final Action _depthPass,
					final Operation _op, final int _reference, final int _mask )
	{
		buffers = MalletList.<ABuffer>newList() ;
		order = _order ;

		stencilFail = _stencilFail ;
		depthFail = _depthFail ;
		depthPass = _depthPass ;

		op = _op ;
		reference = _reference ;
		mask = _mask ;
	}

	/**
		Determine whether the stencilbuffer should cleared
		before enacting the specified stencil operations.
	*/
	public void setClear( final boolean _clear )
	{
		clear = clear ;
	}

	/**
		Determine whether stencil testing should be enabled.
		If set to true, this will trigger enabling the stencil tests.
		If set to false, this will disable the stencil tests.
		NOTE: Stencil tests will be disabled by the World at the end
		of the draw call we do not want stencil operations from one world
		impacting another.
	*/
	public void setEnable( final boolean _enable )
	{
		enable = _enable ;
	}

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

	public void removeBuffers( final ABuffer ... _buffers )
	{
		for( final ABuffer buffer : _buffers )
		{
			buffers.remove( buffer ) ;
		}
	}

	public List<ABuffer> getBuffers()
	{
		return buffers ;
	}

	public Action getStencilFail()
	{
		return stencilFail ;
	}

	public Action getDepthFail()
	{
		return depthFail ;
	}

	public Action getDepthPass()
	{
		return depthPass ;
	}

	public Operation getOperation()
	{
		return op ;
	}

	public int getReference()
	{
		return reference ;
	}

	public int getMask()
	{
		return mask ;
	}

	@Override
	public BufferType getBufferType()
	{
		return BufferType.STENCIL ;
	}

	@Override
	public int getOrder()
	{
		return order ;
	}
}
