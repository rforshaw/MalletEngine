package com.linxonline.mallet.event ;

public interface IEventBlock
{
	public EventState getEventState() ;

	public <T> Event.IProcess<? super T> add( final String _type, final Event.IProcess<? super T> _proc ) ;

	public <T> Event.IProcess<? super T> add( final EventType _type, final Event.IProcess<? super T> _proc ) ;

	public void update() ;
}
