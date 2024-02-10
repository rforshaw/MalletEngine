package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

/**
	GroupBuffer allows you to bring together a set of buffers.
	This allows you to manage the buffers within a world
	as if they were one buffer.

	Originally you would add your buffers directly to the World,
	you would potentially run into the case where another buffer
	could be added that conflicts with your carefully added ones.

	Adding them to a GroupBuffer then adding it to the World can
	remove this problem.
*/
public class GroupBuffer extends ABuffer implements IManageBuffers
{
	private final int order ;
	private final List<ABuffer> buffers = MalletList.<ABuffer>newList() ;

	public GroupBuffer( final int _order )
	{
		order = _order ;
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

	@Override
	public List<ABuffer> getBuffers()
	{
		return buffers ;
	}

	@Override
	public BufferType getBufferType()
	{
		return BufferType.GROUP ;
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
