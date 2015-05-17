package com.linxonline.mallet.event ;

import java.util.ArrayList ;

import com.linxonline.mallet.io.save.SaveClass ;
import com.linxonline.mallet.io.save.Reference ;
import com.linxonline.mallet.io.save.NoSave ;

import com.linxonline.mallet.util.logger.Logger ;

/**
	This is a convience class to reduce Events being processed 
	at the wrong time.

	An Event that changes the state should process it's content 
	at the right moment to prevent crashes or unusual behaviour.

	This is to prevent chain reactions happening, when an Event is 
	processed during the EventSystem update(). And not during an 
	objects actual update time.
**/
public @SaveClass class EventController implements EventHandler
{
	private final ArrayList<EventType> wantedTypes = new ArrayList<EventType>() ;

	private final String name ;
	private @NoSave final EventMessenger messenger = new EventMessenger() ;
	private final ArrayList<EventProcessor> processors = new ArrayList<EventProcessor>() ;
	private @Reference AddEventInterface addInterface = null ;

	public EventController()
	{
		name = "EVENT CONTROLLER" ;
	}

	public EventController( final String _name )
	{
		assert _name != null ;
		name = _name ;
	}

	/**
		Add an Event Processor to begin reading the event stream.
	**/
	public void addEventProcessor( final EventProcessor _processor )
	{
		assert _processor != null ;
		processors.add( _processor ) ;
		wantedTypes.add( _processor.getEventType() ) ;
	}

	public void setAddEventInterface( final AddEventInterface _addInterface )
	{
		addInterface = _addInterface ;
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
	public void processEvent( final Event<?> _event )
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
				proc.passEvent( messenger.getAt( j ) ) ;
			}
		}
	}

	/**
		Pass Event back to an EventSystem defined by addInterface.
	**/
	public void passEvent( final Event<?> _event )
	{
		if( addInterface == null )
		{
			Logger.println( "AddInterface not set in " + getName() + ".", Logger.Verbosity.MAJOR ) ;
			return ;
		}

		addInterface.addEvent( _event ) ;
	}

	public AddEventInterface getAddEventInterface()
	{
		return addInterface ;
	}

	@Override
	public String getName()
	{
		return name ;
	}

	public ArrayList<EventType> getWantedEventTypes()
	{
		return wantedTypes ;
	}
}