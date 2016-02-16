package com.linxonline.mallet.ui ;

import java.util.ArrayList ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.renderer.DrawFactory ;
import com.linxonline.mallet.renderer.Shape ;

import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.util.settings.Settings ;

/**
	Used to determine if the user has acted within a UI area.

	When the user has clicked, rolled over, or the location 
	is reset to a neutral position an event defined by the 
	developer is sent through the entity's event-system.

	The event can then be picked up by other components such as a 
	render-component to modify the visual element of the entity.
*/
public class UIButton extends UIElement
{
	private final ArrayList<Listener> listeners = new ArrayList<Listener>() ;
	private final Vector2 mouse = new Vector2() ;
	private State current = State.NEUTRAL ;

	private enum State
	{
		NEUTRAL,
		ROLLOVER,
		CLICKED
	}

	/**
		If the UIButton is being added to a UILayout
		then you don't have to define the position.
	*/
	public UIButton( final Vector3 _offset,
					 final Vector3 _length )
	{
		this( new Vector3(), _offset, _length, null ) ;
	}

	public UIButton( final Vector3 _position,
					 final Vector3 _offset,
					 final Vector3 _length )
	{
		this( _position, _offset, _length, null ) ;
	}

	public UIButton( final Vector3 _position,
					 final Vector3 _offset,
					 final Vector3 _length,
					 final Listener _listener )
	{
		super() ;
		setPosition( _position.x, _position.y, _position.z ) ;
		setOffset( _offset.x, _offset.y, _offset.z ) ;
		setLength( _length.x, _length.y, _length.z ) ;
	}

	/**
		Add a UIButton.Listener to kick-off an event.
		clicked()  - Called when user clicks on button.
		neutral()  - Called when pointer is not over button.
		rollover() - Called when pointer has moved over button.
	*/
	public void addListener( final Listener _listener )
	{
		if( _listener != null && listeners.contains( _listener ) == false )
		{
			listeners.add( _listener ) ;
			_listener.setEventController( getEventController() ) ;
		}
	}

	@Override
	public void setEventController( final EventController _controller )
	{
		super.setEventController( _controller ) ;
		for( final Listener listener : listeners )
		{
			listener.setEventController( getEventController() ) ;
		}
	}

	@Override
	public void update( final float _dt ) {}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		final InputType type = _event.getInputType() ;
		switch( type )
		{
			case MOUSE_MOVED : 
			case TOUCH_MOVE  : updateMousePosition( _event ) ; break ;
		}

		if( intersectPoint( mouse.x, mouse.y ) == true )
		{
			switch( type )
			{
				case MOUSE1_PRESSED :
				{
					if( current != State.CLICKED )
					{
						clicked( _event ) ;
						current = State.CLICKED ;
						return InputEvent.Action.CONSUME ;
					}
					break ;
				}
				case MOUSE_MOVED :
				{
					if( current != State.ROLLOVER )
					{
						rollover( _event ) ;
						current = State.ROLLOVER ;
					}
					return InputEvent.Action.PROPAGATE ;
				}
			}
		}

		if( current != State.NEUTRAL )
		{
			neutral( _event ) ;
			current = State.NEUTRAL ;
		}

		return InputEvent.Action.PROPAGATE ;
	}

	private void neutral( final InputEvent _event )
	{
		for( final Listener listener : listeners )
		{
			listener.neutral( _event ) ;
		}
	}

	private void rollover( final InputEvent _event )
	{
		for( final Listener listener : listeners )
		{
			listener.rollover( _event ) ;
		}
	}

	private void clicked( final InputEvent _event )
	{
		for( final Listener listener : listeners )
		{
			listener.clicked( _event ) ;
		}
	}

	public void updateMousePosition( final InputEvent _event )
	{
		final InputAdapterInterface adapter = getInputAdapter() ;
		if( adapter != null )
		{
			mouse.x = adapter.convertInputToUIRenderX( _event.mouseX ) ;
			mouse.y = adapter.convertInputToUIRenderY( _event.mouseY ) ;
		}
	}

	@Override
	public void clear()
	{
		super.clear() ;
		// setEventController( null ) in super will null out listeners
		// Ensuring no memory leaks occur, when listeners are cleared.
		listeners.clear() ;
	}

	/**
		Retains listeners on a reset.
		position, offset, and length are reset.
	*/
	@Override
	public void reset()
	{
		super.reset() ;
	}

	/**
		Used in conjunction with constructBasicDrawButton and constructBasicListener.
		Add EventProcessor to the Component-EventController of a RenderComponent.
	*/
	public static EventProcessor<Tuple<Event, String>> constructBasicEventProcessor()
	{
		return new EventProcessor<Tuple<Event,String>>( "RENDER_BUTTON", "BUTTON" )
		{
			public void processEvent( final Event<Tuple<Event,String>> _event )
			{
				final Tuple<Event,String> tuple = _event.getVariable() ;
				final Event event = tuple.getLeft() ;

				DrawFactory.amendTexture( event, tuple.getRight() ) ;
				DrawFactory.forceUpdate( event ) ;
			}
		} ;
	}

	/**
		Used in conjunction with constructBasicEventProcessor and constructBasicListener.
		Add the Event to a RenderComponent, this ensures the event is handled correctly.
		Pass the Event when calling constructBasicListener. One Event per UIButton.
		_callback will most likely be a RenderComponent.
	*/
	public static Event<Settings> constructBasicDrawButton( final String _neutral,
															final UIButton _button,
															final IDInterface _callback )
	{
		final Shape shape = Shape.constructPlane( _button.getLength(), new Vector2(), new Vector2( 1, 1 ) ) ;
		final Event<Settings> event  = DrawFactory.amendGUI( DrawFactory.createTexture( _neutral,
																						shape,
																						_button.getPosition(),
																						_button.getOffset(),
																						10,
																						_callback ), true ) ;
		return event ;
	}

	/**
		Used in conjunction with constructBasicEventProcessor and constructBasicDrawButton.
		Attach the listener to a designated button.
	*/
	public static UIButton.Listener constructBasicListener( final Event<Settings> _draw,
															final String _neutral,
															final String _rollover,
															final String _clicked )
	{
		return new UIButton.Listener()
		{
			private final Tuple<Event,String> neutral = new Tuple<Event,String>( _draw, _neutral ) ;
			private final Tuple<Event,String> rollover = new Tuple<Event,String>( _draw, _rollover ) ;
			private final Tuple<Event,String> clicked = new Tuple<Event,String>( _draw, _clicked ) ;

			private final Event<Tuple<Event,String>> state = new Event<Tuple<Event,String>>( "BUTTON", neutral ) ;

			@Override
			public void clicked( final InputEvent _event )
			{
				state.setEvent( clicked ) ;
				sendEvent( state ) ;
			}

			@Override
			public void rollover( final InputEvent _event )
			{
				state.setEvent( rollover ) ;
				sendEvent( state ) ;
			}

			@Override
			public void neutral( final InputEvent _event )
			{
				state.setEvent( neutral ) ;
				sendEvent( state ) ;
			}
		} ;
	}

	public static abstract class Listener extends BaseListener
	{
		public abstract void clicked( final InputEvent _event ) ;
		public abstract void rollover( final InputEvent _event ) ;
		public abstract void neutral( final InputEvent _event ) ;
	}
}