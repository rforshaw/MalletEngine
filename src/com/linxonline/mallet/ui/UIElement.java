package com.linxonline.mallet.ui ;

import java.util.ArrayList ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

public abstract class UIElement implements InputHandler
{
	private final float DEFAULT_MARGIN_SIZE = 5.0f ;

	private final ListenerUnit<BaseListener> listeners = new ListenerUnit<BaseListener>( this ) ;
	private final ArrayList<Event<?>> events = new ArrayList<Event<?>>() ;
	private InputAdapterInterface adapter = null ;

	private State current = State.NEUTRAL ;

	private final Vector3 minLength = new Vector3() ;
	private Vector3 maxLength = new Vector3() ;

	public boolean destroy = false ;

	private boolean dirty = true ;
	private int layer = 0 ;

	private final Vector3 position ;
	private final Vector3 offset ;
	private final Vector3 length ;
	private final Vector3 margin ;

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
		margin = new Vector3( DEFAULT_MARGIN_SIZE, DEFAULT_MARGIN_SIZE, DEFAULT_MARGIN_SIZE ) ;
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
				case TOUCH_DOWN      : return updateReleased( _event ) ;
				case MOUSE1_RELEASED :
				case MOUSE2_RELEASED :
				case MOUSE3_RELEASED :
				case TOUCH_UP        : return updatePressed( _event ) ;
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

	public void setPosition( final float _x, final float _y, final float _z )
	{
		makeDirty() ;
		position.setXYZ( _x, _y, _z ) ;
	}

	public void setOffset( final float _x, final float _y, final float _z )
	{
		makeDirty() ;
		offset.setXYZ( _x, _y, _z ) ;
	}

	public void setMinimumLength( final float _x, final float _y, final float _z )
	{
		minLength.x = ( _x < 0.0f ) ? 0.0f : _x ;
		minLength.y = ( _y < 0.0f ) ? 0.0f : _y ;
		minLength.z = ( _z < 0.0f ) ? 0.0f : _z ;

		setLength( length.x, length.y, length.z ) ;
	}

	public void setMaximumLength( final float _x, final float _y, final float _z )
	{
		maxLength.x = ( _x < 0.0f ) ? 0.0f : _x ;
		maxLength.y = ( _y < 0.0f ) ? 0.0f : _y ;
		maxLength.z = ( _z < 0.0f ) ? 0.0f : _z ;

		setLength( length.x, length.y, length.z ) ;
	}

	public void setLength( final float _x, final float _y, final float _z )
	{
		makeDirty() ;
		if( minLength == null && maxLength == null )
		{
			length.x = ( _x < 0.0f ) ? 0.0f : _x ;
			length.y = ( _y < 0.0f ) ? 0.0f : _y ;
			length.z = ( _z < 0.0f ) ? 0.0f : _z ;
			return ;
		}

		if( minLength != null )
		{
			length.x = ( _x < minLength.x ) ? minLength.x : _x ;
			length.y = ( _y < minLength.y ) ? minLength.y : _y ;
			length.z = ( _z < minLength.z ) ? minLength.z : _z ;
		}

		if( maxLength != null )
		{
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
		}
	}

	public void setMargin( final float _x, final float _y, final float _z )
	{
		makeDirty() ;
		margin.setXYZ( _x, _y, _z ) ;
	}

	public void setLayer( final int _layer )
	{
		layer = _layer ;
	}

	public boolean isDirty()
	{
		return dirty ;
	}

	public Vector3 getPosition()
	{
		return position ;
	}

	public Vector3 getOffset()
	{
		return offset ;
	}

	public Vector3 getMaximumLength()
	{
		return maxLength ;
	}

	public Vector3 getMinimumLength()
	{
		return minLength ;
	}

	public Vector3 getLength()
	{
		return length ;
	}

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
