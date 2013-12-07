package com.linxonline.mallet.event ;

public abstract class EventProcessor
{
	public final String name ;

	public EventProcessor() { name = "NONE" ; }

	public EventProcessor( final String _name ) { name = _name ; }

	public abstract void processEvent( final Event _event ) ;
}