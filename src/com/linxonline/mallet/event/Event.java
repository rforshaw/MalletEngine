package com.linxonline.mallet.event ;

/*===========================================*/
// Event
// Used to store relevant data to pass to 
// other objects. 
/*===========================================*/
public final class Event<T>
{
	private final EventType eventType ;
	private final T variable ;							// Event package contains data the receiver is interested in

	public Event( final String _eventType )
	{
		this( _eventType, null ) ;
	}

	public Event( final String _eventType, final T _object )
	{
		eventType = EventType.get( _eventType ) ;
		variable = _object ;
	}

	public static <T> Event<T> create( final String _eventType )
	{
		return new Event<T>( _eventType ) ;
	}

	public static <T> Event<T> create( final String _eventType, final T _object )
	{
		return new Event<T>( _eventType, _object ) ;
	}

	public final boolean isEventByType( final EventType _type )
	{
		return eventType == _type ;
	}

	public final EventType getEventType()
	{
		return eventType ;
	}

	public final T getVariable()
	{
		return variable ;
	}

	public String toString()
	{
		final StringBuffer buffer = new StringBuffer() ;
		buffer.append( "[Event Type: " ) ;
		buffer.append( eventType ) ;
		buffer.append( ", Event: " ) ;
		buffer.append( variable.toString() ) ;
		buffer.append( "]" ) ;
		return buffer.toString() ;
	}
}
