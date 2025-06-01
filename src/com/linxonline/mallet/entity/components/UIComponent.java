package com.linxonline.mallet.entity.components ;

import java.util.List ;

import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.ui.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;

public class UIComponent extends InputComponent
{
	private final List<UIElement> elements = MalletList.<UIElement>newList() ;
	private final List<UIElement> toRemove = MalletList.<UIElement>newList() ;

	private final World world ;
	private final Camera camera ;
	private final List<Event<?>> events = MalletList.<Event<?>>newList() ;

	private Entity.ReadyCallback toDestroy = null ;

	private boolean ctrl = false ;

	public UIComponent( final Entity _parent )
	{
		this( _parent, Entity.AllowEvents.YES ) ;
	}

	public UIComponent( final Entity _parent,
						final Entity.AllowEvents _allow )
	{
		this( _parent, _allow, InputMode.UI ) ;
	}

	public UIComponent( final Entity _parent,
						final Entity.AllowEvents _allow,
						final InputMode _mode )
	{
		this( _parent,
			  _allow,
			  _mode,
			  WorldAssist.getDefault(),
			  CameraAssist.getDefault() ) ;
	}

	public UIComponent( final Entity _parent,
						final Entity.AllowEvents _allow,
						final InputMode _mode,
						final World _world,
						final Camera _camera )
	{
		super( _parent, _allow, _mode ) ;

		// Allow the developer to specify what world 
		// this UI should be drawn to - if no world 
		// is specified we will assume its the default.
		world = ( _world != null ) ? _world : WorldAssist.getDefault() ;
		camera = ( _camera != null ) ? _camera : CameraAssist.getDefault() ;
		initEventProcessor() ;
	}

	/**
		Add _element to UIComponent using the elements 
		layer to determine order.
		An element with a higher layer will be updated 
		first, for example an element with layer 100 
		will be processed first compared to an element 
		with a layer of 10.
	*/
	public <T extends UIElement> T addElement( final T _element )
	{
		final int layer = _element.getLayer() ;
		final int size = elements.size() ;
		for( int i = 0; i < size; i++ )
		{
			if( layer >= elements.get( i ).getLayer() )
			{
				return addElement( i, _element ) ;
			}
		}

		return addElement( elements.size(), _element ) ;
	}

	private <T extends UIElement> T addElement( final int _index, final T _element )
	{
		if( elements.contains( _element ) == false )
		{
			elements.add( _index, _element ) ;
			_element.setWorldAndCamera( getWorld(), getCamera() ) ;
		}
		return _element ;
	}

	protected void initEventProcessor() {}

	/**
		Called when parent is flagged for destruction.
		Will remove all UIElements from component.
	*/
	@Override
	public void readyToDestroy( final Entity.ReadyCallback _callback )
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

		updateElements( _dt ) ;
		removeElements() ;
		updateEvents() ;

		if( toDestroy != null )
		{
			toDestroy.ready( this ) ;
		}
	}

	/**
		Update the UI elements added to this UIComponent.
		Any elements flagged for destruction add them to 
		the removal list.
	*/
	protected void updateElements( final float _dt )
	{
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
	}

	/**
		Remove all elements that have been added to 
		the removal list - any elemts added will be 
		shutdown and cleared.
	*/
	protected void removeElements()
	{
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
	}

	/**
		Take any events from the elements and pass 
		them through to the game-state. 
	*/
	protected void updateEvents()
	{
		if( events.isEmpty() == false )
		{
			Event.addEvents( events ) ;
			events.clear() ;
		}
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		if( super.passInputEvent( _event ) == InputEvent.Action.CONSUME )
		{
			return InputEvent.Action.CONSUME ;
		}

		// We'll manage the global undo/redo in the UIComponent.
		// This should probably be moved into a central UI system.
		switch( _event.getInputType() )
		{
			default               : break ;
			case KEYBOARD_PRESSED :
			{
				switch( _event.getKeyCode() )
				{
					default   :
					{
						if( ctrl == false )
						{
							break ;
						}

						switch( _event.getKeyCharacter() )
						{
							default  : break ;
							case 'Y' :
								UI.redo() ;
								return InputEvent.Action.CONSUME ;
							case 'Z' :
								UI.undo() ;
								return InputEvent.Action.CONSUME ;
						}
						break ;
					}
					case CTRL : ctrl = true ; break ;
				}
				break ;
			}
			case KEYBOARD_RELEASED :
			{
				switch( _event.getKeyCode() )
				{
					default   : break ;
					case CTRL : ctrl = false ; break ;
				}
				break ;
			}
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

	public World getWorld()
	{
		return world ;
	}

	public Camera getCamera()
	{
		return camera ;
	}
}
