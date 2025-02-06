package com.linxonline.mallet.renderer.desktop.opengl ;

import java.nio.* ;

import com.linxonline.mallet.renderer.IShape ;

public final class GLIndexWrite implements IShape.IIndexWrite
{
	private IntBuffer buffer ;

	public GLIndexWrite( final IntBuffer _buffer )
	{
		buffer = _buffer ;
	}

	public void set( final IntBuffer _buffer )
	{
		buffer = _buffer ;
	}

	@Override
	public void put( final int _val )
	{
		buffer.put( _val ) ;
	}

	@Override
	public void put( final int[] _val )
	{
		buffer.put( _val ) ;
	}

	@Override
	public void put( final int[] _val, final int _offset, final int _length )
	{
		buffer.put( _val, _offset, _length ) ;
	}
}
