package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Interpolate ;
import com.linxonline.mallet.util.caches.Cacheable ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

public abstract class DrawData<T extends DrawData, P> implements Draw<T>, Cacheable
{
	private static final int POSITION = 0 ;
	private static final int ROTATION = 3 ;
	private static final int SCALE = 6 ;

	private MalletColour colour   = null ;
	private StringBuilder text    = null ;
	private ProgramMap<P> program = null ;

	private int textStart = 0 ;
	private int textEnd = 0 ;
	private boolean update = true ;
	private boolean ui     = false ;

	private int order = 0 ;
	private Interpolation interpolation  = Interpolation.NONE ;
	private UpdateType updateType        = UpdateType.ON_DEMAND ; 

	private Vector3 offset ;
	// Each contain Position, Rotation, and Scale
	private final FloatBuffer old = FloatBuffer.allocate( 9 ) ;
	private final FloatBuffer present = FloatBuffer.allocate( 9 ) ;
	private final FloatBuffer future = FloatBuffer.allocate( 9 ) ;

	public DrawData()
	{
		this( UpdateType.ON_DEMAND,
			  Interpolation.NONE,
			  new Vector3(),			// position
			  new Vector3(),			// offset
			  new Vector3(),			// rotation
			  new Vector3( 1, 1, 1 ),	// scale
			  0 ) ;
	}

	public DrawData( final UpdateType _type,
					 final Interpolation _interpolation,
					 final Vector3 _position,
					 final Vector3 _offset,
					 final Vector3 _rotation,
					 final Vector3 _scale,
					 final int _order )
	{
		setOrder( _order ) ;
		setUpdateType( _type ) ;
		setInterpolationMode( _interpolation ) ;

		offset   = ( _offset != null )   ? _offset   : new Vector3() ;

		future.set( POSITION, ( _position != null ) ? _position : new Vector3()  ) ;
		future.set( ROTATION, ( _rotation != null ) ? _rotation : new Vector3() ) ;
		future.set( SCALE,    ( _scale != null )    ? _scale    : new Vector3() ) ;
	}

	public void setProgram( final ProgramMap<P> _program )
	{
		program = _program ;
	}

	public ProgramMap<P> getProgram()
	{
		return program ;
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
		setTextStart( 0 ) ;
		setTextEnd( text.length() ) ;
	}

	public void setTextStart( final int _start )
	{
		textStart = _start ;
	}

	public void setTextEnd( final int _end )
	{
		textEnd = _end ;
	}

	public StringBuilder getText()
	{
		return text ;
	}

	public int getTextStart()
	{
		return textStart ;
	}

	public int getTextEnd()
	{
		return textEnd ;
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
		order = ( _order >= 0 ) ? _order : 0 ;
	}

	public void setPosition( final float _x, final float _y, final float _z )
	{
		future.set( POSITION, _x, _y, _z ) ;
	}

	public void setOffset( final float _x, final float _y, final float _z )
	{
		offset.setXYZ( _x, _y, _z ) ;
	}

	public void setRotation( final float _x, final float _y, final float _z )
	{
		future.set( ROTATION, _x, _y, _z ) ;
	}

	public void setScale( final float _x, final float _y, final float _z )
	{
		future.set( SCALE, _x, _y, _z ) ;
	}

	public void setUpdateType( final UpdateType _type )
	{
		updateType = ( _type == null ) ? UpdateType.ON_DEMAND : _type ;
	}

	public void setInterpolationMode( final Interpolation _interpolation )
	{
		interpolation = ( _interpolation == null ) ? Interpolation.NONE : _interpolation ;
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
		return interpolation ;
	}

	public Vector3 getPosition( final Vector3 _fill )
	{
		return present.fill( _fill, POSITION ) ;
	}

	public Vector3 getOffset( final Vector3 _fill )
	{
		_fill.setXYZ( offset ) ;
		return _fill ;
	}

	public Vector3 getRotation( final Vector3 _fill )
	{
		return present.fill( _fill, ROTATION ) ;
	}

	public Vector3 getScale( final Vector3 _fill )
	{
		return present.fill( _fill, SCALE ) ;
	}

	/**
		Called by the worlds DrawState.
	*/
	public void update( final int _diff, final int _iteration )
	{
		// Position, Rotation, and Scale should always 
		// be updated even if the data is not being uploaded 
		// to the GPU.
		switch( getInterpolationMode() )
		{
			case LINEAR :
			{
				interpolate( future, old, present, _diff, _iteration ) ;
				break ;
			}
			case NONE   :
			default     :
			{
				FloatBuffer.copy( future, present ) ;
				break ;
			}
		}
	}

	private void interpolate( final FloatBuffer _future, final FloatBuffer _past, final FloatBuffer _present, final int _diff, final int _iteration )
	{
		if( Interpolate.linear( _future, _past, _present, _diff, _iteration ) )
		{
			// If an object has not reached its final state
			// then flag it for updating again during the next draw call.
			forceUpdate() ;
		}
	}

	public boolean toUpdate()
	{
		if( getUpdateType() == UpdateType.CONTINUOUS )
		{
			return true ;
		}

		final boolean temp = update ;
		update = false ;
		return temp ;
	}

	@Override
	public void reset()
	{
		colour  = null ;
		text    = null ;
		program = null ;

		update = true ;
		ui = false ;

		order = 0 ;
		interpolation = Interpolation.NONE ;
		updateType = UpdateType.ON_DEMAND ; 
	}
}
