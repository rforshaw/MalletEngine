package com.linxonline.mallet.ui ;

import java.util.ArrayList ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

public abstract class UIElement implements InputHandler
{
	private final float DEFAULT_MARGIN_SIZE = 9.0f ;

	private ArrayList<Event<?>> events = new ArrayList<Event<?>>() ;
	private InputAdapterInterface adapter = null ;

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
	}

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

	public void setPosition( final float _x, final float _y, final float _z )
	{
		position.setXYZ( _x, _y, _z ) ;
	}

	public void setOffset( final float _x, final float _y, final float _z )
	{
		offset.setXYZ( _x, _y, _z ) ;
	}

	public void setLength( final float _x, final float _y, final float _z )
	{
		length.setXYZ( _x, _y, _z ) ;
	}

	public void setMargin( final float _x, final float _y, final float _z )
	{
		margin.setXYZ( _x, _y, _z ) ;
	}

	public Vector3 getPosition()
	{
		return position ;
	}

	public Vector3 getOffset()
	{
		return offset ;
	}

	public Vector3 getLength()
	{
		return length ;
	}

	public Vector3 getMargin()
	{
		return margin ;
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

	public void clear()
	{
		setInputAdapterInterface( null ) ;
		events.clear() ;
	}
	
	@Override
	public void reset()
	{
		position.setXYZ( 0.0f, 0.0f, 0.0f ) ;
		offset.setXYZ( 0.0f, 0.0f, 0.0f ) ;
		length.setXYZ( 0.0f, 0.0f, 0.0f ) ;
		margin.setXYZ( DEFAULT_MARGIN_SIZE, DEFAULT_MARGIN_SIZE, DEFAULT_MARGIN_SIZE ) ;
	}
}
