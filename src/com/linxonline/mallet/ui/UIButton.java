package com.linxonline.mallet.ui ;

import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.UpdateType ;

import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.MalletColour ;

import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.World ;

import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.Program ;

import com.linxonline.mallet.audio.AudioDelegate ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

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
	private final Connect.Signal pressed = new Connect.Signal() ;
	private final Connect.Signal released = new Connect.Signal() ;

	/**
		If the UIButton is being added to a UILayout
		then you don't have to define the position, 
		offset, or length.
	*/
	public UIButton()
	{
		this( new Vector3(), new Vector3(), new Vector3(), null ) ;
	}

	public UIButton( final Vector3 _length )
	{
		this( new Vector3(), new Vector3(), _length, null ) ;
	}

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
					 final ABase<UIButton> _listener )
	{
		super( _position, _offset, _length ) ;
		init() ;
		addListener( _listener ) ;
	}

	private void init()
	{
		addListener( new InputListener<UIButton>()
		{
			@Override
			public InputEvent.Action touchReleased( final InputEvent _input )
			{
				return mouseReleased( _input ) ;
			}

			@Override
			public InputEvent.Action touchPressed( final InputEvent _input )
			{
				return mousePressed( _input ) ;
			}

			@Override
			public InputEvent.Action mouseReleased( final InputEvent _input )
			{
				final UIButton parent = getParent() ;
				UIElement.signal( parent, parent.released() ) ;
				return InputEvent.Action.CONSUME ;
			}

			@Override
			public InputEvent.Action mousePressed( final InputEvent _input )
			{
				final UIButton parent = getParent() ;
				UIElement.signal( parent, parent.pressed() ) ;
				return InputEvent.Action.CONSUME ;
			}
		} ) ;
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		if( isIntersectInput( _event ) == true )
		{
			final InputEvent.Action action = processInputEvent( _event ) ;
			switch( _event.getInputType() )
			{
				case MOUSE_MOVED :
				case TOUCH_MOVE  : return InputEvent.Action.PROPAGATE ;
				default          : return action ;
			}
		}

		return InputEvent.Action.PROPAGATE ;
	}

	public Connect.Signal pressed()
	{
		return pressed ;
	}

	public Connect.Signal released()
	{
		return released ;
	}
}
