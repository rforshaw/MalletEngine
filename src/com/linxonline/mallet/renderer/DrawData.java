package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;

import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.util.caches.Cacheable ;

public abstract class DrawData<T extends DrawData> implements Draw<T>, Cacheable
{
	private final Draw.DrawInterface DRAW_DEFAULT = new Draw.DrawInterface<T>()
	{
		@Override
		public void draw( final T _data ) {}
	} ;

	private final ArrayList<MalletTexture> textures = new ArrayList<MalletTexture>() ;
	private MalletColour colour = null ;
	private MalletFont font = null ;
	private StringBuilder text = null ;

	private boolean update = true ;
	private boolean ui = false ;

	private int order = 0 ;
	private Interpolation mode = Interpolation.NONE ;
	private UpdateType updateType  = UpdateType.ON_DEMAND ; 
	private Draw.DrawInterface<T> draw = DRAW_DEFAULT ;

	private final Vector3 oldPosition = new Vector3() ;
	private final Vector3 oldRotation = new Vector3()  ;
	private final Vector3 oldScale = new Vector3() ;

	private final Vector3 currentPosition = new Vector3() ;
	private final Vector3 currentRotation = new Vector3()  ;
	private final Vector3 currentScale = new Vector3() ;

	private Vector3 position ;
	private Vector3 offset ;
	private Vector3 rotation ;
	private Vector3 scale ;

	public DrawData() {}

	public DrawData( final UpdateType _type,
					 final Interpolation _mode,
					 final Vector3 _position,
					 final Vector3 _offset,
					 final Vector3 _rotation,
					 final Vector3 _scale,
					 final int _order )
	{
		setUpdateType( _type ) ;
		setInterpolationMode( _mode ) ;
		position = _position ;
		offset = _offset ;
		rotation = _rotation ;
		scale = _scale ;
		order = _order ;
	}

	public void setColour( final MalletColour _colour )
	{
		colour = _colour ;
	}

	public MalletColour getColour()
	{
		return colour ;
	}

	public void setText( final StringBuilder _text )
	{
		text = _text ;
	}

	public StringBuilder getText()
	{
		return text ;
	}

	public void setFont( final MalletFont _font )
	{
		font = _font ;
	}

	public MalletFont getFont()
	{
		return font ;
	}

	public void addTexture( final MalletTexture _texture )
	{
		textures.add( _texture ) ;
	}

	public void removeTexture( final MalletTexture _texture )
	{
		textures.remove( _texture ) ;
	}

	public MalletTexture getMalletTexture( final int _index )
	{
		return textures.get( _index ) ;
	}
	
	public ArrayList<MalletTexture> getMalletTextures()
	{
		return textures ;
	}

	public void setUI( final boolean _ui )
	{
		ui = _ui ;
	}

	public boolean isUI()
	{
		return ui ;
	}

	public void forceUpdate()
	{
		update = true ;
	}

	public void setOrder( final int _order )
	{
		order = _order ;
	}

	public void setPosition( final Vector3 _position )
	{
		position = _position ;
	}

	public void setRotation( final Vector3 _rotation)
	{
		rotation = _rotation ;
	}

	public void setScale( final Vector3 _scale )
	{
		scale = _scale ;
	}

	public void setPosition( final float _x, final float _y, final float _z )
	{
		position.setXYZ( _x, _y, _z ) ;
	}

	public void setRotation( final float _x, final float _y, final float _z )
	{
		rotation.setXYZ( _x, _y, _z ) ;
	}

	public void setScale( final float _x, final float _y, final float _z )
	{
		scale.setXYZ( _x, _y, _z ) ;
	}

	public void setUpdateType( final UpdateType _type )
	{
		updateType = ( _type == null ) ? UpdateType.ON_DEMAND : _type ;
	}

	public void setInterpolationMode( Interpolation _mode )
	{
		mode = ( _mode == null ) ? Interpolation.NONE : _mode ;
	}

	public boolean toUpdate()
	{
		final boolean temp = update ;
		if( temp == true )
		{
			update = false ;
		}

		return temp ;
	}
	
	public int getOrder()
	{
		return order ;
	}

	public UpdateType getUpdateType()
	{
		return updateType ;
	}

	public Interpolation getInterpolationMode()
	{
		return mode ;
	}

	public Vector3 getPosition()
	{
		return currentPosition ;
	}

	public Vector3 getOffset()
	{
		return offset ;
	}

	public Vector3 getRotation()
	{
		return currentRotation ;
	}

	public Vector3 getScale()
	{
		return currentScale ;
	}

	protected void draw( final int _diff, final int _iteration )
	{
		switch( getInterpolationMode() )
		{
			case LINEAR :
			{
				interpolate( position, oldPosition, currentPosition, _diff, _iteration ) ;
				interpolate( scale,    oldScale,    currentScale,    _diff, _iteration ) ;
				interpolate( rotation, oldRotation, currentRotation, _diff, _iteration ) ;
				break ;
			}
			case NONE   :
			default     :
			{
				currentPosition.setXYZ( position ) ;
				currentRotation.setXYZ( rotation ) ;
				currentScale.setXYZ( scale ) ;
				break ;
			}
		}

		draw.draw( ( T )this ) ;
	}

	private void interpolate( final Vector3 _future, final Vector3 _past, final Vector3 _present, final int _diff, final int _iteration )
	{
		final float xDiff = ( _future.x - _past.x ) / _diff ;
		final float yDiff = ( _future.y - _past.y ) / _diff ;
		final float zDiff = ( _future.z - _past.z ) / _diff ;

		if( xDiff > 0.0f || yDiff > 0.0f || zDiff > 0.0f )
		{
			// If an object has not reached its final state
			// then flag it for updating again during the next draw call.
			forceUpdate() ;
		}

		_present.setXYZ( _past.x + ( xDiff * _iteration ),
						 _past.y + ( yDiff * _iteration ),
						 _past.z + ( zDiff * _iteration ) ) ;
		_past.setXYZ( _present ) ;
	}
	
	public abstract void unregister() ;

	@Override
	public void reset()
	{
		textures.clear() ;
		colour = null ;
		font = null ;
		text = null ;

		update = true ;
		ui = false ;

		int order = 0 ;
		mode = Interpolation.NONE ;
		updateType  = UpdateType.ON_DEMAND ; 
		draw = DRAW_DEFAULT ;

		position = null ;
		offset   = null ;
		rotation = null ;
		scale    = null ;
	}

	@Override
	public void setDrawInterface( final Draw.DrawInterface<T> _draw )
	{
		draw = ( _draw == null ) ? DRAW_DEFAULT : _draw ;
	}
}