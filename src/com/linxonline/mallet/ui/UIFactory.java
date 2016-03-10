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
		Used in conjunction with constructButtonListener and UIRenderComponent.
		Pass the Draw Event to constructButtonListener, will eventually be passed to UIRenderComponent.
	*/
	public static Event<Settings> constructButtonDraw( final String _neutral, final UIButton _button )
	{
		final Shape shape = Shape.constructPlane( _button.getLength(), new Vector2(), new Vector2( 1, 1 ) ) ;
		final Event<Settings> event  = DrawFactory.amendGUI( DrawFactory.createTexture( _neutral,
																						shape,
																						_button.getPosition(),
																						_button.getOffset(),
																						10,
																						null ), true ) ;
		return event ;
	}

	/**
		Used in conjunction with UIRenderComponent and constructButtonDraw.
		Attach the listener to a designated button.
		Draw Event is automatically passed to Entity's UIRenderComponent.
		Button Listener updates Draw Event when state changes.
	*/
	public static UIButton.Listener constructButtonListener( final Event<Settings> _draw,
															 final String _neutral,
															 final String _rollover,
															 final String _clicked )
	{
		DrawFactory.amendTexture( _draw, _neutral ) ;

		return new UIButton.Listener()
		{
			@Override
			public void clicked( final InputEvent _event )
			{
				DrawFactory.amendTexture( _draw, _clicked ) ;
				DrawFactory.forceUpdate( _draw ) ;
			}

			@Override
			public void rollover( final InputEvent _event )
			{
				DrawFactory.amendTexture( _draw, _rollover ) ;
				DrawFactory.forceUpdate( _draw ) ;
			}

			@Override
			public void neutral( final InputEvent _event )
			{
				DrawFactory.amendTexture( _draw, _neutral ) ;
				DrawFactory.forceUpdate( _draw ) ;
			}

			@Override
			public void setParent( final UIElement _parent )
			{
				super.setParent( _parent ) ;
				sendEvent( new Event<Event<Settings>>( "ADD_UI_DRAW", _draw ) ) ;
			}
		} ;
	}
}