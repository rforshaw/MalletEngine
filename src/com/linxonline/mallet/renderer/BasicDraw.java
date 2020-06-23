package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.util.Interpolate ;
import com.linxonline.mallet.util.caches.Cacheable ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

public class BasicDraw<P> implements Cacheable
{
	private static final float PI = ( float )Math.PI ;
	private static final float PI2 = ( float )Math.PI * 2.0f ;

	private static final int POSITION = 0 ;
	private static final int OFFSET   = 3 ;
	private static final int ROTATION = 6 ;
	private static final int SCALE    = 9 ;

	private MalletColour colour   = null ;	
	private ProgramMap<P> program = null ;

	private boolean update = true ;
	private boolean ui     = false ;

	private int order = 0 ;
	private Interpolation interpolation  = Interpolation.NONE ;
	private UpdateType updateType        = UpdateType.ON_DEMAND ; 

	// Each contain Position, Rotation, and Scale
	private final float[] old = FloatBuffer.allocate( 12 ) ;
	private final float[] present = FloatBuffer.allocate( 12 ) ;
	private final float[] future = FloatBuffer.allocate( 12 ) ;

	public BasicDraw()
	{
		this( UpdateType.ON_DEMAND,
			  Interpolation.NONE,
			  new Vector3(),			// position
			  new Vector3(),			// offset
			  new Vector3(),			// rotation
			  new Vector3( 1, 1, 1 ),	// scale
			  0 ) ;
	}

	public BasicDraw( final UpdateType _type,
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

		FloatBuffer.set( old, POSITION, ( _position != null ) ? _position : new Vector3() ) ;
		FloatBuffer.set( old, OFFSET,   ( _offset != null )   ? _offset   : new Vector3() ) ;
		FloatBuffer.set( old, ROTATION, ( _rotation != null ) ? _rotation : new Vector3() ) ;
		FloatBuffer.set( old, SCALE,    ( _scale != null )    ? _scale    : new Vector3() ) ;

		FloatBuffer.set( present, POSITION, ( _position != null ) ? _position : new Vector3() ) ;
		FloatBuffer.set( present, OFFSET,   ( _offset != null )   ? _offset   : new Vector3() ) ;
		FloatBuffer.set( present, ROTATION, ( _rotation != null ) ? _rotation : new Vector3() ) ;
		FloatBuffer.set( present, SCALE,    ( _scale != null )    ? _scale    : new Vector3() ) ;

		FloatBuffer.set( future, POSITION, ( _position != null ) ? _position : new Vector3() ) ;
		FloatBuffer.set( future, OFFSET,   ( _offset != null )   ? _offset   : new Vector3() ) ;
		FloatBuffer.set( future, ROTATION, ( _rotation != null ) ? _rotation : new Vector3() ) ;
		FloatBuffer.set( future, SCALE,    ( _scale != null )    ? _scale    : new Vector3() ) ;
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
		FloatBuffer.set( future, POSITION, _x, _y, _z ) ;
	}

	public void setOffset( final float _x, final float _y, final float _z )
	{
		FloatBuffer.set( future, OFFSET, _x, _y, _z ) ;
	}

	public void setRotation( final float _x, final float _y, final float _z )
	{
		float oX = FloatBuffer.get( old, ROTATION + 0 ) ;
		float oY = FloatBuffer.get( old, ROTATION + 1 ) ;
		float oZ = FloatBuffer.get( old, ROTATION + 2 ) ;

		final float diffX = Math.abs( _x - oX ) ;
		if( diffX > PI )
		{
			oX += ( _x > oX ) ? PI2 : -PI2 ;
		}

		final float diffY = Math.abs( _y - oY ) ;
		if( diffY > PI )
		{
			oY += ( _y > oY ) ? PI2 : -PI2 ;
		}

		final float diffZ = Math.abs( _z - oZ ) ;
		if( diffZ > PI )
		{
			oZ += ( _z > oZ ) ? PI2 : -PI2 ;
		}

		FloatBuffer.set( old, ROTATION, oX, oY, oZ ) ;
		FloatBuffer.set( future, ROTATION, _x, _y, _z ) ;
	}

	public void setScale( final float _x, final float _y, final float _z )
	{
		FloatBuffer.set( future, SCALE, _x, _y, _z ) ;
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
		return FloatBuffer.fill( present, _fill, POSITION ) ;
	}

	public Vector3 getOffset( final Vector3 _fill )
	{
		return FloatBuffer.fill( present, _fill, OFFSET ) ;
	}

	public Vector3 getRotation( final Vector3 _fill )
	{
		return FloatBuffer.fill( present, _fill, ROTATION ) ;
	}

	public Vector3 getScale( final Vector3 _fill )
	{
		return FloatBuffer.fill( present, _fill, SCALE ) ;
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

	private void interpolate( final float[] _future,
							  final float[] _past,
							  final float[] _present,
							  final int _diff,
							  final int _iteration )
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
		program = null ;

		update = true ;
		ui = false ;

		order = 0 ;
		interpolation = Interpolation.NONE ;
		updateType = UpdateType.ON_DEMAND ; 
	}
}
