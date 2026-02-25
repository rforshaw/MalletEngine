package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.Comparator ;
import java.util.Collections ;

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
public final class GroupBuffer extends ABuffer implements IManageBuffers, IManageCompatible
{
	private final int order ;
	private final List<ICompatibleBuffer> buffers = MalletList.<ICompatibleBuffer>newList() ;

	public GroupBuffer( final int _order )
	{
		order = _order ;
	}

	@Override
	public <T extends IManageCompatible> T addBuffer( final T _buffer )
	{
		insert( _buffer, buffers ) ;
		return _buffer ;
	}

	private static void insert( final IManageCompatible _insert, final List<ICompatibleBuffer> _list )
	{
		final int size = _list.size() ;
		for( int i = 0; i < size; i++ )
		{
			final ICompatibleBuffer toCompare = _list.get( i ) ;
			if( _insert.getOrder() <= toCompare.getOrder() )
			{
				_list.add( i, _insert ) ;		// Insert at index location
				return ;
			}
		}

		_list.add( _insert ) ;
	}

	@Override
	public <T extends IManageCompatible> void removeBuffer( final T _buffer )
	{
		buffers.remove( _buffer ) ;
	}

	@Override
	public List<ICompatibleBuffer> getBuffers()
	{
		return buffers ;
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

	/**
		Trigger a call to sort the order in which
		the buffers are rendered to.
		This is the same as if you called:
		Collections.sort( buffers, ... ) ;
	*/
	public void sortBuffers( final Comparator<ICompatibleBuffer> _c )
	{
		Collections.sort( buffers, _c ) ;
	}
}
