package com.linxonline.mallet.event ;

import java.util.Map ;

import com.linxonline.mallet.util.MalletMap ;

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
	private final static Map<String, EventType> eventTypes = MalletMap.<String, EventType>newMap() ;

	// An Event Processor set to use NONE will not 
	// receive any events from the Event System.
	public static final EventType NONE = EventType.get( "NONE" ) ;

	// An Event Processor set to use ALL will accept all 
	// events from the Event System it is registered to.
	public static final EventType ALL = EventType.get( "ALL" ) ;

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
			final EventType type = eventTypes.get( _type ) ;
			if( type != null )
			{
				return type ;
			}

			final EventType newType = new EventType( _type ) ;
			eventTypes.put( _type, newType ) ;
			return newType ;
		}
	}
}
