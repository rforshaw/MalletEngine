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
	private final List<Event<?>> events = MalletList.<Event<?>>newList() ;

	protected final EventController eventController = new EventController( id.toString() ) ;
	private Component.ReadyCallback toDestroy = null ;

	public UIComponent()
	{
		super( "UI", "UICOMPONENT", InputMode.UI ) ;
	}

	public UIComponent( final String _name )
	{
		this( _name, "UICOMPONENT", InputMode.UI ) ;
	}

	public UIComponent( final String _name, final String _group )
	{
		this( _name, _group, InputMode.UI ) ;
	}

	public UIComponent( final String _name, final InputMode _mode )
	{
		this( _name, "UICOMPONENT", _mode ) ;
	}

	public UIComponent( final String _name, final String _group, final InputMode _mode )
	{
		super( _name, _group, _mode ) ;
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
