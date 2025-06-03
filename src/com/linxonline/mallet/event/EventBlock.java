package com.linxonline.mallet.event ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Tuple ;

public final class EventBlock implements IEventBlock
{
	private final List<Tuple<String, Event.IProcess<?>>> sources = MalletList.<Tuple<String, Event.IProcess<?>>>newList() ;
	private final List<Tuple<Event.IProcess<?>, EventQueue<?>>> procs = MalletList.<Tuple<Event.IProcess<?>, EventQueue<?>>>newList() ;

	private final EventState state ;

	public EventBlock()
	{
		this( Event.getGlobalState() ) ;
	}

	public EventBlock( final EventState _state )
	{
		state = _state ;
	}
	
	public EventBlock( final Tuple<String, Event.IProcess<?>> ... _processors )
	{
		this( Event.getGlobalState(), _processors ) ;
	}

	public EventBlock( final EventState _state, final Tuple<String, Event.IProcess<?>> ... _processors )
	{
		state = _state ;
		for( final Tuple<String, Event.IProcess<?>> t : _processors )
		{
			add( t.getLeft(), t.getRight() ) ;
		}
	}

	@Override
	public EventState getEventState()
	{
		return state ;
	}

	@Override
	public <T> Event.IProcess<T> add( final String _type, final Event.IProcess<T> _proc )
	{
		return add( EventType.get( _type ), _proc ) ;
	}

	@Override
	public <T> Event.IProcess<T> add( final EventType _type, final Event.IProcess<T> _proc )
	{
		sources.add( Tuple.<String, Event.IProcess<?>>build( _type.getType(), _proc ) ) ;
		procs.add( Tuple.<Event.IProcess<?>, EventQueue<?>>build( _proc, state.get( _type ) ) ) ;

		return _proc ;
	}

	@Override
	public void update()
	{
		final int size = procs.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Tuple<Event.IProcess<?>, EventQueue<?>> tuple = procs.get( i ) ;

			final Event.IProcess proc = tuple.getLeft() ;
			final EventQueue queue = tuple.getRight() ;

			queue.process( proc ) ;
		}
	}
}
