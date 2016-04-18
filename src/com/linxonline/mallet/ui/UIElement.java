package com.linxonline.mallet.ui ;

import java.util.ArrayList ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

public abstract class UIElement implements InputHandler
{
	private final float DEFAULT_MARGIN_SIZE = 5.0f ;

	private final ArrayList<Event<?>> events = new ArrayList<Event<?>>() ;
	private InputAdapterInterface adapter = null ;

	private final Vector3 minLength = new Vector3() ;
	private Vector3 maxLength = null ;

	private boolean dirty = true ;
	private int layer = 0 ;

	private final Vector3 position ;
	private final Vector3 offset ;
	private final Vector3 length ;
	private final Vector3 margin ;

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
	public abstract void refresh() ;

	public abstract InputEvent.Action passInputEvent( final InputEvent _event ) ;

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
		if( maxLength == null )
		{
			maxLength = new Vector3( _x, _y, _z ) ;
			return ;
		}

		maxLength.setXYZ( _x, _y, _z ) ;
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
			length.x = ( _x > maxLength.x ) ? maxLength.x : _x ;
			length.y = ( _y > maxLength.y ) ? maxLength.y : _y ;
			length.z = ( _z > maxLength.z ) ? maxLength.z : _z ;
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
	public abstract void shutdown() ;

	/**
		Blank out any content it may be retaining.
	*/
	public void clear()
	{
		setInputAdapterInterface( null ) ;
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
