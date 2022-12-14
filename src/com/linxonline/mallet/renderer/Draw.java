package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.util.Interpolate ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

public final class Draw implements IUpdate
{
	private static final float PI = ( float )Math.PI ;
	private static final float PI2 = ( float )Math.PI * 2.0f ;

	private static final int POSITION = 0 ;
	private static final int OFFSET   = 3 ;
	private static final int ROTATION = 6 ;
	private static final int SCALE    = 9 ;

	public static final IMeta EMPTY_META = new IMeta()
	{
		public int getType()
		{
			return -1 ;
		}
	} ;

	private IMeta meta = EMPTY_META ;
	private boolean hidden = false ;
	private final IShape[] shapes ;
	private MalletColour colour = null ;

	// Each contain Position, Rotation, and Scale
	private final float[] old = FloatBuffer.allocate( 12 ) ;
	private final float[] present = FloatBuffer.allocate( 12 ) ;
	private final float[] future = FloatBuffer.allocate( 12 ) ;

	public Draw()
	{
		this( 0.0f, 0.0f, 0.0f,
			  0.0f, 0.0f, 0.0f ) ;
	}

	public Draw( final float _posX, final float _posY, final float _posZ,
				 final float _offX, final float _offY, final float _offZ )
	{
		this( _posX, _posY, _posZ,
			  _offX, _offY, _offZ,
			  0.0f, 0.0f, 0.0f ) ;
	}

	public Draw( final float _posX, final float _posY, final float _posZ,
				 final float _offX, final float _offY, final float _offZ,
				 final float _rotX, final float _rotY, final float _rotZ )
	{
		this( new IShape[1],
			  _posX, _posY, _posZ,
			  _offX, _offY, _offZ,
			  _rotX, _rotY, _rotZ ) ;
	}

	public Draw( final IShape[] _shapes,
				 final float _posX, final float _posY, final float _posZ,
				 final float _offX, final float _offY, final float _offZ,
				 final float _rotX, final float _rotY, final float _rotZ )
	{
		shapes = _shapes ;
		FloatBuffer.set( old, POSITION, _posX, _posY, _posZ ) ;
		FloatBuffer.set( old, OFFSET, _offX, _offY, _offZ ) ;
		FloatBuffer.set( old, ROTATION, _rotX, _rotY, _rotZ ) ;
		FloatBuffer.set( old, SCALE, 1.0f, 1.0f, 1.0f ) ;

		FloatBuffer.set( present, POSITION, _posX, _posY, _posZ ) ;
		FloatBuffer.set( present, OFFSET, _offX, _offY, _offZ ) ;
		FloatBuffer.set( present, ROTATION, _rotX, _rotY, _rotZ ) ;
		FloatBuffer.set( present, SCALE, 1.0f, 1.0f, 1.0f ) ;

		FloatBuffer.set( future, POSITION, _posX, _posY, _posZ ) ;
		FloatBuffer.set( future, OFFSET, _offX, _offY, _offZ ) ;
		FloatBuffer.set( future, ROTATION, _rotX, _rotY, _rotZ ) ;
		FloatBuffer.set( future, SCALE, 1.0f, 1.0f, 1.0f ) ;
	}

	/**
		Associate the draw object with further information.
		This could be used for a variety of purposes specific
		to the developers own use-cases.
	*/
	public void setMeta( final IMeta _meta )
	{
		meta = ( _meta != null ) ? _meta : EMPTY_META ;
	}

	/**
		Return the meta object specified by the developer, or null
		if nothing has been set.
	*/
	public IMeta getMeta()
	{
		return meta ;
	}

	public IShape[] getShapes()
	{
		return shapes ;
	}

	/**
		It's likely that the majority of Draw objects will 
		contain only one shape.
	*/
	public IShape setShape( final Shape _shape )
	{
		shapes[0] = _shape ;
		return _shape ;
	}

	/**
		It's likely that the majority of Draw objects will 
		contain only one shape.
	*/
	public IShape getShape()
	{
		return shapes[0] ;
	}

	public void setHidden( final boolean _hide )
	{
		hidden = _hide ;
	}

	public boolean isHidden()
	{
		return hidden ;
	}

	public MalletColour setColour( final MalletColour _colour )
	{
		colour = _colour ;
		return colour ;
	}

	public MalletColour getColour()
	{
		return colour ;
	}

	public void setPositionInstant( final float _x, final float _y, final float _z )
	{
		FloatBuffer.set( old, POSITION, _x, _y, _z ) ;
		FloatBuffer.set( present, POSITION, _x, _y, _z ) ;
		FloatBuffer.set( future, POSITION, _x, _y, _z ) ;
	}

	public void setPosition( final float _x, final float _y, final float _z )
	{
		FloatBuffer.set( future, POSITION, _x, _y, _z ) ;
	}

	public void addToPosition( final float _x, final float _y, final float _z )
	{
		FloatBuffer.add( future, POSITION, _x, _y, _z ) ;
	}

	public Vector3 getPosition( final Vector3 _fill )
	{
		return FloatBuffer.fill( present, _fill, POSITION ) ;
	}

	public Vector2 getPosition( final Vector2 _fill )
	{
		return FloatBuffer.fill( present, _fill, POSITION ) ;
	}

	public void setOffsetInstant( final float _x, final float _y, final float _z )
	{
		FloatBuffer.set( old, OFFSET, _x, _y, _z ) ;
		FloatBuffer.set( present, OFFSET, _x, _y, _z ) ;
		FloatBuffer.set( future, OFFSET, _x, _y, _z ) ;
	}

	public void setOffset( final float _x, final float _y, final float _z )
	{
		FloatBuffer.set( future, OFFSET, _x, _y, _z ) ;
	}

	public void addToOffset( final float _x, final float _y, final float _z )
	{
		FloatBuffer.add( future, OFFSET, _x, _y, _z ) ;
	}

	public Vector3 getOffset( final Vector3 _fill )
	{
		return FloatBuffer.fill( present, _fill, OFFSET ) ;
	}

	public Vector2 getOffset( final Vector2 _fill )
	{
		return FloatBuffer.fill( present, _fill, OFFSET ) ;
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

	public Vector3 getRotation( final Vector3 _fill )
	{
		return FloatBuffer.fill( present, _fill, ROTATION ) ;
	}

	public void setScaleInstant( final float _x, final float _y, final float _z )
	{
		FloatBuffer.set( old, SCALE, _x, _y, _z ) ;
		FloatBuffer.set( present, SCALE, _x, _y, _z ) ;
		FloatBuffer.set( future, SCALE, _x, _y, _z ) ;
	}

	public void setScale( final float _x, final float _y, final float _z )
	{
		FloatBuffer.set( future, SCALE, _x, _y, _z ) ;
	}

	public Vector3 getScale( final Vector3 _fill )
	{
		return FloatBuffer.fill( present, _fill, SCALE ) ;
	}

	public Vector2 getScale( final Vector2 _fill )
	{
		return FloatBuffer.fill( present, _fill, SCALE ) ;
	}

	/**
		Update the draw object state.
		Returns true if the state has changed, false if the 
		state has not changed.
	*/
	@Override
	public boolean update( Interpolation _mode, final int _diff, final int _iteration )
	{
		boolean update = false ;

		// Position, Rotation, and Scale should always 
		// be updated even if the data is not being uploaded 
		// to the GPU.
		switch( _mode )
		{
			case LINEAR :
			{
				update |= Interpolate.linear( future, old, present, _diff, _iteration ) ;
				break ;
			}
			case NONE   :
			default     :
			{
				update = false ;
				FloatBuffer.copy( future, old ) ;
				FloatBuffer.copy( future, present ) ;
				break ;
			}
		}

		return update ;
	}

	/**
		Extend the meta interface when you have information
		you want to bundle along with the Draw object.
		This meta information could be used for a variety
		of purposes, such as: occlusion or identify the parent.
	*/
	public interface IMeta
	{
		/**
			It's possible the developer will define multiple
			meta classes for different draw object use-cases.
			Each class should return a unique int that can be
			used to cast the object to the correct definition.
		*/
		public int getType() ;
	}
}
