package com.linxonline.mallet.entity.components ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.audio.* ;

import com.linxonline.mallet.ui.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;

public class UIComponent extends InputComponent
{
	private final List<UIElement> elements = MalletList.<UIElement>newList() ;
	private final List<UIElement> toRemove = MalletList.<UIElement>newList() ;

	private final List<Draw> toAddBasic = MalletList.<Draw>newList() ;
	private final List<Draw> toAddText = MalletList.<Draw>newList() ;

	private final World world ;
	private final List<Event<?>> events = MalletList.<Event<?>>newList() ;

	protected final EventController eventController = new EventController( id.toString() ) ;
	private Component.ReadyCallback toDestroy = null ;
	private DrawDelegate<World, Draw> delegate = null ;

	public UIComponent()
	{
		this( "UI", "UICOMPONENT", InputMode.UI, WorldAssist.getDefaultWorld() ) ;
	}

	public UIComponent( final String _name )
	{
		this( _name, "UICOMPONENT", InputMode.UI, WorldAssist.getDefaultWorld() ) ;
	}

	public UIComponent( final String _name, final String _group )
	{
		this( _name, _group, InputMode.UI, WorldAssist.getDefaultWorld() ) ;
	}

	public UIComponent( final String _name, final InputMode _mode )
	{
		this( _name, "UICOMPONENT", _mode, WorldAssist.getDefaultWorld() ) ;
	}

	public UIComponent( final String _name,
						final String _group,
						final InputMode _mode,
						final World _world )
	{
		super( _name, _group, _mode ) ;

		// Allow the developer to specify what world 
		// this UI should be drawn to - if no world 
		// is specified we will assume its the default.
		world = ( _world != null ) ? _world : WorldAssist.getDefaultWorld() ;
	}

	public <T extends UIElement> T addElement( final T _element )
	{
		if( elements.contains( _element ) == false )
		{
			elements.add( _element ) ;
		}
		return _element ;
	}

	/**
		Called when parent is flagged for destruction.
		Will remove all UIElements from component.
	*/
	@Override
	public void readyToDestroy( final Component.ReadyCallback _callback )
	{
		if( delegate != null )
		{
			delegate.shutdown() ;
		}

		for( final UIElement element : elements )
		{
			removeElement( element ) ;
		}

		toDestroy = _callback ;
	}

	/**
		Removing an element will shut it down.
		Removing any assigned resources and blanking 
		it out.
	*/
	public void removeElement( final UIElement _element )
	{
		if( toRemove.contains( _element ) == false )
		{
			toRemove.add( _element ) ;
		}
	}

	@Override
	public void update( final float _dt )
	{
		super.update( _dt ) ;
		if( elements.isEmpty() == false )
		{
			final int size = elements.size() ;
			for( int i = 0; i < size; i++ )
			{
				final UIElement element = elements.get( i ) ;
				element.update( _dt, events ) ;
				if( element.destroy == true )
				{
					removeElement( element ) ;
				}
			}
		}

		if( toRemove.isEmpty() == false )
		{
			final int size = toRemove.size() ;
			for( int i = 0; i < size; i++ )
			{
				final UIElement element = toRemove.get( i ) ;
				if( elements.remove( element ) == true )
				{
					element.shutdown() ;
					element.clear() ;
				}
			}
			toRemove.clear() ;
		}

		if( events.isEmpty() == false )
		{
			final int size = events.size() ;
			for( int i = 0; i < size; i++ )
			{
				final Event event = events.get( i ) ;
				eventController.passEvent( event ) ;
			}
			events.clear() ;
		}

		eventController.update() ;

		if( toDestroy != null )
		{
			toDestroy.ready( this ) ;
		}
	}

	@Override
	public void passInitialEvents( final List<Event<?>> _events )
	{
		super.passInitialEvents( _events ) ;
		_events.add( new Event<EventController>( "ADD_GAME_STATE_EVENT", eventController ) ) ;
		_events.add( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
		{
			public void callback( final DrawDelegate<World, Draw> _delegate )
			{
				if( delegate != null )
				{
					// Don't call shutdown(), we don't want to 
					// clean anything except an existing DrawDelegate.
					delegate.shutdown() ;
				}

				delegate = _delegate ;

				final int size = elements.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = elements.get( i ) ;
					element.passDrawDelegate( delegate, world ) ;
				}
			}
		} ) ) ;
	}

	@Override
	public void passFinalEvents( final List<Event<?>> _events )
	{
		super.passFinalEvents( _events ) ;
		_events.add( new Event<EventController>( "REMOVE_GAME_STATE_EVENT", eventController )  ) ;
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		if( super.passInputEvent( _event ) == InputEvent.Action.CONSUME )
		{
			return InputEvent.Action.CONSUME ;
		}

		final int size = elements.size() ;
		for( int i = 0; i < size; i++ )
		{
			final UIElement element = elements.get( i ) ;
			if( element.passInputEvent( _event ) == InputEvent.Action.CONSUME )
			{
				return InputEvent.Action.CONSUME ;
			}
		}

		return InputEvent.Action.PROPAGATE ;
	}
}
