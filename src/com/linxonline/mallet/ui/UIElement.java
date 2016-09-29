package com.linxonline.mallet.ui ;

import java.util.ArrayList ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

public class UIElement implements InputHandler
{
	private final float DEFAULT_MARGIN_SIZE = 5.0f ;

	private final ListenerUnit<BaseListener> listeners = new ListenerUnit<BaseListener>( this ) ;
	private final ArrayList<Event<?>> events = new ArrayList<Event<?>>() ;
	private InputAdapterInterface adapter = null ;

	private State current = State.NEUTRAL ;

	public boolean destroy = false ;

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
		NEUTRAL,
		ENGAGED
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

		final float ratioMargin = ratio.toPixel( DEFAULT_MARGIN_SIZE ) ;
		margin = new Vector3( ratioMargin, ratioMargin, ratioMargin ) ;
	}

	public void addEvent( final Event<?> _event )
	{
		events.add( _event ) ;
	}

	public void addListener( final BaseListener _listener )
	{
		listeners.addListener( _listener ) ;
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

	public void update( final float _dt, final ArrayList<Event<?>> _events )
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

	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		if( isIntersectInput( _event ) == true )
		{
			current = State.ENGAGED ;
			switch( _event.getInputType() )
			{
				case MOUSE_MOVED     :
				case TOUCH_MOVE      : return updateMove( _event ) ;
				case MOUSE1_PRESSED  :
				case MOUSE2_PRESSED  :
				case MOUSE3_PRESSED  :
				case TOUCH_DOWN      : return updatePressed( _event ) ;
				case MOUSE1_RELEASED :
				case MOUSE2_RELEASED :
				case MOUSE3_RELEASED :
				case TOUCH_UP        : return updateReleased( _event ) ;
			}
		}

		if( current == State.ENGAGED )
		{
			current = State.NEUTRAL ;
			updateExited( _event ) ;
		}

		return InputEvent.Action.PROPAGATE ;
	}

	private boolean isIntersectInput( final InputEvent _event )
	{
		final InputAdapterInterface adapter = getInputAdapter() ;
		if( adapter != null )
		{
			return intersectPoint( adapter.convertInputToUIRenderX( _event.mouseX ),
								   adapter.convertInputToUIRenderY( _event.mouseY ) ) ;
		}

		return false ;
	}

	private InputEvent.Action updateMove( final InputEvent _event )
	{
		final ArrayList<BaseListener> base = listeners.getListeners() ;
		final int size = base.size() ;
		for( int i = 0; i < size; i++ )
		{
			if( base.get( i ).move( _event ) == InputEvent.Action.CONSUME )
			{
				return InputEvent.Action.CONSUME ;
			}
		}

		return InputEvent.Action.PROPAGATE ;
	}

	private InputEvent.Action updateReleased( final InputEvent _event )
	{
		final ArrayList<BaseListener> base = listeners.getListeners() ;
		final int size = base.size() ;
		for( int i = 0; i < size; i++ )
		{
			if( base.get( i ).released( _event ) == InputEvent.Action.CONSUME )
			{
				return InputEvent.Action.CONSUME ;
			}
		}

		return InputEvent.Action.PROPAGATE ;
	}

	private InputEvent.Action updatePressed( final InputEvent _event )
	{
		final ArrayList<BaseListener> base = listeners.getListeners() ;
		final int size = base.size() ;
		for( int i = 0; i < size; i++ )
		{
			if( base.get( i ).pressed( _event ) == InputEvent.Action.CONSUME )
			{
				return InputEvent.Action.CONSUME ;
			}
		}

		return InputEvent.Action.PROPAGATE ;
	}

	private InputEvent.Action updateExited( final InputEvent _event )
	{
		final ArrayList<BaseListener> base = listeners.getListeners() ;
		final int size = base.size() ;
		for( int i = 0; i < size; i++ )
		{
			if( base.get( i ).exited( _event ) == InputEvent.Action.CONSUME )
			{
				return InputEvent.Action.CONSUME ;
			}
		}

		return InputEvent.Action.PROPAGATE ;
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

	/**
		Set the UIElement's absolute position.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.
	*/
	public void setPosition( final float _x, final float _y, final float _z )
	{
		makeDirty() ;
		position.setXYZ( ratio.toPixel( _x ), ratio.toPixel( _y ), ratio.toPixel( _z ) ) ;
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
		offset.setXYZ( ratio.toPixel( _x ), ratio.toPixel( _y ), ratio.toPixel( _z ) ) ;
	}

	/**
		Set the UIElement's minimum length, min size of the element.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.
	*/
	public void setMinimumLength( final float _x, final float _y, final float _z )
	{
		minLength.x = ( _x < 0.0f ) ? 0.0f : ratio.toPixel( _x ) ;
		minLength.y = ( _y < 0.0f ) ? 0.0f : ratio.toPixel( _y ) ;
		minLength.z = ( _z < 0.0f ) ? 0.0f : ratio.toPixel( _z ) ;

		// Ensure that length adheres to the new minimum length
		setLength( length.x, length.y, length.z ) ;
	}

	/**
		Set the UIElement's maximum length, max size of the element.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.
	*/
	public void setMaximumLength( final float _x, final float _y, final float _z )
	{
		maxLength.x = ( _x < 0.0f ) ? 0.0f : ratio.toPixel( _x ) ;
		maxLength.y = ( _y < 0.0f ) ? 0.0f : ratio.toPixel( _y ) ;
		maxLength.z = ( _z < 0.0f ) ? 0.0f : ratio.toPixel( _z ) ;

		// Ensure that length adheres to the new maximum length
		setLength( length.x, length.y, length.z ) ;
	}

	/**
		Set the UIElement's length, actual size of the element.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.
	*/
	public void setLength( final float _x, final float _y, final float _z )
	{
		makeDirty() ;
		if( minLength != null )
		{
			length.x = ( _x < minLength.x ) ? minLength.x : ratio.toPixel( _x ) ;
			length.y = ( _y < minLength.y ) ? minLength.y : ratio.toPixel( _y ) ;
			length.z = ( _z < minLength.z ) ? minLength.z : ratio.toPixel( _z ) ;
		}

		if( maxLength.x > 0.0f )
		{
			length.x = ( _x > maxLength.x ) ? maxLength.x : ratio.toPixel( _x ) ;
		}

		if( maxLength.y > 0.0f )
		{
			length.y = ( _y > maxLength.y ) ? maxLength.y : ratio.toPixel( _y ) ;
		}

		if( maxLength.z > 0.0f )
		{
			length.z = ( _z > maxLength.z ) ? maxLength.z : ratio.toPixel( _z ) ;
		}
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
		margin.setXYZ( ratio.toPixel( _x ), ratio.toPixel( _y ), ratio.toPixel( _z ) ) ;
	}

	public void setLayer( final int _layer )
	{
		layer = _layer ;
	}

	public boolean isDirty()
	{
		return dirty ;
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

	@Override
	public void setInputAdapterInterface( final InputAdapterInterface _adapter )
	{
		adapter = _adapter ;
	}

	public InputAdapterInterface getInputAdapter()
	{
		return adapter ;
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
		setInputAdapterInterface( null ) ;
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
}
