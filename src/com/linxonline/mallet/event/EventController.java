package com.linxonline.mallet.event ;

import java.util.ArrayList ;

/**
	This is a convience class to reduce Events being processed 
	at the wrong time.

	An Event that changes the state should process it's content 
	at the right moment to prevent crashes or unusual behaviour.

	This is to prevent chain reactions happening, when an Event is 
	processed during the EventSystem update(). And not during an 
	objects actual update time.
**/
public class EventController implements EventHandler
{
	private String[] wantedTypes = Event.ALL_EVENT_TYPES ;

	private final EventMessenger messenger = new EventMessenger() ;
	private final ArrayList<EventProcessor> processors = new ArrayList<EventProcessor>() ;
	private AddEventInterface addInterface = null ;

	public EventController() {}

	/**
		Add an Event Processor to begin reading the event stream.
	**/
	public void addEventProcessor( final EventProcessor _processor )
	{
		assert _processor != null ;
		processors.add( _processor ) ;
	}

	public void setAddEventInterface( final AddEventInterface _addInterface )
	{
		addInterface = _addInterface ;
	}
	
	/**
		Define what type of events this controller should be interested in.
	**/
	public void setWantedEventTypes( final String[] _types )
	{
		wantedTypes = _types ;
	}

	public int getProcessorSize()
	{
		return processors.size() ;
	}

	public int getEventSize()
	{
		return messenger.size() ;
	}

	/**
		Should not be overriden.
		Adds events to messenger and will process them at the appropriate time.
	**/
	public void processEvent( final Event _event )
	{
		messenger.addEvent( _event ) ;
	}

	/**
		Should be called during an update.
		Override useEvent() if you wish to use the Events passed in
	**/
	public void update()
	{
		messenger.refreshEvents() ;
		final int messengerSize = messenger.size() ;
		final int processorSize = processors.size() ;
		for( int i = 0; i < processorSize; ++i )
		{
			final EventProcessor proc = processors.get( i ) ;
			for( int j = 0; j < messengerSize; ++j )
			{
				proc.processEvent( messenger.getAt( j ) ) ;
			}
		}
	}

	/**
		Pass Event back to an EventSystem defined by addInterface.
	**/
	public void passEvent( final Event _event )
	{
		if( addInterface != null )
		{
			addInterface.addEvent( _event ) ;
		}
	}

	public AddEventInterface getAddEventInterface()
	{
		return addInterface ;
	}
	
	public String[] getWantedEventTypes()
	{
		return wantedTypes ;
	}
}