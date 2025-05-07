package com.linxonline.mallet.event ;

public interface IEventBlock
{
	public EventState getEventState() ;

	public <T> Event.IProcess<T> add( final String _type, final Event.IProcess<T> _proc ) ;

	public <T> Event.IProcess<T> add( final EventType _type, final Event.IProcess<T> _proc ) ;

	public void update() ;
}
