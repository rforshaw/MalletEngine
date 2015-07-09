package com.linxonline.mallet.event ;

/*===========================================*/
// Event
// Used to store relevant data to pass to 
// other objects. 
/*===========================================*/
public final class Event<T>
{
	public static final EventType NONE_EVENT_TYPES = EventType.get( "NONE" ) ;				// Not interested in any Event Types, used by getWantedEventTypes()
	public static final EventType ALL_EVENT_TYPES = EventType.get( "ALL" ) ;				// Interested in all Event Types, used by getWantedEventTypes()

	private static final EventHandlerMeta BLANK_META = new EventHandlerMeta()	// Blank Meta Handler, used if sender doesn't provide one
	{
		public String getName()
		{
			return "UNKNOWN" ;
		}
	} ;

	private EventType eventType = null ;
	private EventHandlerMeta meta = BLANK_META ;			// Information about sender
	private T variable = null ;				// Event package contains data the reciever is interested in

	public Event()
	{
		setEvent( "NONE", null ) ;
	}

	public Event( final String _eventType, final T _object )
	{
		setEvent( _eventType, _object ) ;
	}

	public Event( final String _eventType, final T _object, final EventHandlerMeta _meta )
	{
		setEvent( _eventType, _object, _meta ) ;
	}

	/**
		Do the exceptionally slow String by String comparison
	**/
	public final boolean isEventByString( final String _name )
	{
		return EventType.equals( _name, eventType ) ;
	}

	public final boolean isEventByType( final EventType _type )
	{
		return EventType.equals( _type, eventType ) ;
	}

	/**
		Enables an Event to be reused.
	**/
	public final void setEvent( final String _eventType, final T _object )
	{
		setEvent( _object ) ;
		setEventType( _eventType ) ;
	}

	/**
		Enables an Event to be reused.
	**/
	public final void setEvent( final String _eventType, final T _object, final EventHandlerMeta _meta )
	{
		setEvent( _object ) ;
		setEventType( _eventType ) ;
		meta = ( _meta != null ) ? _meta : BLANK_META ;
	}

	public void setEventType( final EventType _eventType )
	{
		eventType = EventType.get( _eventType ) ;
	}
	
	public void setEventType( final String _eventType )
	{
		eventType = EventType.get( _eventType ) ;
	}

	public void setEvent( final T _object )
	{
		variable = _object ;
	}
	
	public final EventType getEventType()
	{
		return eventType ;
	}

	public final T getVariable()
	{
		return variable ;
	}

	/**
		Return any information about the sender of 
		this Event.
	*/
	public EventHandlerMeta getHandlerMeta()
	{
		return meta ;
	}

	public String toString()
	{
		final StringBuffer buffer = new StringBuffer() ;
		buffer.append( "[Event Type: " + eventType ) ;
		buffer.append( ", Meta: " + meta.getName() ) ;
		buffer.append( ", Event: " + variable.toString() + "]" ) ;
		return buffer.toString() ;
	}
}