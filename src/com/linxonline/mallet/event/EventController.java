package com.linxonline.mallet.event ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
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
public class EventController implements EventHandler
{
	private final List<EventType> wantedTypes = MalletList.<EventType>newList() ;
	private final AddEventFallback ADD_EVENT_FALLBACK = new AddEventFallback() ;

	private final String name ;
	private final EventMessenger messenger = new EventMessenger() ;
	private final List<EventProcessor> processors = MalletList.<EventProcessor>newList() ;
	private AddEventInterface addInterface = ADD_EVENT_FALLBACK ;

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

		final EventType type = _processor.getEventType() ;
		if( wantedTypes.contains( type ) == false )
		{
			wantedTypes.add( type ) ;
		}
	}

	public void setAddEventInterface( final AddEventInterface _addInterface )
	{
		if( _addInterface != null )
		{
			addInterface = _addInterface ;
			ADD_EVENT_FALLBACK.transferEvents( addInterface ) ;
		}
		else
		{
			addInterface = ADD_EVENT_FALLBACK ;
		}
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
		Override processEvent() if you wish to use the Events passed in
	**/
	public void update()
	{
		messenger.refreshEvents() ;
		final int messengerSize = messenger.size() ;
		final int processorSize = processors.size() ;
		for( int i = 0; i < processorSize; ++i )
		{
			final EventProcessor<?> proc = processors.get( i ) ;
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
		addInterface.addEvent( _event ) ;
	}

	@Override
	public void reset()
	{
		clearEvents() ;
		processors.clear() ;
		wantedTypes.clear() ;
		setAddEventInterface( null ) ;
	}
	
	public void clearEvents()
	{
		messenger.clearEvents() ;
		ADD_EVENT_FALLBACK.clear() ;
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

	public List<EventType> getWantedEventTypes()
	{
		return wantedTypes ;
	}

	/**
		Allows events to be passed to a controller without it 
		being added to an EventSystem.
		Once the controller is added to an Event System the 
		events should be passed to it.
	*/
	private static class AddEventFallback implements AddEventInterface
	{
		private final List<Event<?>> events = MalletList.<Event<?>>newList() ;

		public void transferEvents( final AddEventInterface _addInterface )
		{
			for( final Event<?> event : events )
			{
				_addInterface.addEvent( event ) ;
			}
			clear() ;
		}

		public void addEvent( final Event<?> _event )
		{
			events.add( _event ) ;
		}

		public void clear()
		{
			events.clear() ;
		}
	}
}
