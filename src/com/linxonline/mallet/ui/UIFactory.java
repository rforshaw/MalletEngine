package com.linxonline.mallet.ui ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.renderer.DrawFactory ;
import com.linxonline.mallet.renderer.Shape ;

import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.util.settings.Settings ;

/**
	Contains helper functions for the construction of 
	commonly used UI elements and their supporting classes.
*/
public class UIFactory
{
	/**
		Used in conjunction with constructButtonDraw and constructButtonListener.
		Add EventProcessor to the Component-EventController of a RenderComponent.
	*/
	public static EventProcessor<Tuple<Event, String>> constructButtonEventProcessor()
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
		Used in conjunction with constructButtonEventProcessor and constructButtonListener.
		Add the Event to a RenderComponent, this ensures the event is handled correctly.
		Pass the Event when calling constructButtonListener. One Event per UIButton.
		_callback will most likely be a RenderComponent.
	*/
	public static Event<Settings> constructButtonDraw( final String _neutral,
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
		Used in conjunction with constructButtonEventProcessor and constructButtonDraw.
		Attach the listener to a designated button.
	*/
	public static UIButton.Listener constructButtonListener( final Event<Settings> _draw,
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
}