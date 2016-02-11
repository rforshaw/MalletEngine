package com.linxonline.mallet.ui ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

public abstract class UIElement implements InputHandler
{
	private EventController controller = null ;
	private InputAdapterInterface adapter = null ;

	private final Vector3 position ;
	private final Vector3 offset ;
	private final Vector3 length ;

	public UIElement()
	{
		position = new Vector3() ;
		offset = new Vector3() ;
		length = new Vector3() ;
	}
	
	public UIElement( final Vector3 _position, final Vector3 _offset, final Vector3 _length )
	{
		position = _position ;
		offset = _offset ;
		length = _length ;
	}

	public abstract void update( final float _dt ) ;

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

	@Override
	public void setInputAdapterInterface( final InputAdapterInterface _adapter )
	{
		adapter = _adapter ;
	}

	public void setEventController( final EventController _controller )
	{
		controller = _controller ;
	}

	public InputAdapterInterface getInputAdapter()
	{
		return adapter ;
	}

	public EventController getEventController()
	{
		return controller ;
	}

	public void clear()
	{
		setInputAdapterInterface( null ) ;
		setEventController( null ) ;
	}
	
	@Override
	public void reset()
	{
		position.setXYZ( 0.0f, 0.0f, 0.0f ) ;
		offset.setXYZ( 0.0f, 0.0f, 0.0f ) ;
		length.setXYZ( 0.0f, 0.0f, 0.0f ) ;
	}
}