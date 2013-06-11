package com.linxonline.mallet.event ;

import java.io.Serializable ;

/*===========================================*/
// Event
// Used to store relevant data to pass to 
// other objects. 
/*===========================================*/
public final class Event implements Serializable
{
	public static final String[] NONE_EVENT_TYPES = { "NONE" } ;
	public static final String[] ALL_EVENT_TYPES = { "ALL" } ;

	private int hashCode = 0 ;
	private String eventType = null ;
	private Object variable = null ;

	public Event()
	{
		eventType = "NONE" ;
		hashCode = eventType.hashCode() ;
	}

	public Event( final String _eventType, final Object _object )
	{
		setEvent( _eventType, _object ) ;
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
	public final void setEvent( final String _eventType, final Object _object )
	{
		eventType = _eventType ;
		variable = _object ;
		hashCode = eventType.hashCode() ;
	}

	public final String getEventType()
	{
		return eventType ;
	}

	public final Object getVariable()
	{
		return variable ;
	}
}