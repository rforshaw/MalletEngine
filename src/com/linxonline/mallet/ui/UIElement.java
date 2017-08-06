package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

/**
	Base class for all UI related systems.
	It can receive user input and event.

	The UIElement defines the confines that the developer is restricted to
	when implementing their custom listeners.

	UIElement does not directly handle the visual display 
	that is delegated to a UIListener and the developer is expected 
	to implement it in whatever way they see fit.
	
*/
public class UIElement implements InputHandler
{
	private final static float DEFAULT_MARGIN_SIZE = 5.0f ;		// In pixels

	private final ListenerUnit<IBase<? extends UIElement>> listeners = new ListenerUnit<IBase<? extends UIElement>>() ;
	private final List<Event<?>> events = MalletList.<Event<?>>newList() ;

	protected State current = State.NEUTRAL ;

	public boolean destroy = false ;
	public boolean visible = true ;

	private boolean dirty = true ;			// Causes refresh when true
	private int layer = 0 ;

	private final UIRatio ratio = UIRatio.getGlobalUIRatio() ;	// <pixels:unit>

	private final Vector3 minLength = new Vector3() ;	// In pixels
	private final Vector3 maxLength = new Vector3() ;	// In pixels
	private final Vector3 position ;					// In pixels
	private final Vector3 offset ;						// In pixels
	private final Vector3 length ;						// In pixels
	private final Vector3 margin ;						// In pixels

	public enum State
	{
		NEUTRAL,		// Element does not have focus
		ENGAGED,		// Element has focus
		CHILD_ENGAGED	// A child has focus within the element
	}

	public UIElement()
	{
		this( new Vector3(), new Vector3(), new Vector3() ) ;
	}

	public UIElement( final Vector3 _position, final Vector3 _offset, final Vector3 _length )
	{
		position = _position ;
		offset = _offset ;
		length = _length ;

		final float ratioMargin = DEFAULT_MARGIN_SIZE ;
		margin = new Vector3( ratioMargin, ratioMargin, ratioMargin ) ;
	}

	/**
		Return the Draw objects that this UIElement wishes 
		to render to the rendering system.
	*/
	public void passDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world )
	{
		final List<IBase<? extends UIElement>> base = listeners.getListeners() ;
		final int size = base.size() ;
		for( int i = 0; i < size; i++ )
		{
			base.get( i ).passDrawDelegate( _delegate, _world ) ;
		}
	}

	/**
		Add an event to the event list.
		This list will be polled on the next elements 
		update, the events will eventually find their 
		way to the Game State event-system if using UIComponent. 
	*/
	public void addEvent( final Event<?> _event )
	{
		events.add( _event ) ;
	}

	/**
		Add a listener to the UIElement.
		Caution: BaseListeners not designed for the elements 
		sub-type can still be added, not caught at compile-time.
	*/
	public <T extends IBase> T addListener( final T _listener )
	{
		if( listeners.add( _listener ) == true )
		{
			_listener.setParent( this ) ;
		}
		return _listener ;
	}

	/**
		Remove the listener from the UIElement.
		return true if the listener was removed else 
		return false.
	*/
	public <T extends IBase<? extends UIElement>> boolean removeListener( final T _listener )
	{
		if( listeners.remove( _listener ) == true )
		{
			_listener.setParent( null ) ;
			return true ;
		}
		return false ;
	}

	/**
		An element can flag itself for destruction.
		If it is contained by a UILayout or UIComponent,
		then it will be removed and it will be shutdown 
		and cleared.
	*/
	public void destroy()
	{
		destroy = true ;
	}

	/**
		Flag the UIElement as being engaged.
	*/
	public void engage()
	{
		current = State.ENGAGED ;
		final List<IBase<? extends UIElement>> base = listeners.getListeners() ;
		final int size = base.size() ;
		for( int i = 0; i < size; i++ )
		{
			base.get( i ).engage() ;
		}
	}

	/**
		Flag the UIElement as being disengaged.
	*/
	public void disengage()
	{
		current = State.NEUTRAL ;
		final List<IBase<? extends UIElement>> base = listeners.getListeners() ;
		final int size = base.size() ;
		for( int i = 0; i < size; i++ )
		{
			base.get( i ).disengage() ;
		}
	}

	public void setEngage( final boolean _engaged )
	{
		if( _engaged == true )
		{
			engage() ;
		}
		else
		{
			disengage() ;
		}
	}

	/**
		Inform the caller whether the element is engaged.
		If a child element is engaged then the parent 
		should be considered engaged too.
	*/
	public boolean isEngaged()
	{
		return current == State.ENGAGED ||
			   current == State.CHILD_ENGAGED ;
	}

	public void update( final float _dt, final List<Event<?>> _events )
	{
		if( events.isEmpty() == false )
		{
			_events.addAll( events ) ;
			events.clear() ;
		}

		if( isDirty() == true )
		{
			refresh() ;
			dirty = false ;
		}
	}

	/**
		Do not call directly, call makeDirty() instead.
		Implement refresh() to update the visual elements 
		of the UIElement. Refresh is called when isDirty() 
		returns true.
	*/
	public void refresh()
	{
		listeners.refresh() ;
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		if( isIntersectInput( _event ) == false )
		{
			// A UIElement should only pass the InputEvent 
			// to its listeners if the input is intersecting 
			// else we run the risk of doing pointless processing.
			return InputEvent.Action.PROPAGATE ;
		}

		switch( _event.getInputType() )
		{
			case MOUSE_MOVED       : return updateListeners( listeners.getListeners(), mouseMoveAction, _event ) ;
			case MOUSE1_PRESSED    :
			case MOUSE2_PRESSED    :
			case MOUSE3_PRESSED    : return updateListeners( listeners.getListeners(), mousePressedAction, _event ) ;
			case MOUSE1_RELEASED   :
			case MOUSE2_RELEASED   :
			case MOUSE3_RELEASED   : return updateListeners( listeners.getListeners(), mouseReleasedAction, _event ) ;
			case TOUCH_MOVE        : return updateListeners( listeners.getListeners(), touchMoveAction, _event ) ;
			case TOUCH_DOWN        : return updateListeners( listeners.getListeners(), touchPressedAction, _event ) ;
			case TOUCH_UP          : return updateListeners( listeners.getListeners(), touchReleasedAction, _event ) ;
			case GAMEPAD_RELEASED  :
			case KEYBOARD_RELEASED : return updateListeners( listeners.getListeners(), keyReleasedAction, _event ) ;
			case GAMEPAD_PRESSED   :
			case KEYBOARD_PRESSED  : return updateListeners( listeners.getListeners(), keyPressedAction, _event ) ;
			case GAMEPAD_ANALOGUE  : return updateListeners( listeners.getListeners(), analogueMoveAction, _event ) ;
		}

		return InputEvent.Action.PROPAGATE ;
	}

	private static InputEvent.Action updateListeners( final List<IBase<? extends UIElement>> _base,
													  final InputAction _action,
													  final InputEvent _event )
	{
		final int size = _base.size() ;
		for( int i = 0; i < size; i++ )
		{
			if( _action.action( _base.get( i ), _event ) == InputEvent.Action.CONSUME )
			{
				return InputEvent.Action.CONSUME ;
			}
		}

		return InputEvent.Action.PROPAGATE ;
	}

	public boolean isIntersectInput( final InputEvent _event )
	{
		final Camera camera = CameraAssist.getDefaultCamera() ;
		return intersectPoint( CameraAssist.convertInputToUICameraX( camera, _event.mouseX ),
							   CameraAssist.convertInputToUICameraY( camera, _event.mouseY ) ) ;
	}

	/**
		Expected in pixels.
	*/
	public boolean intersectPoint( final float _x, final float _y, final float _z )
	{
		final float zMin = position.z + offset.z ;
		final float zMax = position.z + offset.z + length.z ;

		if( intersectPoint( _x, _y ) == true )
		{
			if( _z >= zMin && _z <= zMax )
			{
				return true ;
			}
		}

		return false ;
	}

	/**
		Expected in pixels.
	*/
	public boolean intersectPoint( final float _x, final float _y )
	{
		final float xMin = position.x + offset.x ;
		final float xMax = position.x + offset.x + length.x ;

		final float yMin = position.y + offset.y ;
		final float yMax = position.y + offset.y + length.y ;

		if( _x >= xMin && _x <= xMax )
		{
			if( _y >= yMin && _y <= yMax )
			{
				return true ;
			}
		}

		return false ;
	}

	/**
		Inform the UIElement that is should update its 
		visual elements.
	*/
	public void makeDirty()
	{
		dirty = true ;
	}

	public void setVisible( final boolean _visibility )
	{
		if( visible != _visibility )
		{
			visible = _visibility ;
			makeDirty() ;
		}
	}

	/**
		Set the UIElement's absolute position.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.
	*/
	public void setPosition( final float _x, final float _y, final float _z )
	{
		makeDirty() ;
		position.setXYZ( ratio.toPixelX( _x ), ratio.toPixelY( _y ), ratio.toPixelZ( _z ) ) ;
		//System.out.println( "Position: " + position ) ;
	}

	/**
		Set the UIElement's offset from position.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.
	*/
	public void setOffset( final float _x, final float _y, final float _z )
	{
		makeDirty() ;
		offset.setXYZ( ratio.toPixelX( _x ), ratio.toPixelY( _y ), ratio.toPixelZ( _z ) ) ;
		//System.out.println( "Offset: " + offset ) ;
	}

	/**
		Set the UIElement's minimum length, min size of the element.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.
	*/
	public void setMinimumLength( final float _x, final float _y, final float _z )
	{
		minLength.x = ( _x < 0.0f ) ? 0.0f : ratio.toPixelX( _x ) ;
		minLength.y = ( _y < 0.0f ) ? 0.0f : ratio.toPixelY( _y ) ;
		minLength.z = ( _z < 0.0f ) ? 0.0f : ratio.toPixelZ( _z ) ;

		// Ensure that length adheres to the new minimum length
		setLength( ratio.toUnitX( length.x ),
				   ratio.toUnitY( length.y ),
				   ratio.toUnitZ( length.z ) ) ;
	}

	/**
		Set the UIElement's maximum length, max size of the element.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.
	*/
	public void setMaximumLength( final float _x, final float _y, final float _z )
	{
		maxLength.x = ( _x < 0.0f ) ? 0.0f : ratio.toPixelX( _x ) ;
		maxLength.y = ( _y < 0.0f ) ? 0.0f : ratio.toPixelY( _y ) ;
		maxLength.z = ( _z < 0.0f ) ? 0.0f : ratio.toPixelZ( _z ) ;

		// Ensure that length adheres to the new maximum length
		setLength( ratio.toUnitX( length.x ),
				   ratio.toUnitY( length.y ),
				   ratio.toUnitZ( length.z ) ) ;
	}

	/**
		Set the UIElement's length, actual size of the element.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.
	*/
	public void setLength( float _x, float _y, float _z )
	{
		makeDirty() ;
		_x = ratio.toPixelX( _x ) ;
		_y = ratio.toPixelY( _y ) ;
		_z = ratio.toPixelZ( _z ) ;

		length.x = ( _x < minLength.x ) ? minLength.x : _x ;
		length.y = ( _y < minLength.y ) ? minLength.y : _y ;
		length.z = ( _z < minLength.z ) ? minLength.z : _z ;

		if( maxLength.x > 0.0f )
		{
			length.x = ( _x > maxLength.x ) ? maxLength.x : _x ;
		}

		if( maxLength.y > 0.0f )
		{
			length.y = ( _y > maxLength.y ) ? maxLength.y : _y ;
		}

		if( maxLength.z > 0.0f )
		{
			length.z = ( _z > maxLength.z ) ? maxLength.z : _z ;
		}

		//System.out.println( "Length: " + length ) ;
	}

	/**
		Set the UIElement's margin, the spacing before the next 
		UIElement is displayed.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.
	*/
	public void setMargin( final float _x, final float _y, final float _z )
	{
		makeDirty() ;
		margin.setXYZ( ratio.toPixelX( _x ), ratio.toPixelY( _y ), ratio.toPixelZ( _z ) ) ;
	}

	public void setLayer( final int _layer )
	{
		layer = _layer ;
	}

	public boolean isDirty()
	{
		return dirty ;
	}

	public boolean isVisible()
	{
		return visible ;
	}
	
	/**
		Returns the elements position in pixels.
		Pass in a Vector3 to retrieve the position in units.
	*/
	public Vector3 getPosition( final Vector3 _unit )
	{
		if( _unit != null )
		{
			ratio.toUnit( getPosition(), _unit ) ;
		}

		return getPosition() ;
	}

	/**
		Returns the element's position in pixels.
	*/
	public Vector3 getPosition()
	{
		return position ;
	}

	/**
		Returns the elements offset in pixels.
		Pass in a Vector3 to retrieve the offset in units.
	*/
	public Vector3 getOffset( final Vector3 _unit )
	{
		if( _unit != null )
		{
			ratio.toUnit( getOffset(), _unit ) ;
		}

		return getOffset() ;
	}

	/**
		Return the element's offset in pixels.
	*/
	public Vector3 getOffset()
	{
		return offset ;
	}

	/**
		Returns the elements maximum length in pixels.
		Pass in a Vector3 to retrieve the maximum length in units.
	*/
	public Vector3 getMaximumLength( final Vector3 _unit )
	{
		if( _unit != null )
		{
			ratio.toUnit( getMaximumLength(), _unit ) ;
		}

		return getMaximumLength() ;
	}

	/**
		Return the element's potential maximum length in pixels.
	*/
	public Vector3 getMaximumLength()
	{
		return maxLength ;
	}

	/**
		Returns the elements minimum length in pixels.
		Pass in a Vector3 to retrieve the minimum length in units.
	*/
	public Vector3 getMinimumLength( final Vector3 _unit )
	{
		if( _unit != null )
		{
			ratio.toUnit( getMinimumLength(), _unit ) ;
		}

		return getMinimumLength() ;
	}

	/**
		Return the element's potential minimum length in pixels.
	*/
	public Vector3 getMinimumLength()
	{
		return minLength ;
	}

	/**
		Returns the elements length in pixels.
		Pass in a Vector3 to retrieve the length in units.
	*/
	public Vector3 getLength( final Vector3 _unit )
	{
		if( _unit != null )
		{
			ratio.toUnit( getLength(), _unit ) ;
		}

		return getLength() ;
	}

	/**
		Return the element's actual length in pixels.
	*/
	public Vector3 getLength()
	{
		return length ;
	}

	/**
		Returns the elements margin in pixels.
		Pass in a Vector3 to retrieve the margin in units.
	*/
	public Vector3 getMargin( final Vector3 _unit )
	{
		if( _unit != null )
		{
			ratio.toUnit( getMargin(), _unit ) ;
		}

		return getMargin() ;
	}

	/**
		Return the elements margin around itself in pixels.
	*/
	public Vector3 getMargin()
	{
		return margin ;
	}

	public int getLayer()
	{
		return layer ;
	}

	public UIElement.State getState()
	{
		return current ;
	}

	public UIRatio getRatio()
	{
		return ratio ;
	}

	/**
		Inform the UIElement it needs to release any 
		resources or handlers it may have acquired.
	*/
	public void shutdown()
	{
		listeners.shutdown() ;
	}

	/**
		Blank out any content it may be retaining.
	*/
	public void clear()
	{
		listeners.clear() ;
		events.clear() ;
	}

	/**
		Reset the UIElement as if it has just been 
		constructed.
	*/
	@Override
	public void reset()
	{
		position.setXYZ( 0.0f, 0.0f, 0.0f ) ;
		offset.setXYZ( 0.0f, 0.0f, 0.0f ) ;
		length.setXYZ( 0.0f, 0.0f, 0.0f ) ;
		margin.setXYZ( DEFAULT_MARGIN_SIZE, DEFAULT_MARGIN_SIZE, DEFAULT_MARGIN_SIZE ) ;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder() ;
		builder.append( "[" ) ;
		builder.append( "Visible: " ) ;
		builder.append( isVisible() ) ;
		builder.append( " Engaged: " ) ;
		builder.append( isEngaged() ) ;
		builder.append( "]" ) ;
		return builder.toString() ;
	}

	private interface InputAction
	{
		public InputEvent.Action action( final IBase _listener, final InputEvent _event ) ;
	}
	
	private static final InputAction mouseMoveAction = new InputAction()
	{
		@Override
		public InputEvent.Action action( final IBase _listener, final InputEvent _event )
		{
			return _listener.mouseMove( _event ) ;
		}
	} ;

	private static final InputAction mousePressedAction = new InputAction()
	{
		@Override
		public InputEvent.Action action( final IBase _listener, final InputEvent _event )
		{
			return _listener.mousePressed( _event ) ;
		}
	} ;

	private static final InputAction mouseReleasedAction = new InputAction()
	{
		@Override
		public InputEvent.Action action( final IBase _listener, final InputEvent _event )
		{
			return _listener.mouseReleased( _event ) ;
		}
	} ;

	private static final InputAction touchMoveAction = new InputAction()
	{
		@Override
		public InputEvent.Action action( final IBase _listener, final InputEvent _event )
		{
			return _listener.touchMove( _event ) ;
		}
	} ;

	private static final InputAction touchPressedAction = new InputAction()
	{
		@Override
		public InputEvent.Action action( final IBase _listener, final InputEvent _event )
		{
			return _listener.touchPressed( _event ) ;
		}
	} ;

	private static final InputAction touchReleasedAction = new InputAction()
	{
		@Override
		public InputEvent.Action action( final IBase _listener, final InputEvent _event )
		{
			return _listener.touchReleased( _event ) ;
		}
	} ;

	private static final InputAction keyReleasedAction = new InputAction()
	{
		@Override
		public InputEvent.Action action( final IBase _listener, final InputEvent _event )
		{
			return _listener.keyReleased( _event ) ;
		}
	} ;

	private static final InputAction keyPressedAction = new InputAction()
	{
		@Override
		public InputEvent.Action action( final IBase _listener, final InputEvent _event )
		{
			return _listener.keyPressed( _event ) ;
		}
	} ;

	private static final InputAction analogueMoveAction = new InputAction()
	{
		@Override
		public InputEvent.Action action( final IBase _listener, final InputEvent _event )
		{
			return _listener.analogueMove( _event ) ;
		}
	} ;

	public static class UV
	{
		public final Vector2 min ;
		public final Vector2 max ;

		public UV()
		{
			this( new Vector2( 0.0f, 0.0f ), new Vector2( 1.0f, 1.0f ) ) ;
		}

		public UV( final Vector2 _min, final Vector2 _max )
		{
			min = _min ;
			max = _max ;
		}
	}
}
