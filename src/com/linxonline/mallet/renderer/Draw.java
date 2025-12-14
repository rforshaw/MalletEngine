package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Matrix4 ;

import com.linxonline.mallet.util.Interpolate ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

public final class Draw implements IUpdate
{
	// Each contain Position, Rotation, and Scale
	private final Transformation trans ;

	private IShape[] shapes ;
	private IMeta meta = IMeta.EMPTY_META ;
	private boolean hidden = false ;

	// We don't want to construct a map for uniforms unless the client
	// has some uniforms for the draw object.
	private StructUniform uniforms = null ;

	public Draw()
	{
		this( 0.0f, 0.0f, 0.0f,
			  0.0f, 0.0f, 0.0f ) ;
	}

	public Draw( final float _posX, final float _posY, final float _posZ )
	{
		this( _posX, _posY, _posZ,
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
		this( null,
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
		trans = new Transformation( _posX, _posY, _posZ,
									_offX, _offY, _offZ,
									_rotX, _rotY, _rotZ ) ;
	}

	/**
		Associate the draw object with further information.
		This could be used for a variety of purposes specific
		to the developers own use-cases.
	*/
	public <T extends IMeta> T setMeta( final T _meta )
	{
		meta = ( _meta != null ) ? _meta : IMeta.EMPTY_META ;
		return _meta ;
	}

	/**
		Return the meta object specified by the developer, or null
		if nothing has been set.
	*/
	public IMeta getMeta()
	{
		return meta ;
	}

	public boolean removeUniform( final String _handler )
	{
		if( uniforms == null )
		{
			// Can't remove something when the map
			// doesn't exist in the first place.
			return false ;
		}
	
		return uniforms.remove( _handler ) ;
	}

	public boolean addUniform( final String _handler, final IUniform _uniform )
	{
		final boolean success = switch( _uniform )
		{
			// We don't want to support complex structures
			// while using uniforms on draw objects.
			case StructUniform su -> false ;
			case ArrayUniform au -> false ;
			default -> true ;
		} ;

		if( !success )
		{
			return false ;
		}

		if( uniforms == null )
		{
			uniforms = new StructUniform() ;
		}

		return uniforms.add( _handler, _uniform ) ;
	}

	/**
		Return the mapped object associated with _id.
	*/
	public IUniform getUniform( final String _id )
	{
		if( uniforms == null )
		{
			return null ;
		}

		return uniforms.get( _id ) ;
	}

	public IUniform getUniform( final int _index )
	{
		return uniforms.get( _index ) ;
	}

	public int uniformSize()
	{
		return ( uniforms == null ) ? 0 : uniforms.size() ;
	}
	
	public boolean isUniformsEmpty()
	{
		return uniforms == null || uniforms.isEmpty() ;
	}

	public IShape[] getShapes()
	{
		return shapes ;
	}

	/**
		It's likely that the majority of Draw objects will 
		contain only one shape.
	*/
	public <T extends IShape> T setShape( final T _shape )
	{
		if( shapes == null )
		{
			shapes = new IShape[1] ;
		}

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

	public void setPositionInstant( final float _x, final float _y, final float _z )
	{
		trans.setPositionInstant( _x, _y, _z ) ;
	}

	public void setPosition( final float _x, final float _y, final float _z )
	{
		trans.setPosition( _x, _y, _z ) ;
	}

	public void addToPosition( final float _x, final float _y, final float _z )
	{
		trans.addToPosition( _x, _y, _z ) ;
	}

	public Vector3 getPosition( final Vector3 _fill )
	{
		return trans.getPosition( _fill ) ;
	}

	public Vector2 getPosition( final Vector2 _fill )
	{
		return trans.getPosition( _fill ) ;
	}

	public void setOffsetInstant( final float _x, final float _y, final float _z )
	{
		trans.setOffsetInstant( _x, _y, _z ) ;
	}

	public void setOffset( final float _x, final float _y, final float _z )
	{
		trans.setOffset( _x, _y, _z ) ;
	}

	public void addToOffset( final float _x, final float _y, final float _z )
	{
		trans.addToOffset( _x, _y, _z ) ;
	}

	public Vector3 getOffset( final Vector3 _fill )
	{
		return trans.getOffset( _fill ) ;
	}

	public Vector2 getOffset( final Vector2 _fill )
	{
		return trans.getOffset( _fill ) ;
	}

	public void setRotation( final float _x, final float _y, final float _z )
	{
		trans.setRotation( _x, _y, _z ) ;
	}

	public Vector3 getRotation( final Vector3 _fill )
	{
		return trans.getRotation( _fill ) ;
	}

	public void setScaleInstant( final float _x, final float _y, final float _z )
	{
		trans.setScaleInstant( _x, _y, _z ) ;
	}

	public void setScale( final float _x, final float _y, final float _z )
	{
		trans.setScale( _x, _y, _z ) ;
	}

	public Vector3 getScale( final Vector3 _fill )
	{
		return trans.getScale( _fill ) ;
	}

	public Vector2 getScale( final Vector2 _fill )
	{
		return trans.getScale( _fill ) ;
	}

	public Matrix4 getTransformation( final Matrix4 _mat )
	{
		return trans.getTransformation( _mat ) ;
	}

	/**
		Update the draw object state.
		Returns true if the state has changed, false if the 
		state has not changed.
	*/
	@Override
	public boolean update( Interpolation _mode, final float _coefficient )
	{
		return trans.update( _mode, _coefficient ) ;
	}
}
