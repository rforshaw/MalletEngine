package com.linxonline.mallet.renderer.desktop.opengl ;

import java.nio.* ;

import com.linxonline.mallet.renderer.IShape ;

public final class GLVertWrite implements IShape.IVertWrite
{
	private FloatBuffer buffer ;

	public GLVertWrite( final FloatBuffer _buffer )
	{
		buffer = _buffer ;
	}

	public void set( final FloatBuffer _buffer )
	{
		buffer = _buffer ;
	}

	@Override
	public void put( final float _val )
	{
		buffer.put( _val ) ;
	}

	@Override
	public void put( final float[] _val )
	{
		buffer.put( _val ) ;
	}

	@Override
	public void put( final float[] _val, final int _offset, final int _length )
	{
		buffer.put( _val, _offset, _length ) ;
	}
}
