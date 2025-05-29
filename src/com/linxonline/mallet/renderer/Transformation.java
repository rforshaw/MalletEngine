package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Matrix4 ;

import com.linxonline.mallet.util.Interpolate ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

public final class Transformation implements IUpdate
{
	private static final float PI = ( float )Math.PI ;
	private static final float PI2 = ( float )Math.PI * 2.0f ;

	private static final int POSITION = 0 ;
	private static final int OFFSET   = 3 ;
	private static final int ROTATION = 6 ;
	private static final int SCALE    = 9 ;

	// Each contain Position, Rotation, and Scale
	private final float[] present = FloatBuffer.allocate( 12 ) ;
	private final float[] future = FloatBuffer.allocate( 12 ) ;

	public Transformation()
	{
		this( 0.0f, 0.0f, 0.0f,
			  0.0f, 0.0f, 0.0f ) ;
	}

	public Transformation( final float _posX, final float _posY, final float _posZ )
	{
		this( _posX, _posY, _posZ,
			  0.0f, 0.0f, 0.0f ) ;
	}
	
	public Transformation( final float _posX, final float _posY, final float _posZ,
						   final float _offX, final float _offY, final float _offZ )
	{
		this( _posX, _posY, _posZ,
			  _offX, _offY, _offZ,
			  0.0f, 0.0f, 0.0f ) ;
	}

	public Transformation( final float _posX, final float _posY, final float _posZ,
						   final float _offX, final float _offY, final float _offZ,
						   final float _rotX, final float _rotY, final float _rotZ )
	{
		FloatBuffer.set( present, POSITION, _posX, _posY, _posZ ) ;
		FloatBuffer.set( present, OFFSET, _offX, _offY, _offZ ) ;
		FloatBuffer.set( present, ROTATION, _rotX, _rotY, _rotZ ) ;
		FloatBuffer.set( present, SCALE, 1.0f, 1.0f, 1.0f ) ;

		FloatBuffer.set( future, POSITION, _posX, _posY, _posZ ) ;
		FloatBuffer.set( future, OFFSET, _offX, _offY, _offZ ) ;
		FloatBuffer.set( future, ROTATION, _rotX, _rotY, _rotZ ) ;
		FloatBuffer.set( future, SCALE, 1.0f, 1.0f, 1.0f ) ;
	}

	public void setPositionInstant( final float _x, final float _y, final float _z )
	{
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

	public void setRotation( float _x, float _y, float _z )
	{
		float oX = FloatBuffer.get( present, ROTATION + 0 ) ;
		float oY = FloatBuffer.get( present, ROTATION + 1 ) ;
		float oZ = FloatBuffer.get( present, ROTATION + 2 ) ;

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

		FloatBuffer.set( present, ROTATION, oX, oY, oZ ) ;
		FloatBuffer.set( future, ROTATION, _x, _y, _z ) ;
	}

	public Vector3 getRotation( final Vector3 _fill )
	{
		return FloatBuffer.fill( present, _fill, ROTATION ) ;
	}

	public void setScaleInstant( final float _x, final float _y, final float _z )
	{
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

	public Matrix4 getTransformation( final Matrix4 _mat )
	{
		final float px = present[POSITION] + present[OFFSET] ;
		final float py = present[POSITION + 1] + present[OFFSET + 1] ;
		final float pz = present[POSITION + 2] + present[OFFSET + 2] ;

		final float rx = present[ROTATION] ;
		final float ry = present[ROTATION + 1] ;
		final float rz = present[ROTATION + 2] ;

		final float sx = present[SCALE] ;
		final float sy = present[SCALE + 1] ;
		final float sz = present[SCALE + 2] ;

		_mat.applyTransformations( px, py, pz, rx, ry, rz, sx, sy, sz ) ;
		return _mat ;
	}

	/**
		Update the draw object state.
		Returns true if the state has changed, false if the 
		state has not changed.
	*/
	@Override
	public boolean update( Interpolation _mode, final float _coefficient )
	{
		boolean update = false ;

		// Position, Rotation, and Scale should always 
		// be updated even if the data is not being uploaded 
		// to the GPU.
		switch( _mode )
		{
			case LINEAR :
			{
				if(Interpolate.linear( future, present, _coefficient ))
				{
					update = true ;
				}
				break ;
			}
			case NONE   :
			default     :
			{
				update = false ;
				FloatBuffer.copy( future, present ) ;
				break ;
			}
		}

		return update ;
	}
}
