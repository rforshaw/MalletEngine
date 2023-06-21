package com.linxonline.mallet.renderer.web.gl ;

import org.teavm.jso.webgl.WebGLBuffer ;

import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.io.serialisation.Serialise ;

public final class GLStorage implements Storage.ISerialise
{
	public final WebGLBuffer[] id = new WebGLBuffer[1] ;

	private float[] buffer ;
	private boolean stable = false ;

	public GLStorage( final Storage _storage )
	{
		final Storage.IData data = _storage.getData() ;

		final int lengthInBytes = data.getLength() ;
		final int lengthInFloats = lengthInBytes / 4 ;

		id[0] = MGL.createBuffer() ;

		buffer = new float[lengthInFloats] ;
	}

	public boolean update( final Storage _storage )
	{
		stable = false ;

		final Storage.IData data = _storage.getData() ;

		final int lengthInBytes = data.getLength() ;
		final int lengthInFloats = lengthInBytes / 4 ;

		if( buffer.length < lengthInFloats )
		{
			buffer = new float[lengthInFloats] ;
		}

		data.serialise( this ) ;

		//MGL.glBindBuffer( MGL.GL_SHADER_STORAGE_BUFFER, id[0] ) ;
		//MGL.glBufferData( MGL.GL_SHADER_STORAGE_BUFFER, lengthInBytes, floatBuffer, MGL.GL_DYNAMIC_COPY ) ;

		// We successfully updated the buffer, nothing more is need 
		// but to inform the trigger.
		stable = true ;
		return stable ;
	}

	public void shutdown()
	{
		MGL.deleteBuffer( id[0] ) ;
	}

	@Override
	public int writeInt( final int _offset, final int _val )
	{
		final int index = _offset / 4 ;
		//buffer.put( index, _val ) ;
		return _offset + 4 ;
	}

	@Override
	public int writeFloat( final int _offset, final float _val )
	{
		final int index = _offset / 4 ;
		//buffer.put( index, _val ) ;
		return _offset + 4 ;
	}

	@Override
	public int writeFloats( final int _offset, final float[] _val )
	{
		final int index = _offset / 4 ;
		//buffer.put( index, _val ) ;
		return _offset + ( _val.length * 4 ) ;
	}

	@Override
	public int writeVec2( final int _offset, final float _x, final float _y )
	{
		int index = _offset / 4 ;

		//buffer.put( index, _x ) ;
		//buffer.put( ++index, _y ) ;

		return _offset + 8 ;
	}

	@Override
	public int writeVec3( final int _offset, final float _x, final float _y, final float _z )
	{
		int index = _offset / 4 ;

		//buffer.put( index, _x ) ;
		//buffer.put( ++index, _y ) ;
		//buffer.put( ++index, _z ) ;

		return _offset + 12 ;
	}

	@Override
	public int writeVec4( final int _offset, final float _x, final float _y, final float _z, final float _w )
	{
		int index = _offset / 4 ;

		//buffer.put( index, _x ) ;
		//buffer.put( ++index, _y ) ;
		//buffer.put( ++index, _z ) ;
		//buffer.put( ++index, _w ) ;

		return _offset + 16 ;
	}
}
