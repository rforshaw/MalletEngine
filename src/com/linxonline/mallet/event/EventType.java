package com.linxonline.mallet.event ;

import java.util.HashMap ;

/**
	The aim of Event Type is to provide faster checks 
	when processing Events.
	An EventType is created to match-up with a specific 
	keyword, all Events that use that specific keyword 
	will be given the same EventType.
	Instead of a String comparison being used, a memory 
	address comparison is done.
	This implementation still allows the developer to handle 
	Events as if their EventType was a String.
*/
public final class EventType
{
	private final static HashMap<String, EventType> eventTypes = new HashMap<String, EventType>() ;

	private final String type ;

	private EventType( final String _type )
	{
		type = _type ;
	}

	public String getType()
	{
		return type ;
	}

	@Override
	public String toString()
	{
		return "[Type: " + type + "]" ;
	}

	/**
		Return the EventType corresponding to the String 
		passed in. Create an EventType if one doesn't exist.
	*/
	public static EventType get( final String _type )
	{
		final EventType type ;
		synchronized( eventTypes )
		{
			type = eventTypes.get( _type ) ;
		}

		return ( type != null ) ? type : createEventType( _type ) ;
	}

	/**
		Creates an EventType that correspondes with the String 
		passed in.
	*/
	private static EventType createEventType( final String _type )
	{
		final EventType type = new EventType( _type ) ;
		synchronized( eventTypes )
		{
			eventTypes.put( _type, type ) ;
		}

		return type ;
	}

	public static boolean equals( final String _left, final EventType _right )
	{
		return EventType.equals( get( _left ), _right ) ;
	}

	public static boolean equals( final EventType _left, final EventType _right )
	{
		return _left == _right ;
	}
}