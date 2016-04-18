package com.linxonline.mallet.ui ;

import java.util.ArrayList ;

import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.UpdateType ;

import com.linxonline.mallet.renderer.MalletFont ;

import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Shape ;

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
					 final Listener _listener )
	{
		super( _position, _offset, _length ) ;
		addListener( _listener ) ;
	}

	/**
		Add a UIButton.Listener to kick-off an event.
		clicked()  - Called when user clicks on button.
		neutral()  - Called when pointer is not over button.
		rollover() - Called when pointer has moved over button.
	*/
	public void addListener( final Listener _listener )
	{
		if( _listener != null )
		{
			if( listeners.contains( _listener ) == false )
			{
				listeners.add( _listener ) ;
				_listener.setParent( this ) ;
			}
		}
	}

	/**
		Update all of the Button listeners.
	*/
	@Override
	public void refresh()
	{
		final int size = listeners.size() ;
		for( int i = 0; i < size; i++ )
		{
			listeners.get( i ).refresh() ;
		}
	}

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

	/**
		Inform all attached listeners that the UIButton 
		has moved into a neutral state.
		Caused when the mouse is not hovering over the button. 
	*/
	private void neutral( final InputEvent _event )
	{
		for( final Listener listener : listeners )
		{
			listener.neutral( _event ) ;
		}
	}

	/**
		Inform all attached listeners that the UIButton 
		has moved into the rollover state.
		Caused when the mouse has began hovering over the button. 
	*/
	private void rollover( final InputEvent _event )
	{
		for( final Listener listener : listeners )
		{
			listener.rollover( _event ) ;
		}
	}

	/**
		Inform all attached listeners that the UIButton 
		has moved into a click state.
		Caused when the mouse is over the button and has 
		been clicked, state will then revert back to rollover 
		or neutral after click. 
	*/
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
		listeners.clear() ;
	}

	/**
		Cleanup any resources, handlers that the listeners 
		may have acquired.
	*/
	@Override
	public void shutdown()
	{
		for( final Listener listener : listeners )
		{
			listener.shutdown() ;
		}
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

	public static UIListener constructUIListener( final MalletTexture _sheet,
												  final UIButton.UV _neutral,
												  final UIButton.UV _rollover,
												  final UIButton.UV _clicked )
	{
		return new UIListener( _sheet, _neutral, _rollover, _clicked ) ;
	}

	public static class UV
	{
		public final Vector2 min ;
		public final Vector2 max ;

		public UV( final Vector2 _min, final Vector2 _max )
		{
			min = _min ;
			max = _max ;
		}
	}

	public static class UIListener extends Listener
	{
		private final MalletTexture sheet ;
		private final UIButton.UV neutral ;
		private final UIButton.UV rollover ;
		private final UIButton.UV clicked ;

		private DrawDelegate delegate = null ;
		private Draw draw = null ;

		public UIListener( final MalletTexture _sheet,
						   final UIButton.UV _neutral,
						   final UIButton.UV _rollover,
						   final UIButton.UV _clicked )
		{
			sheet = _sheet ;
			neutral = _neutral ;
			rollover = _rollover ;
			clicked = _clicked ;
		}

		@Override
		public void setParent( final UIElement _parent )
		{
			super.setParent( _parent ) ;
			_parent.addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
			{
				public void callback( DrawDelegate _delegate )
				{
					delegate = _delegate ;
					delegate.addBasicDraw( draw ) ;
				}
			} ) ) ;

			draw = DrawAssist.createDraw( _parent.getPosition(),
										  _parent.getOffset(),
										  new Vector3(),
										  new Vector3( 1, 1, 1 ), _parent.getLayer() ) ;
			DrawAssist.amendUI( draw, true ) ;
			DrawAssist.amendTexture( draw, sheet ) ;
			DrawAssist.amendShape( draw, Shape.constructPlane( _parent.getLength(), neutral.min, neutral.max ) ) ;
			DrawAssist.attachProgram( draw, "SIMPLE_TEXTURE" ) ;
		}

		@Override
		public void clicked( final InputEvent _event )
		{
			Shape.updatePlaneUV( DrawAssist.getDrawShape( draw ), clicked.min, clicked.max ) ;
			DrawAssist.forceUpdate( draw ) ;
		}

		@Override
		public void rollover( final InputEvent _event )
		{
			Shape.updatePlaneUV( DrawAssist.getDrawShape( draw ), rollover.min, rollover.max ) ;
			DrawAssist.forceUpdate( draw ) ;
		}

		@Override
		public void neutral( final InputEvent _event )
		{
			Shape.updatePlaneUV( DrawAssist.getDrawShape( draw ), neutral.min, neutral.max ) ;
			DrawAssist.forceUpdate( draw ) ;
		}

		@Override
		public void refresh()
		{
			Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), getParent().getLength() ) ;
			DrawAssist.forceUpdate( draw ) ;
		}

		@Override
		public void shutdown()
		{
			if( delegate != null )
			{
				delegate.shutdown() ;
			}
		}
	}

	public static abstract class Listener extends BaseListener
	{
		public abstract void clicked( final InputEvent _event ) ;
		public abstract void rollover( final InputEvent _event ) ;
		public abstract void neutral( final InputEvent _event ) ;

		public abstract void refresh() ;
	}
}