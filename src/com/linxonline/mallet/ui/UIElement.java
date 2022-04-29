package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.id.ID ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

/**
	Base class for all UI related systems.
	It can receive user input and event.

	The UIElement defines the confines that the developer is restricted to
	when implementing their custom components.

	UIElement does not directly handle the visual display 
	that is delegated to a GUIComponent and the developer is expected 
	to implement it in whatever way they see fit.
*/
public class UIElement implements IInputHandler, Connect.Connection
{
	private final static float DEFAULT_MARGIN_SIZE = 5.0f ;		// In pixels

	private final ComponentUnit components = new ComponentUnit() ;
	private final List<Event<?>> events = MalletList.<Event<?>>newList() ;
	private final static Connect connect = new Connect() ;

	protected State current = State.NEUTRAL ;

	public boolean destroy = false ;
	public boolean visible = true ;
	public boolean disabled = false ;

	private boolean dirty = true ;			// Causes refresh when true
	private int layer = 0 ;

	private World world ;
	private Camera camera = CameraAssist.getDefault() ;
	private final UIRatio ratio = UIRatio.getGlobalUIRatio() ;	// <pixels:unit>

	private final Vector3 minLength = new Vector3() ;	// In pixels
	private final Vector3 maxLength = new Vector3() ;	// In pixels
	private final Vector3 position = new Vector3()  ;	// In pixels
	private final Vector3 offset = new Vector3() ;		// In pixels
	private final Vector3 length = new Vector3() ;		// In pixels
	private final Vector3 margin ;						// In pixels

	private final Connect.Signal positionChanged   = new Connect.Signal() ;
	private final Connect.Signal offsetChanged     = new Connect.Signal() ;
	private final Connect.Signal lengthChanged     = new Connect.Signal() ;
	private final Connect.Signal minLengthChanged  = new Connect.Signal() ;
	private final Connect.Signal maxLengthChanged  = new Connect.Signal() ;
	private final Connect.Signal marginChanged     = new Connect.Signal() ;
	private final Connect.Signal elementDestroyed  = new Connect.Signal() ;
	private final Connect.Signal elementShown      = new Connect.Signal() ;
	private final Connect.Signal elementHidden     = new Connect.Signal() ;
	private final Connect.Signal layerChanged      = new Connect.Signal() ;
	private final Connect.Signal elementEngaged    = new Connect.Signal() ;
	private final Connect.Signal elementDisengaged = new Connect.Signal() ;
	private final Connect.Signal elementEnabled    = new Connect.Signal() ;
	private final Connect.Signal elementDisabled   = new Connect.Signal() ;
	private final Connect.Signal elementShutdown   = new Connect.Signal() ;
	private final Connect.Signal elementClear      = new Connect.Signal() ;
	private final Connect.Signal elementReset      = new Connect.Signal() ;

	private InputAction scrollAction        = DEFAULT_SCROLL_ACTION ;
	private InputAction mouseMoveAction     = DEFAULT_MOUSE_MOVE_ACTION ;
	private InputAction mousePressedAction  = DEFAULT_MOUSE_PRESSED_ACTION ;
	private InputAction mouseReleasedAction = DEFAULT_MOUSE_RELEASED_ACTION ;
	private InputAction touchMoveAction     = DEFAULT_TOUCH_MOVE_ACTION ;
	private InputAction touchPressedAction  = DEFAULT_TOUCH_PRESSED_ACTION ;
	private InputAction touchReleasedAction = DEFAULT_TOUCH_RELEASED_ACTION ;
	private InputAction keyReleasedAction   = DEFAULT_KEY_RELEASED_ACTION ;
	private InputAction keyPressedAction    = DEFAULT_KEY_PRESSED_ACTION ;
	private InputAction analogueMoveAction  = DEFAULT_ANALOGUE_MOVE_ACTION ;

	public enum State
	{
		NEUTRAL,		// Element does not have focus
		ENGAGED,		// Element has focus
		CHILD_ENGAGED	// A child has focus within the element
	}

	public UIElement()
	{
		final float ratioMargin = DEFAULT_MARGIN_SIZE ;
		margin = new Vector3( ratioMargin, ratioMargin, ratioMargin ) ;
	}

	/**
		Return the Draw objects that this UIElement wishes 
		to render to the rendering system.
	*/
	public void setWorldAndCamera( final World _world, final Camera _camera )
	{
		world = _world ;
		camera = ( _camera != null ) ? _camera : camera ;

		final List<UIElement.Component> base = components.getComponents() ;
		final int size = base.size() ;
		for( int i = 0; i < size; i++ )
		{
			base.get( i ).setWorld( _world ) ;
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
		Add a component to the UIElement.
	*/
	private <T extends UIElement.Component> T addComponent( final T _component )
	{
		return addComponent( 0, _component ) ;
	}

	/**
		Add a component to the UIElement.

		Inserts the component at the specified index, shifts existing 
		components to the right. 
	*/
	private <T extends UIElement.Component> T addComponent( final int _index, final T _component )
	{
		components.add( _index, _component ) ;
		return _component ;
	}

	/**
		Remove the component from the UIElement.
		return true if the component was removed else 
		return false.
	*/
	private <T extends UIElement.Component> boolean removeComponent( final T _component )
	{
		return components.remove( _component ) ;
	}

	public <T extends UIElement.Component> T getComponent( final String _group, final String _name, final Class<T> _class )
	{
		final List<UIElement.Component> comps = components.getComponents() ;
		final int size = comps.size() ;
		for( int i = 0; i < size; i++ )
		{
			final UIElement.Component comp = comps.get( i ) ;
			if( comp.isGroup( _group ) )
			{
				if( comp.isName( _name ) )
				{
					return _class.cast( comp ) ;
				}
			}
		}

		return null ;
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
		UIElement.signal( this, elementDestroyed() ) ;
	}

	/**
		Flag the UIElement as being engaged.
		Only UIElements that are not disabled can be flagged as engaged.
	*/
	public void engage()
	{
		if( current != State.ENGAGED && isDisabled() == false )
		{
			current = State.ENGAGED ;
			UIElement.signal( this, elementEngaged() ) ;
		}
	}

	/**
		Flag the UIElement as being disengaged.
	*/
	public void disengage()
	{
		if( current != State.NEUTRAL )
		{
			current = State.NEUTRAL ;
			UIElement.signal( this, elementDisengaged() ) ;
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
	protected void refresh()
	{
		components.refresh() ;
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		if( isDisabled() == true )
		{
			return InputEvent.Action.PROPAGATE ;
		}

		switch( _event.getInputType() )
		{
			case SCROLL_WHEEL      : 
			case KEYBOARD_PRESSED  :
			case KEYBOARD_RELEASED : return processInputEvent( _event ) ;
			default                :
			{
				if( isIntersectInput( _event ) == false )
				{
					// A UIElement should only pass the InputEvent 
					// to its components if the input is intersecting 
					// else we run the risk of doing pointless processing.
					return InputEvent.Action.PROPAGATE ;
				}
				return processInputEvent( _event ) ;
			}
		}
	}

	protected InputEvent.Action processInputEvent( final InputEvent _event )
	{
		switch( _event.getInputType() )
		{
			case SCROLL_WHEEL      : return updateListeners( components.getComponents(), scrollAction, _event ) ;
			case MOUSE_MOVED       : return updateListeners( components.getComponents(), mouseMoveAction, _event ) ;
			case MOUSE1_PRESSED    :
			case MOUSE2_PRESSED    :
			case MOUSE3_PRESSED    : return updateListeners( components.getComponents(), mousePressedAction, _event ) ;
			case MOUSE1_RELEASED   :
			case MOUSE2_RELEASED   :
			case MOUSE3_RELEASED   : return updateListeners( components.getComponents(), mouseReleasedAction, _event ) ;
			case TOUCH_MOVE        : return updateListeners( components.getComponents(), touchMoveAction, _event ) ;
			case TOUCH_DOWN        : return updateListeners( components.getComponents(), touchPressedAction, _event ) ;
			case TOUCH_UP          : return updateListeners( components.getComponents(), touchReleasedAction, _event ) ;
			case GAMEPAD_RELEASED  :
			case KEYBOARD_RELEASED : return updateListeners( components.getComponents(), keyReleasedAction, _event ) ;
			case GAMEPAD_PRESSED   :
			case KEYBOARD_PRESSED  : return updateListeners( components.getComponents(), keyPressedAction, _event ) ;
			case GAMEPAD_ANALOGUE  : return updateListeners( components.getComponents(), analogueMoveAction, _event ) ;
			default                : return InputEvent.Action.PROPAGATE ;
		}
	}

	private static InputEvent.Action updateListeners( final List<UIElement.Component> _base,
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
		return isIntersectInput( _event, getCamera() ) ;
	}

	protected boolean isIntersectInput( final InputEvent _event, final Camera _camera )
	{
		return intersectPoint( _camera.convertInputToUIX( _event.mouseX ),
							   _camera.convertInputToUIY( _event.mouseY ) ) ;
	}

	/**
		Expected in pixels.
	*/
	public boolean intersectPoint( final float _x, final float _y, final float _z )
	{
		final float zMin = position.z + offset.z ;
		final float zMax = zMin + length.z ;

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
		final float xMax = xMin + length.x ;

		final float yMin = position.y + offset.y ;
		final float yMax = yMin + length.y ;

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
		Inform the UIElement that it should update its 
		visual elements.
	*/
	public void makeDirty()
	{
		dirty = true ;
	}

	/**
		Set the element to be visible or not.

		Causes element to be flagged as dirty.
	*/
	public void setVisible( final boolean _visibility )
	{
		if( visible != _visibility )
		{
			visible = _visibility ;

			final Connect.Signal signal = ( visible == true ) ? elementShown() : elementHidden() ;
			UIElement.signal( this, signal ) ;
			makeDirty() ;
		}
	}

	/**
		Enable the UIElement allow it to accept input.

		Causes element to be flagged as dirty.
	*/
	public void enable()
	{
		if( disabled == true )
		{
			disabled = false ;
			UIElement.signal( this, elementEnabled() ) ;
			makeDirty() ;
		}
	}

	/**
		Disable the UIElement from accepting input.
		It should not acknowledge any user modifications.
		A UIElement that is visible is still affected by 
		layout adjustments.

		Causes element to be flagged as dirty.
	*/
	public void disable()
	{
		if( disabled == false )
		{
			disabled = true ;
			disengage() ;
			UIElement.signal( this, elementDisabled() ) ;
			makeDirty() ;
		}
	}

	/**
		Set the UIElement's absolute position.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.

		Causes element to be flagged as dirty.
	*/
	public void setPosition( final float _x, final float _y, final float _z )
	{
		if( UI.applyVec3( position, ratio.toPixelX( _x ), ratio.toPixelY( _y ), ratio.toPixelZ( _z ) ) == true )
		{
			UIElement.signal( this, positionChanged() ) ;
			makeDirty() ;
		}
	}

	/**
		Set the UIElement's offset from position.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.

		Causes element to be flagged as dirty.
	*/
	public void setOffset( final float _x, final float _y, final float _z )
	{
		if( UI.applyVec3( offset, ratio.toPixelX( _x ), ratio.toPixelY( _y ), ratio.toPixelZ( _z ) ) == true )
		{
			UIElement.signal( this, offsetChanged() ) ;
			makeDirty() ;
		}
	}

	/**
		Set the UIElement's minimum length, min size of the element.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.

		Causes element to be flagged as dirty if the length requires to 
		be changed to be above new minimum range.
	*/
	public void setMinimumLength( float _x, float _y, float _z )
	{
		_x = ( _x < 0.0f ) ? 0.0f : ratio.toPixelX( _x ) ;
		_y = ( _y < 0.0f ) ? 0.0f : ratio.toPixelY( _y ) ;
		_z = ( _z < 0.0f ) ? 0.0f : ratio.toPixelZ( _z ) ;

		if( UI.applyVec3( minLength, _x, _y, _z ) == true )
		{
			makeDirty() ;
			UIElement.signal( this, minLengthChanged() ) ;

			// Ensure that length adheres to the new minimum length
			setLength( ratio.toUnitX( length.x ),
					   ratio.toUnitY( length.y ),
					   ratio.toUnitZ( length.z ) ) ;
		}
	}

	/**
		Set the UIElement's maximum length, max size of the element.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.

		Causes element to be flagged as dirty if the length requires to 
		be changed to be within new maximum range.
	*/
	public void setMaximumLength( float _x, float _y, float _z )
	{
		_x = ( _x < 0.0f ) ? 0.0f : ratio.toPixelX( _x ) ;
		_y = ( _y < 0.0f ) ? 0.0f : ratio.toPixelY( _y ) ;
		_z = ( _z < 0.0f ) ? 0.0f : ratio.toPixelZ( _z ) ;

		if( UI.applyVec3( maxLength, _x, _y, _z ) == true )
		{
			makeDirty() ;
			UIElement.signal( this, maxLengthChanged() ) ;

			// Ensure that length adheres to the new maximum length
			setLength( ratio.toUnitX( length.x ),
					   ratio.toUnitY( length.y ),
					   ratio.toUnitZ( length.z ) ) ;
		}
	}

	/**
		Set the UIElement's length, actual size of the element.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.

		Causes element to be flagged as dirty.
	*/
	public void setLength( float _x, float _y, float _z )
	{
		_x = ratio.toPixelX( _x ) ;
		_y = ratio.toPixelY( _y ) ;
		_z = ratio.toPixelZ( _z ) ;

		_x = ( _x < minLength.x ) ? minLength.x : _x ;
		_y = ( _y < minLength.y ) ? minLength.y : _y ;
		_z = ( _z < minLength.z ) ? minLength.z : _z ;

		if( maxLength.x > 0.0f )
		{
			_x = ( _x > maxLength.x ) ? maxLength.x : _x ;
		}

		if( maxLength.y > 0.0f )
		{
			_y = ( _y > maxLength.y ) ? maxLength.y : _y ;
		}

		if( maxLength.z > 0.0f )
		{
			_z = ( _z > maxLength.z ) ? maxLength.z : _z ;
		}

		if( UI.applyVec3( length, _x, _y, _z ) == true )
		{
			makeDirty() ;
			UIElement.signal( this, lengthChanged() ) ;
		}
	}

	/**
		Set the UIElement's margin, the spacing before the next 
		UIElement is displayed.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.

		Causes element to be flagged as dirty.
	*/
	public void setMargin( final float _x, final float _y, final float _z )
	{
		if( UI.applyVec3( margin, ratio.toPixelX( _x ), ratio.toPixelY( _y ), ratio.toPixelZ( _z ) ) == true )
		{
			UIElement.signal( this, marginChanged() ) ;
			makeDirty() ;
		}
	}

	/**
		Set the layer that visual elements are expected to 
		be placed on.

		Causes element to be flagged as dirty.
	*/
	public void setLayer( final int _layer )
	{
		if( layer != _layer )
		{
			layer = _layer ;
			UIElement.signal( this, layerChanged() ) ;
			makeDirty() ;
		}
	}

	public void setScrollAction( final InputAction _action )
	{
		scrollAction = ( _action != null ) ? _action : DEFAULT_SCROLL_ACTION ;
	} ;

	public void setMouseMoveAction( final InputAction _action )
	{
		mouseMoveAction = ( _action != null ) ? _action : DEFAULT_MOUSE_MOVE_ACTION ;
	} ;

	public void setMousePressedAction( final InputAction _action )
	{
		mousePressedAction = ( _action != null ) ? _action : DEFAULT_MOUSE_PRESSED_ACTION ;
	} ;

	public void setMouseReleasedAction( final InputAction _action )
	{
		mouseReleasedAction = ( _action != null ) ? _action : DEFAULT_MOUSE_RELEASED_ACTION ;
	} ;

	public void setTouchMoveAction( final InputAction _action )
	{
		touchMoveAction = ( _action != null ) ? _action : DEFAULT_TOUCH_MOVE_ACTION ;
	} ;

	public void setTouchPressedAction( final InputAction _action )
	{
		touchPressedAction = ( _action != null ) ? _action : DEFAULT_TOUCH_PRESSED_ACTION ;
	} ;

	public void setTouchReleasedAction( final InputAction _action )
	{
		touchReleasedAction = ( _action != null ) ? _action : DEFAULT_TOUCH_RELEASED_ACTION ;
	} ;

	public void setKeyReleasedAction( final InputAction _action )
	{
		keyReleasedAction = ( _action != null ) ? _action : DEFAULT_KEY_RELEASED_ACTION ;
	} ;

	public void setKeyPressedAction( final InputAction _action )
	{
		keyPressedAction = ( _action != null ) ? _action : DEFAULT_KEY_PRESSED_ACTION ;
	} ;

	public void setAnalogueMoveAction( InputAction _action )
	{
		analogueMoveAction = ( _action != null ) ? _action : DEFAULT_ANALOGUE_MOVE_ACTION ;
	}
	
	/**
		Returns true if the elements considers itself 
		requiring a need to be refreshed.

		You can dirty an element by calling makeDirty() 
		or calling a function that makes the element dirty.
	*/
	public boolean isDirty()
	{
		return dirty ;
	}

	public boolean isVisible()
	{
		return visible ;
	}

	public boolean isDisabled()
	{
		return disabled ;
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
		Return the world that draw objects are expected to 
		be displayed in.
	*/
	public World getWorld()
	{
		return world ;
	}

	/**
		Return the camera that this UI is expected to be 
		displayed on - used to convert inputs to the 
		correct co-ordinate system.
	*/
	public Camera getCamera()
	{
		return camera ;
	}

	/**
		Returns the components owned by this UIElement.
	*/
	protected ComponentUnit getComponentUnit()
	{
		return components ;
	}

	/**
		Inform the UIElement and Listeners it needs to release 
		any resources or handlers it may have acquired.
	*/
	public void shutdown()
	{
		UIElement.signal( this, elementShutdown() ) ;
		components.shutdown() ;
		connect.disconnect( this ) ;
	}

	/**
		Clear out each of the systems.
		Remove all slots connected to signals.
		Remove all components - note call shutdown if they have 
		any resources attached.
		Remove any events that may be in the event stream.
	*/
	public void clear()
	{
		UIElement.signal( this, elementClear() ) ;
		components.clear() ;
		events.clear() ;
		UIElement.disconnect( this ) ;
	}

	/**
		Reset the UIElement as if it has just been constructed.
		This does not remove components or connections.
	*/
	public void reset()
	{
		UIElement.signal( this, elementReset() ) ;
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

	@Override
	public Connect getConnect()
	{
		return connect ;
	}

	/**
		Called when the position of the element 
		has been changed using setPosition().
	*/
	public Connect.Signal positionChanged()
	{
		return positionChanged ;
	}

	/**
		Called when the offset of the element 
		has been changed using setOffset().
	*/
	public Connect.Signal offsetChanged()
	{
		return offsetChanged ;
	}

	/**
		Called when the length of the element 
		has been changed using setLength().
	*/
	public Connect.Signal lengthChanged()
	{
		return lengthChanged ;
	}

	public Connect.Signal minLengthChanged()
	{
		return minLengthChanged ;
	}

	public Connect.Signal maxLengthChanged()
	{
		return maxLengthChanged ;
	}

	/**
		Called when the margin of the element 
		has been changed using setMargin().
	*/
	public Connect.Signal marginChanged()
	{
		return marginChanged ;
	}

	/**
		Called when the element has been flagged 
		for destruction and will be destroyed.
	*/
	public Connect.Signal elementDestroyed()
	{
		return elementDestroyed ;
	}

	/**
		Called when the element has been flagged 
		to be visible to the user.
	*/
	public Connect.Signal elementShown()
	{
		return elementShown ;
	}

	/**
		Called when the element has been flagged 
		to be invisible to the user.
	*/
	public Connect.Signal elementHidden()
	{
		return elementHidden ;
	}

	/**
		Called when the element has had its layer 
		change to a different value.
	*/
	public Connect.Signal layerChanged()
	{
		return layerChanged ;
	}

	/**
		Called when the element is considered 
		engaged with the user.
	*/
	public Connect.Signal elementEngaged()
	{
		return elementEngaged ;
	}

	/**
		Called when the element is considered 
		not engaged with the user.
	*/
	public Connect.Signal elementDisengaged()
	{
		return elementDisengaged ;
	}

	/**
		Called when the element is considered usable 
		by the user.
		An element that is enabled can be engaged.
	*/
	public Connect.Signal elementEnabled()
	{
		return elementEnabled ;
	}

	/**
		Called when the element is considered not 
		usable by the user.
		An element that is disabled cannot be engaged.
	*/
	public Connect.Signal elementDisabled()
	{
		return elementDisabled ;
	}

	/**
		Called when the shutdown operation is initiated.
	*/
	public Connect.Signal elementShutdown()
	{
		return elementShutdown ;
	}

	/**
		Called when the clear operation is initiated.
	*/
	public Connect.Signal elementClear()
	{
		return elementClear ;
	}

	/**
		Called when the reset operation is initiated.
	*/
	public Connect.Signal elementReset()
	{
		return elementReset ;
	}

	/**
		Connect the slot to the signal, a signal may contain 
		multiple data-points if any of those data points change 
		the slot will be informed of the change.

		Signals or Variables can not be immutable - for example String.
	*/
	public static <T extends Connect.Connection> Connect.Slot<T> connect( final T _element, final Connect.Signal _signal, final Connect.Slot<T> _slot )
	{
		final Connect connect = _element.getConnect() ;
		connect.connect( _element, _signal, _slot ) ;
		return _slot ;
	}

	/**
		Disconnect the specific slot from a particular signal
		and associated variable.

		Disconnect will not work if the signal or variable is 
		immutable.
	*/
	public static <T extends Connect.Connection> boolean disconnect( final T _element, final Connect.Signal _signal, final Connect.Slot<T> _slot )
	{
		final Connect connect = _element.getConnect() ;
		return connect.disconnect( _element, _signal, _slot ) ;
	}

	/**
		Disconnect all slots from a signal.
	*/
	public static <T extends Connect.Connection> boolean disconnect( final T _element )
	{
		final Connect connect = _element.getConnect() ;
		return connect.disconnect( _element ) ;
	}

	/**
		Inform all slots connected to this signal and associated
		to the variable that the signal's state has changed.
	*/
	public static <T extends Connect.Connection> void signal( final T _element, final Connect.Signal _signal )
	{
		final Connect connect = _element.getConnect() ;
		connect.signal( _element, _signal ) ;
	}

	public static <T extends UIElement> T applyMeta( final UIElement.Meta _meta, final T _element )
	{
		final Vector3 temp = new Vector3() ;

		final Vector3 position = _meta.getPosition( temp ) ;
		_element.setPosition( position.x, position.y, position.z ) ;

		final Vector3 offset = _meta.getOffset( temp ) ;
		_element.setOffset( offset.x, offset.y, offset.z ) ;

		final Vector3 length = _meta.getLength( temp ) ;
		_element.setLength( length.x, length.y, length.z ) ;

		final Vector3 minLength = _meta.getMinimumLength( temp ) ;
		_element.setMinimumLength( minLength.x, minLength.y, minLength.z ) ;

		final Vector3 maxLength = _meta.getMaximumLength( temp ) ;
		_element.setMaximumLength( maxLength.x, maxLength.y, maxLength.z ) ;

		_element.setLayer( _meta.getLayer() ) ;
		_element.setVisible( _meta.getVisibleFlag() ) ;

		if( _meta.getDisableFlag() == true )
		{
			_element.disable() ;
		}
		else
		{
			_element.enable() ;
		}

		return _element ;
	}

	public interface InputAction
	{
		public InputEvent.Action action( final UIElement.Component _listener, final InputEvent _event ) ;
	}

	private static final InputAction DEFAULT_SCROLL_ACTION = new InputAction()
	{
		@Override
		public InputEvent.Action action( final UIElement.Component _listener, final InputEvent _event )
		{
			return _listener.scroll( _event ) ;
		}
	} ;

	private static final InputAction DEFAULT_MOUSE_MOVE_ACTION = new InputAction()
	{
		@Override
		public InputEvent.Action action( final UIElement.Component _listener, final InputEvent _event )
		{
			return _listener.mouseMove( _event ) ;
		}
	} ;

	private static final InputAction DEFAULT_MOUSE_PRESSED_ACTION = new InputAction()
	{
		@Override
		public InputEvent.Action action( final UIElement.Component _listener, final InputEvent _event )
		{
			return _listener.mousePressed( _event ) ;
		}
	} ;

	private static final InputAction DEFAULT_MOUSE_RELEASED_ACTION = new InputAction()
	{
		@Override
		public InputEvent.Action action( final UIElement.Component _listener, final InputEvent _event )
		{
			return _listener.mouseReleased( _event ) ;
		}
	} ;

	private static final InputAction DEFAULT_TOUCH_MOVE_ACTION = new InputAction()
	{
		@Override
		public InputEvent.Action action( final UIElement.Component _listener, final InputEvent _event )
		{
			return _listener.touchMove( _event ) ;
		}
	} ;

	private static final InputAction DEFAULT_TOUCH_PRESSED_ACTION = new InputAction()
	{
		@Override
		public InputEvent.Action action( final UIElement.Component _listener, final InputEvent _event )
		{
			return _listener.touchPressed( _event ) ;
		}
	} ;

	private static final InputAction DEFAULT_TOUCH_RELEASED_ACTION = new InputAction()
	{
		@Override
		public InputEvent.Action action( final UIElement.Component _listener, final InputEvent _event )
		{
			return _listener.touchReleased( _event ) ;
		}
	} ;

	private static final InputAction DEFAULT_KEY_RELEASED_ACTION = new InputAction()
	{
		@Override
		public InputEvent.Action action( final UIElement.Component _listener, final InputEvent _event )
		{
			return _listener.keyReleased( _event ) ;
		}
	} ;

	private static final InputAction DEFAULT_KEY_PRESSED_ACTION = new InputAction()
	{
		@Override
		public InputEvent.Action action( final UIElement.Component _listener, final InputEvent _event )
		{
			return _listener.keyPressed( _event ) ;
		}
	} ;

	private static final InputAction DEFAULT_ANALOGUE_MOVE_ACTION = new InputAction()
	{
		@Override
		public InputEvent.Action action( final UIElement.Component _listener, final InputEvent _event )
		{
			return _listener.analogueMove( _event ) ;
		}
	} ;

	public static class Meta extends UIAbstractModel implements Connect.Connection
	{
		private final UIVariant name     = new UIVariant( "NAME",     "",    new Connect.Signal() ) ;
		private final UIVariant layer    = new UIVariant( "LAYER",    0,     new Connect.Signal() ) ;
		private final UIVariant visible  = new UIVariant( "VISIBLE",  true,  new Connect.Signal() ) ;
		private final UIVariant disabled = new UIVariant( "DISABLED", false, new Connect.Signal() ) ;

		private final UIVariant position = new UIVariant( "POSITION", new Vector3(), new Connect.Signal() ) ;
		private final UIVariant offset   = new UIVariant( "OFFSET",   new Vector3(), new Connect.Signal() ) ;
		private final UIVariant margin   = new UIVariant( "MARGIN",   new Vector3(), new Connect.Signal() ) ;

		private final UIVariant length        = new UIVariant( "LENGTH",     new Vector3(), new Connect.Signal() ) ;
		private final UIVariant minimumLength = new UIVariant( "MIN_LENGTH", new Vector3(), new Connect.Signal() ) ;
		private final UIVariant maximumLength = new UIVariant( "MAX_LENGTH", new Vector3(), new Connect.Signal() ) ;

		private final List<UIElement.MetaComponent> components = MalletList.<UIElement.MetaComponent>newList() ;

		private final Connect.Signal listenerAdded = new Connect.Signal() ;
		private final Connect.Signal listenerRemoved = new Connect.Signal() ;

		private final Connect connect = new Connect() ;

		public Meta()
		{
			int row = rowCount( root() ) ;
			createData( null, row + 10, 1 ) ;
			setData( new UIModelIndex( root(), row++, 0 ), name, UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), layer, UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), visible, UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), disabled, UIAbstractModel.Role.User ) ;

			setData( new UIModelIndex( root(), row++, 0 ), position, UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), offset, UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), margin, UIAbstractModel.Role.User ) ;

			setData( new UIModelIndex( root(), row++, 0 ), length, UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), minimumLength, UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), maximumLength, UIAbstractModel.Role.User ) ;
		}

		public void setName( final String _name )
		{
			if( _name != null && name.toString().equals( _name ) == false )
			{
				name.setString( _name ) ;
				UIElement.signal( this, name.getSignal() ) ;
			}
		}

		public void setLayer( final int _layer )
		{
			if( layer.toInt() != _layer )
			{
				layer.setInt( _layer ) ;
				UIElement.signal( this, layer.getSignal() ) ;
			}
		}

		public void setDisableFlag( final boolean _flag )
		{
			if( disabled.toBool() != _flag )
			{
				disabled.setBool( _flag ) ;
				UIElement.signal( this, disabled.getSignal() ) ;
			}
		}

		public void setVisibleFlag( final boolean _flag )
		{
			if( visible.toBool() != _flag )
			{
				visible.setBool( _flag ) ;
				UIElement.signal( this, visible.getSignal() ) ;
			}
		}

		public String getName()
		{
			return name.toString() ;
		}

		public int getLayer()
		{
			return layer.toInt() ;
		}

		public boolean getDisableFlag()
		{
			return disabled.toBool() ;
		}

		public boolean getVisibleFlag()
		{
			return visible.toBool() ;
		}

		public void setPosition( final Vector3 _val )
		{
			if( _val != null )
			{
				setPosition( _val.x, _val.y, _val.z ) ;
			}
		}

		public void setPosition( final float _x, final float _y, final float _z )
		{
			if( UI.applyVec3( position.toVector3(), _x, _y, _z ) == true )
			{
				UIElement.signal( this, position.getSignal() ) ;
			}
		}

		public void setOffset( final Vector3 _val )
		{
			if( _val != null )
			{
				setOffset( _val.x, _val.y, _val.z ) ;
			}
		}

		public void setOffset( final float _x, final float _y, final float _z )
		{
			if( UI.applyVec3( offset.toVector3(), _x, _y, _z ) == true )
			{
				UIElement.signal( this, offset.getSignal() ) ;
			}
		}

		public void setMargin( final Vector3 _val )
		{
			if( _val != null )
			{
				setMargin( _val.x, _val.y, _val.z ) ;
			}
		}

		public void setMargin( final float _x, final float _y, final float _z )
		{
			if( UI.applyVec3( margin.toVector3(), _x, _y, _z ) == true )
			{
				UIElement.signal( this, margin.getSignal() ) ;
			}
		}

		public Vector3 getPosition( final Vector3 _populate )
		{
			_populate.setXYZ( position.toVector3() ) ;
			return _populate ;
		}

		public Vector3 getOffset( final Vector3 _populate )
		{
			_populate.setXYZ( offset.toVector3() ) ;
			return _populate ;
		}

		public Vector3 getMargin( final Vector3 _populate )
		{
			_populate.setXYZ( margin.toVector3() ) ;
			return _populate ;
		}

		public void setLength( final Vector3 _val )
		{
			if( _val != null )
			{
				setLength( _val.x, _val.y, _val.z ) ;
			}
		}

		public void setLength( final float _x, final float _y, final float _z )
		{
			if( UI.applyVec3( length.toVector3(), _x, _y, _z ) == true )
			{
				UIElement.signal( this, length.getSignal() ) ;
			}
		}

		public void setMinimumLength( final Vector3 _val )
		{
			if( _val != null )
			{
				setMinimumLength( _val.x, _val.y, _val.z ) ;
			}
		}

		public void setMinimumLength( final float _x, final float _y, final float _z )
		{
			if( UI.applyVec3( minimumLength.toVector3(), _x, _y, _z ) == true )
			{
				UIElement.signal( this, minimumLength.getSignal() ) ;
			}
		}

		public void setMaximumLength( final Vector3 _val )
		{
			if( _val != null )
			{
				setMaximumLength( _val.x, _val.y, _val.z ) ;
			}
		}

		public void setMaximumLength( final float _x, final float _y, final float _z )
		{
			if( UI.applyVec3( maximumLength.toVector3(), _x, _y, _z ) == true )
			{
				UIElement.signal( this, maximumLength.getSignal() ) ;
			}
		}

		public Vector3 getLength( final Vector3 _populate )
		{
			_populate.setXYZ( length.toVector3() ) ;
			return _populate ;
		}

		public Vector3 getMinimumLength( final Vector3 _populate )
		{
			_populate.setXYZ( minimumLength.toVector3() ) ;
			return _populate ;
		}

		public Vector3 getMaximumLength( final Vector3 _populate )
		{
			_populate.setXYZ( maximumLength.toVector3() ) ;
			return _populate ;
		}

		public <M extends UIElement.MetaComponent> M addComponent( final M _meta )
		{
			if( _meta != null && components.contains( _meta ) == false )
			{
				components.add( _meta ) ;
				UIElement.signal( this, listenerAdded() ) ;
			}
			return _meta ;
		}

		public <M extends UIElement.MetaComponent> M removeComponent( final M _meta )
		{
			if( _meta != null && components.contains( _meta ) == true )
			{
				if( components.remove( _meta ) == true )
				{
					UIElement.signal( this, listenerRemoved() ) ;
					return _meta ;
				}
			}
			return null ;
		}

		public List<UIElement.MetaComponent> getComponents( final List<UIElement.MetaComponent> _components )
		{
			_components.addAll( components ) ;
			return _components ;
		}

		/**
			Remove all connections made to this packet.
			Should only be called by the instance's owner.
		*/
		public void shutdown()
		{
			for( UIElement.MetaComponent component : components )
			{
				component.shutdown() ;
			}
			UIElement.disconnect( this ) ;
		}

		/**
			Element type is used to figure out what UIGenerator is 
			required to create the correct UI element.
			When extending any Meta object override this function.
			If you do not override this then it will fallback to the 
			parent element-type.
		*/
		public String getElementType()
		{
			return "UIELEMENT" ;
		}

		public boolean supportsChildren()
		{
			return false ;
		}

		@Override
		public Connect getConnect()
		{
			return connect ;
		}

		public Connect.Signal nameChanged()
		{
			return name.getSignal() ;
		}

		public Connect.Signal layerChanged()
		{
			return layer.getSignal() ;
		}

		public Connect.Signal disableChanged()
		{
			return disabled.getSignal() ;
		}

		public Connect.Signal visibleChanged()
		{
			return visible.getSignal() ;
		}

		public Connect.Signal positionChanged()
		{
			return position.getSignal() ;
		}

		public Connect.Signal offsetChanged()
		{
			return offset.getSignal() ;
		}

		public Connect.Signal marginChanged()
		{
			return margin.getSignal() ;
		}

		public Connect.Signal lengthChanged()
		{
			return length.getSignal() ;
		}

		public Connect.Signal minimumLengthChanged()
		{
			return minimumLength.getSignal() ;
		}

		public Connect.Signal maximumLengthChanged()
		{
			return maximumLength.getSignal() ;
		}
		
		public Connect.Signal listenerAdded()
		{
			return listenerAdded ;
		}

		public Connect.Signal listenerRemoved()
		{
			return listenerRemoved ;
		}
	}

	/**
		Allows the developer to specify custom logic for 
		a UIElement that can be reused for other elements.
	*/
	public abstract class Component
	{
		protected final ID id ;

		public Component( final MetaComponent _meta )
		{
			id = new ID( _meta.getName(), _meta.getGroup() ) ;
			getParent().addComponent( this ) ;
		}

		public final boolean isName( final String _name )
		{
			return id.isName( _name ) ;
		}

		public final boolean isGroup( final String _group )
		{
			return id.isGroup( _group ) ;
		}

		/**
			Return the parent UIElement that this listener was 
			added to.
			Should return null if the listener has not or has been 
			removed from a UIElement.
		*/
		public UIElement getParent()
		{
			return UIElement.this ;
		}

		/**
			Send the event back-up the chain.
		*/
		public void sendEvent( final Event<?> _event )
		{
			getParent().addEvent( _event ) ;
		}

		/**
			Called when the scroll is moved.
		*/
		public InputEvent.Action scroll( final InputEvent _input )
		{
			return InputEvent.Action.PROPAGATE ;
		}

		/**
			Called when the mouse is moved.
		*/
		public InputEvent.Action mouseMove( final InputEvent _input )
		{
			return InputEvent.Action.PROPAGATE ;
		}

		/**
			Called when the mouse has a trigger pressed.
		*/
		public InputEvent.Action mousePressed( final InputEvent _input )
		{
			return InputEvent.Action.PROPAGATE ;
		}

		/**
			Called when the mouse has a trigger released.
		*/
		public InputEvent.Action mouseReleased( final InputEvent _input )
		{
			return InputEvent.Action.PROPAGATE ;
		}

		/**
			Called when the user is moving a pressed finger 
			on the touch screen.
		*/
		public InputEvent.Action touchMove( final InputEvent _input )
		{
			return InputEvent.Action.PROPAGATE ;
		}

		/**
			Called when a touch screen is pressed.
		*/
		public InputEvent.Action touchPressed( final InputEvent _input )
		{
			return InputEvent.Action.PROPAGATE ;
		}

		/**
			Called when a touch screen is released.
		*/
		public InputEvent.Action touchReleased( final InputEvent _input )
		{
			return InputEvent.Action.PROPAGATE ;
		}

		/**
			Called when a key is pressed.
		*/
		public InputEvent.Action keyPressed( final InputEvent _input )
		{
			return InputEvent.Action.PROPAGATE ;
		}

		/**
			Called when a key is released.
		*/
		public InputEvent.Action keyReleased( final InputEvent _input )
		{
			return InputEvent.Action.PROPAGATE ;
		}

		/**
			Called when a joystick/gamepad stick is waggled.
		*/
		public InputEvent.Action analogueMove( final InputEvent _input )
		{
			return InputEvent.Action.PROPAGATE ;
		}

		/**
			Called when the parent's World is set.
		*/
		public void setWorld( final World _world ) {}

		/**
			Called when parent UIElement is refreshing itself.
		*/
		public abstract void refresh() ;

		/**
			Remove the component from the parent element.
			Calls shutdown after component has been removed.
		*/
		public void destroy()
		{
			if( getParent().removeComponent( this ) == true )
			{
				shutdown() ;
			}
		}

		/**
			Called when parent UIElement has been flagged for shutdown.
			Clean up any resources you may have allocated.
		*/
		public abstract void shutdown() ;
	}

	/**
		A UIElement may want to contain additional components 
		that are automatically available when the element is 
		constructed by default. 

		For example it may contain a GUIDraw component - use the 
		Meta Component to build these components for their 
		associated meta element.
	*/
	public static abstract class MetaComponent extends UIAbstractModel implements Connect.Connection
	{
		private final UIVariant name  = new UIVariant( "NAME",  "", new Connect.Signal() ) ;
		private final UIVariant group = new UIVariant( "GROUP", "", new Connect.Signal() ) ;

		private final Connect connect = new Connect() ;

		public MetaComponent()
		{
			int row = rowCount( root() ) ;
			createData( null, row + 2, 1 ) ;

			setData( new UIModelIndex( root(), row++, 0 ), name,  UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), group, UIAbstractModel.Role.User ) ;
		}
		
		public void setName( final String _name )
		{
			if( _name != null && name.toString().equals( _name ) == false )
			{
				name.setString( _name ) ;
				UIElement.signal( this, name.getSignal() ) ;
			}
		}

		public void setGroup( final String _group )
		{
			if( _group != null && group.toString().equals( _group ) == false )
			{
				group.setString( _group ) ;
				UIElement.signal( this, group.getSignal() ) ;
			}
		}

		public String getName()
		{
			return name.toString() ;
		}

		public String getGroup()
		{
			return group.toString() ;
		}

		/**
			Remove all connections made to this packet.
			Should only be called by the instance's owner.
		*/
		public void shutdown()
		{
			UIElement.disconnect( this ) ;
		}

		@Override
		public Connect getConnect()
		{
			return connect ;
		}

		public Connect.Signal nameChanged()
		{
			return name.getSignal() ;
		}

		public Connect.Signal groupChanged()
		{
			return group.getSignal() ;
		}

		public abstract String getType() ;
	}

	public static class UV
	{
		public final Vector2 min ;
		public final Vector2 max ;

		public UV()
		{
			this( 0.0f, 0.0f, 1.0f, 1.0f ) ;
		}

		public UV( final float _minX, final float _minY, final float _maxX, final float _maxY )
		{
			this( new Vector2( _minX, _minY ), new Vector2( _maxX, _maxY ) ) ;
		}
		
		public UV( final Vector2 _min, final Vector2 _max )
		{
			min = _min ;
			max = _max ;
		}

		@Override
		public String toString()
		{
			return "Min " + min.toString() + " Max " + max.toString() ;
		}
	}
}
