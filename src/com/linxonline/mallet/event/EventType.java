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
	static
	{
		eventTypes.put( "NONE", new EventType() ) ;
		eventTypes.put( "ALL", new EventType() ) ;
	}

	private final String name ;

	private EventType()
	{
		name = "UNDEFINED" ;
	}

	private EventType( final String _name )
	{
		name = _name ;
	}

	public String toString()
	{
		return name ;
	}

	/**
		Return the EventType corresponding to the String 
		passed in. Create an EventType if one doesn't exist.
	*/
	public static EventType get( final String _type )
	{
		synchronized( eventTypes )
		{
			if( eventTypes.containsKey( _type ) )
			{
				return eventTypes.get( _type ) ;
			}

			final EventType type = new EventType( _type ) ;
			eventTypes.put( _type, type ) ;
			return type ;
		}
	}
}
