package com.linxonline.mallet.event ;

/**
	Used by the Event Controller to allow the developer to 
	effectively process the Event stream.
	It's aim is to allow the developer to split-up event tasks 
	into smaller more managable groups.
*/
public abstract class EventProcessor
{
	private final String name ;			// Event Processor name, for easy identification
	private final EventType type ;		// Only process events that match this Event Type

	public EventProcessor()
	{
		name = "NONE" ;
		type = null ;
	}

	public EventProcessor( final String _name )
	{
		name = _name ;
		type = null ;
	}

	public EventProcessor( final String _name, final String _type )
	{
		name = _name ;
		type = EventType.get( _type ) ;
	}

	public void passEvent( final Event<?> _event )
	{
		if( isInterested( _event ) == true )
		{
			processEvent( _event ) ;
		}
	}

	public abstract void processEvent( final Event<?> _event ) ;

	private boolean isInterested( final Event<?> _event )
	{
		if( type != null )
		{
			return _event.isEventByType( type ) ;
		}

		return true ;		// If type not set, assumme interested in all
	}

	public String getName()
	{
		return name ;
	}

	public String toString()
	{
		return "[Event Processor: " + name + " Type: " + type + "]" ;
	}
}