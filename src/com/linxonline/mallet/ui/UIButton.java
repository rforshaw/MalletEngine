package com.linxonline.mallet.ui ;

import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.UpdateType ;

import com.linxonline.mallet.renderer.MalletFont ;

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
		addListener( _listener ) ;
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		if( isIntersectInput( _event ) == true )
		{
			processInputEvent( _event ) ;
			switch( _event.getInputType() )
			{
				case MOUSE_MOVED :
				case TOUCH_MOVE  : return InputEvent.Action.PROPAGATE ;
				default          : return InputEvent.Action.CONSUME ;
			}
		}

		return InputEvent.Action.PROPAGATE ;
	}

	public static GUIDraw createGUIBasic( final MalletTexture _sheet,
											final UIButton.UV _neutral,
											final UIButton.UV _rollover,
											final UIButton.UV _clicked )
	{
		return new GUIDraw( _sheet, _neutral, _rollover, _clicked ) ;
	}

	public static class GUIDraw extends UIFactory.GUIDraw<UIButton>
	{
		private final UIButton.UV neutral ;
		private final UIButton.UV rollover ;
		private final UIButton.UV clicked ;

		public GUIDraw( final MalletTexture _sheet,
						 final UIButton.UV _neutral,
						 final UIButton.UV _rollover,
						 final UIButton.UV _clicked )
		{
			super( _sheet, _neutral ) ;
			neutral = _neutral ;
			rollover = _rollover ;
			clicked = _clicked ;
		}

		@Override
		public InputEvent.Action mouseReleased( final InputEvent _input )
		{
			Shape.updatePlaneUV( DrawAssist.getDrawShape( getDraw() ), rollover.min, rollover.max ) ;
			DrawAssist.forceUpdate( getDraw() ) ;
			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public InputEvent.Action touchReleased( final InputEvent _input )
		{
			return mousePressed( _input ) ;
		}

		@Override
		public InputEvent.Action mousePressed( final InputEvent _input )
		{
			Shape.updatePlaneUV( DrawAssist.getDrawShape( getDraw() ), clicked.min, clicked.max ) ;
			DrawAssist.forceUpdate( getDraw() ) ;
			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public InputEvent.Action touchPressed( final InputEvent _input )
		{
			return mousePressed( _input ) ;
		}

		@Override
		public void engage()
		{
			Shape.updatePlaneUV( DrawAssist.getDrawShape( getDraw() ), rollover.min, rollover.max ) ;
			DrawAssist.forceUpdate( getDraw() ) ;
		}

		@Override
		public void disengage()
		{
			Shape.updatePlaneUV( DrawAssist.getDrawShape( getDraw() ), neutral.min, neutral.max ) ;
			DrawAssist.forceUpdate( getDraw() ) ;
		}
	}
}
