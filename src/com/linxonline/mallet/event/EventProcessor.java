package com.linxonline.mallet.event ;

/**
	Used by the Event Controller to allow the developer to 
	effectively process the Event stream.
	Its aim is to allow the developer to split-up event tasks 
	into smaller more managable groups.
*/
public abstract class EventProcessor<T>
{
	private final String name ;			// Event Processor name, for easy identification
	private final EventType type ;		// Only process events that match this Event Type

	public EventProcessor()
	{
		name = "UNKNOWN" ;
		type = Event.ALL_EVENT_TYPES ;
	}

	public EventProcessor( final String _name )
	{
		name = _name ;
		type = Event.ALL_EVENT_TYPES ;
	}

	public EventProcessor( final String _name, final String _type )
	{
		name = _name ;
		type = EventType.get( _type ) ;
	}

	@SuppressWarnings( "unchecked" )
	public void passEvent( final Event<?> _event )
	{
		if( isInterested( _event ) == true )
		{
			// The Event Type should infer what variable 
			// is being stored by the Event.
			processEvent( ( Event<T> )_event ) ;
		}
	}

	public abstract void processEvent( final Event<T> _event ) ;

	/**
		Check to see if the event passed in is of interest
		to the EventProcessor.
	*/
	private boolean isInterested( final Event<?> _event )
	{
		return _event.isEventByType( type ) ;
	}

	public EventType getEventType()
	{
		return type ;
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
