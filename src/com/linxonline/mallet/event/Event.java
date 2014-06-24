package com.linxonline.mallet.event ;

/*===========================================*/
// Event
// Used to store relevant data to pass to 
// other objects. 
/*===========================================*/
public final class Event<T>
{
	public static final String[] NONE_EVENT_TYPES = { "NONE" } ;				// Not interested in any Event Types, used by getWantedEventTypes()
	public static final String[] ALL_EVENT_TYPES = { "ALL" } ;					// Interested in all Event Types, used by getWantedEventTypes()

	private static final EventHandlerMeta BLANK_META = new EventHandlerMeta()	// Blank Meta Handler, used if sender doesn't provide one
	{
		public String getName()
		{
			return "UNKNOWN" ;
		}
	} ;

	private String eventType = null ;				// Identifier
	private int hashCode = 0 ;						// Hash representation of identifier

	private EventHandlerMeta meta = BLANK_META ;	// Information about sender
	private T variable = null ;						// Event package contains data the reciever is interested in

	public Event()
	{
		eventType = "NONE" ;
		hashCode = eventType.hashCode() ;
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
		return eventType.equals( _name ) ;
	}

	/**
		Best used with Strings of 16 or less characters
	**/
	public final boolean isEventByHashCode( final String _name )
	{
		return ( hashCode == _name.hashCode() ) ;
	}

	/**
		Best used with Strings of 16 or less characters
	**/
	public final boolean isEventByHashCode( final int _hashCode )
	{
		return ( hashCode == _hashCode ) ;
	}

	/**
		Enables an Event to be reused.
	**/
	public final void setEvent( final String _eventType, final T _object )
	{
		setEvent( _eventType, _object, null ) ;
	}

	/**
		Enables an Event to be reused.
	**/
	public final void setEvent( final String _eventType, final T _object, final EventHandlerMeta _meta )
	{
		eventType = _eventType ;
		hashCode = eventType.hashCode() ;
		variable = _object ;
		meta = ( _meta != null ) ? _meta : BLANK_META ;
	}

	public final String getEventType()
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
		buffer.append( ", Meta: " + meta.getName() + "]" ) ;
		return buffer.toString() ;
	}
}