package com.linxonline.mallet.renderer.desktop.opengl ;

import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

public class GLStorage implements Storage
{
	public final int[] id = new int[1] ;
	private float[] buffer ;

	public GLStorage( final int _size )
	{
		buffer = FloatBuffer.allocate( _size ) ;
	}

	@Override
	public void expand( final int _by )
	{
		buffer = FloatBuffer.expand( buffer, _by ) ;
	}

	@Override
	public float[] getBuffer()
	{
		return buffer ;
	}
}
