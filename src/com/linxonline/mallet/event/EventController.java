package com.linxonline.mallet.event ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

/**
	This is a convience class to reduce Events being processed 
	at the wrong time.

	An Event that changes the state should process it's content 
	at the right moment to prevent crashes or unusual behaviour.

	This is to prevent chain reactions happening, when an Event is 
	processed during the EventSystem update(). And not during an 
	objects actual update time.
**/
public class EventController implements IEventHandler
{
	private final List<EventType> wantedTypes = MalletList.<EventType>newList() ;
	private final AddEventFallback ADD_EVENT_FALLBACK = new AddEventFallback() ;

	private final String name ;
	private final SwapList<Event<?>> messenger = new SwapList<Event<?>>() ;
	private final List<EventProcessor> processors = MalletList.<EventProcessor>newList() ;
	private IAddEvent addInterface = ADD_EVENT_FALLBACK ;

	public EventController()
	{
		this( null ) ;
	}

	public EventController( final String _name )
	{
		name = ( _name != null ) ? _name : "EVENT CONTROLLER" ;
	}

	public <T> void addProcessor( final String _type, final IProcessor<T> _processor )
	{
		addProcessor( _type, _type, _processor ) ;
	}

	public <T> void addProcessor( final String _name, final String _type, final IProcessor<T> _processor )
	{
		addEventProcessor( new EventProcessor<T>( _name, _type )
		{
			@Override
			public void processEvent( final Event<T> _event )
			{
				_processor.process( _event.getVariable() ) ;
			}
		} ) ;
	}

	/**
		Add an Event Processor to begin reading the event stream.
		The event controller is considered the parent of the processor.
	*/
	public void addEventProcessor( final EventProcessor _processor )
	{
		if( _processor == null )
		{
			Logger.println( "Attempting to add null processor to controller.", Logger.Verbosity.MAJOR ) ;
			return ;
		}

		if( processors.contains( _processor ) == false )
		{
			processors.add( _processor ) ;
			final EventType type = _processor.getEventType() ;
			if( wantedTypes.contains( type ) == false )
			{
				wantedTypes.add( type ) ;
			}
		}
	}

	public void setAddEventInterface( final IAddEvent _addInterface )
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
		messenger.add( _event ) ;
	}

	/**
		Should be called during an update.
		Override processEvent() if you wish to use the Events passed in
	*/
	public void update()
	{
		final List<Event<?>> events = messenger.swap() ;
		if( events.isEmpty() )
		{
			return ;
		}

		final int eventsSize = events.size() ;
		final int processorSize = processors.size() ;
		for( int i = 0; i < processorSize; ++i )
		{
			final EventProcessor<?> proc = processors.get( i ) ;
			for( int j = 0; j < eventsSize; ++j )
			{
				proc.passEvent( events.get( j ) ) ;
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
		messenger.clear() ;
		ADD_EVENT_FALLBACK.clear() ;
	}

	public IAddEvent getAddEventInterface()
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
	private static class AddEventFallback implements IAddEvent
	{
		private final List<Event<?>> events = MalletList.<Event<?>>newList() ;

		public void transferEvents( final IAddEvent _addInterface )
		{
			if( events.isEmpty() == false )
			{
				final int size = events.size() ;
				for( int i = 0; i < size; i++ )
				{
					final Event<?> event = events.get( i ) ;
					_addInterface.addEvent( event ) ;
				}
				clear() ;
			}
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

	@FunctionalInterface
	public interface IProcessor<T>
	{
		public void process( final T _variable ) ;
	}
}
