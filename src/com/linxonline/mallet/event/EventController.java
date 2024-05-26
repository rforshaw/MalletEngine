package com.linxonline.mallet.event ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Tuple ;

/**
	This is a convience class to reduce Events being processed 
	at the wrong time.

	An Event that changes the state should process it's content 
	at the right moment to prevent crashes or unusual behaviour.

	This is to prevent chain reactions happening, when an Event is 
	processed during the EventSystem update(). And not during an 
	objects actual update time.
**/
public class EventController implements IEventController
{
	private final static IProcessor<Object> PROCESSOR_FALLBACK = ( Object _obj ) -> {} ;

	private final int eventCapacity ;

	private final AddEventFallback ADD_EVENT_FALLBACK = new AddEventFallback() ;

	private final List<EventType> wantedTypes ;
	private final EventType.Lookup<IProcessor<?>> processors ;
	private List<Event<?>> messenger ;
	private IAddEvent addInterface = ADD_EVENT_FALLBACK ;

	public EventController()
	{
		this( 5, 5 ) ;
	}

	public EventController( final Tuple<String, IProcessor<?>> ... _processors )
	{
		this( 10, _processors ) ;
	}

	public EventController( final int _eventCapacity, final Tuple<String, IProcessor<?>> ... _processors )
	{
		this( _processors.length, _eventCapacity ) ;
		for( final Tuple<String, IProcessor<?>> processor : _processors )
		{
			final EventType type = EventType.get( processor.getLeft() ) ;
			if( wantedTypes.contains( type ) == false )
			{
				wantedTypes.add( type ) ;
			}
			processors.add( type, processor.getRight() ) ;
		}
	}

	public EventController( final int _processorCapacity, final int _eventCapacity )
	{
		eventCapacity = _eventCapacity ;

		processors = new EventType.Lookup<IProcessor<?>>( _processorCapacity, PROCESSOR_FALLBACK ) ;
		wantedTypes = MalletList.<EventType>newList( _processorCapacity ) ;
		messenger = MalletList.<Event<?>>newList( eventCapacity ) ;
	}

	public final <T> void addProcessor( final String _type, final IProcessor<T> _processor )
	{
		final EventType type = EventType.get( _type ) ;
		if( wantedTypes.contains( type ) == false )
		{
			wantedTypes.add( type ) ;
		}
		processors.add( type, _processor ) ;
	}

	/**
		Called when added to an EventSystem.
		If the Event Controller is not added to an EventSystem then 
		any events will be added to the ADD_EVENT_FALLBACK mechanism.
		If an EventSystem is added the FALLBACK is removed - if the 
		addInterface is reset to a null state the FALLBACK is reintroduced.
	*/
	@Override
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
	@Override
	public final void processEvent( final Event<?> _event )
	{
		messenger.add( _event ) ;
	}

	/**
		Should be called during an update.
		Override processEvent() if you wish to use the Events passed in
	*/
	@Override
	public void update()
	{
		if( messenger.isEmpty() )
		{
			return ;
		}

		final int size = messenger.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Event<?> event = messenger.get( i ) ;
			final IProcessor proc = processors.get( event.getEventType() ) ;
			proc.process( event.getVariable() ) ;
		}
		messenger.clear() ;

		if( size > eventCapacity )
		{
			messenger = MalletList.<Event<?>>newList( eventCapacity ) ;
		}
	}

	/**
		Pass Event back to an EventSystem defined by addInterface.
	**/
	@Override
	public void passEvent( final Event<?> _event )
	{
		addInterface.addEvent( _event ) ;
	}

	@Override
	public void clearEvents()
	{
		messenger.clear() ;
		ADD_EVENT_FALLBACK.clear() ;
	}

	@Override
	public List<EventType> getWantedEventTypes( List<EventType> _fill )
	{
		_fill.addAll( wantedTypes ) ;
		return _fill ;
	}

	public static Tuple<String, IProcessor<?>> create( final String _name, final IProcessor<?> _processor )
	{
		return Tuple.<String, IProcessor<?>>build( _name, _processor ) ;
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
			if( events.isEmpty() )
			{
				return ;
			}

			final int size = events.size() ;
			for( int i = 0; i < size; i++ )
			{
				final Event<?> event = events.get( i ) ;
				_addInterface.addEvent( event ) ;
			}
			clear() ;
		}

		@Override
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
