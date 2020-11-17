package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.util.Interpolate ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

public class Draw implements IUpdate
{
	private static final float PI = ( float )Math.PI ;
	private static final float PI2 = ( float )Math.PI * 2.0f ;

	private static final int POSITION = 0 ;
	private static final int OFFSET   = 3 ;
	private static final int ROTATION = 6 ;
	private static final int SCALE    = 9 ;

	private boolean dirty = false ;
	private boolean hidden = false ;
	private Shape shape = null ;
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

	public void setHidden( final boolean _hide )
	{
		hidden = _hide ;
	}

	public boolean isHidden()
	{
		return hidden ;
	}

	public Shape setShape( final Shape _shape )
	{
		shape = _shape ;
		return shape ;
	}

	public Shape getShape()
	{
		return shape ;
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

	public void setPosition( final float _x, final float _y, final float _z )
	{
		FloatBuffer.set( future, POSITION, _x, _y, _z ) ;
	}

	public Vector3 getPosition( final Vector3 _fill )
	{
		return FloatBuffer.fill( present, _fill, POSITION ) ;
	}

	public void setOffset( final float _x, final float _y, final float _z )
	{
		FloatBuffer.set( future, OFFSET, _x, _y, _z ) ;
	}

	public Vector3 getOffset( final Vector3 _fill )
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

	public void setScale( final float _x, final float _y, final float _z )
	{
		FloatBuffer.set( future, SCALE, _x, _y, _z ) ;
	}

	public Vector3 getScale( final Vector3 _fill )
	{
		return FloatBuffer.fill( present, _fill, SCALE ) ;
	}

	@Override
	public boolean update( Interpolation _mode, final int _diff, final int _iteration )
	{
		boolean update = dirty ;
		dirty = false ;

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
				FloatBuffer.copy( future, old ) ;
				FloatBuffer.copy( future, present ) ;
				break ;
			}
		}

		return update ;
	}

	/**
		Force the draw objects rendering state to be updated,
		irrspective of whether the update() thinks the state 
		needs to be updated.
	*/
	public void makeDirty()
	{
		dirty = true ;
	}
}
