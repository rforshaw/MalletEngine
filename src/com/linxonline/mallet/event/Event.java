package com.linxonline.mallet.event ;

/*===========================================*/
// Event
// Used to store relevant data to pass to 
// other objects. 
/*===========================================*/
public final class Event<T>
{
	// Blank Meta Handler, used if sender doesn't provide one
	private static final IEventHandlerMeta BLANK_META = new IEventHandlerMeta()
	{
		public String getName()
		{
			return "UNKNOWN" ;
		}
	} ;

	private EventType eventType = EventType.NONE ;
	private IEventHandlerMeta meta = BLANK_META ;		// Information about sender
	private T variable = null ;							// Event package contains data the reciever is interested in

	public Event()
	{
		this( "NONE", null ) ;
	}

	public Event( final String _eventType )
	{
		this( _eventType, null ) ;
	}

	public Event( final String _eventType, final T _object )
	{
		setEvent( _eventType, _object ) ;
	}

	public Event( final String _eventType, final T _object, final IEventHandlerMeta _meta )
	{
		setEvent( _eventType, _object, _meta ) ;
	}

	public final boolean isEventByType( final EventType _type )
	{
		return eventType == _type ;
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
	public final void setEvent( final String _eventType, final T _object, final IEventHandlerMeta _meta )
	{
		setEvent( _object ) ;
		setEventType( _eventType ) ;
		meta = ( _meta != null ) ? _meta : BLANK_META ;
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
	public IEventHandlerMeta getHandlerMeta()
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
