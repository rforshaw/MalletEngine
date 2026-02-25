package com.linxonline.mallet.event ;

import java.util.List ;

import com.linxonline.mallet.util.Tuple ;

/*===========================================*/
// Event
// Used to store relevant data to pass to 
// other objects. 
/*===========================================*/
public final class Event<T>
{
	private static final EventState STATE = new EventState() ;

	private final EventType eventType ;
	private final T variable ;							// Event package contains data the receiver is interested in

	private Event( final String _eventType )
	{
		this( _eventType, null ) ;
	}

	private Event( final String _eventType, final T _object )
	{
		eventType = EventType.get( _eventType ) ;
		variable = _object ;
	}

	private Event( final EventType _type, final T _object )
	{
		eventType = _type ;
		variable = _object ;
	}

	public static Tuple<String, IProcess<?>> create( final String _name, final IProcess<?> _processor )
	{
		return Tuple.<String, IProcess<?>>build( _name, _processor ) ;
	}

	public static void addEvent( final Event<?> _event )
	{
		STATE.addEvent( _event ) ;
	}

	public static void addEvents( final List<Event<?>> _events )
	{
		STATE.addEvents( _events ) ;
	}

	public static <T> EventQueue<T> get( final String _type )
	{
		return STATE.get( _type ) ;
	}

	/**
	 * Get the event-queue associated with the event-type.
	 * If the event-queue does not exist, create it.
	 */
	public static <T> EventQueue<T> get( final EventType _type )
	{
		return STATE.get( _type ) ;
	}

	public static void clear()
	{
		STATE.clear() ;
	}

	public static EventState getGlobalState()
	{
		return STATE ;
	}

	public static <T> Event<T> create( final String _eventType )
	{
		return new Event<T>( _eventType ) ;
	}

	public static <T> Event<T> create( final String _eventType, final T _object )
	{
		return new Event<T>( _eventType, _object ) ;
	}

	public static <T> Event<T> create( final EventType _type, final T _object )
	{
		return new Event<T>( _type, _object ) ;
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

	@Override
	public int hashCode()
	{
		return eventType.hashCode() * variable.hashCode() ;
	}

	@Override
	public boolean equals( final Object _obj )
	{
		if( _obj instanceof Event<?> e )
		{
			return eventType == e.eventType && variable == e.variable ;
		}

		return false ;
	}

	@Override
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

	@FunctionalInterface
	public interface IProcess<T>
	{
		public void process( final T _value ) ;
	}
}
