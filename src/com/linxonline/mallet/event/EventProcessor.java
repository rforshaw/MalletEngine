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

	public EventProcessor( final String _name, final EventType _type )
	{
		assert _type != null ;
		name = _name ;
		type = _type ;
	}

	public void passEvent( final Event<T> _event )
	{
		if( isInterested( _event ) == true )
		{
			processEvent( _event ) ;
		}
	}

	public abstract void processEvent( final Event<T> _event ) ;

	/**
		Check to see if the event passed in is of interest
		to the EventProcessor.
	*/
	private boolean isInterested( final Event<T> _event )
	{
		if( type != null )
		{
			return _event.isEventByType( type ) ;
		}

		return true ;		// Accepts all Events being passed to it
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