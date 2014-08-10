package com.linxonline.mallet.event ;

import java.util.HashMap ;

public final class EventType
{
	private final static HashMap<String, EventType> eventTypes = new HashMap<String, EventType>() ;

	public static EventType get( final String _type )
	{
		final EventType type = eventTypes.get( _type ) ;
		return ( type != null ) ? type : createEventType( _type ) ;
	}

	private static EventType createEventType( final String _type )
	{
		final EventType type = new EventType( _type ) ;
		eventTypes.put( _type, type ) ;

		return type ;
	}

	public static boolean equals( final String _left, final EventType _right )
	{
		return EventType.equals( get( _left ), _right ) ;
	}

	public static boolean equals( final EventType _left, final EventType _right )
	{
		return _left.equals( _right ) ;
	}

	private final String type ;

	protected EventType( final String _type )
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
}