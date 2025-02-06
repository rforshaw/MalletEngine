package com.linxonline.mallet.renderer.desktop.opengl ;

import java.nio.ByteOrder ;
import java.nio.ByteBuffer ;

import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;
import com.linxonline.mallet.util.tools.ConvertBytes ;

public final class GLStorage implements Storage.ISerialise
{
	private final int BACK_BUFFERS_NUM = 2 ;

	private final int[] id = new int[BACK_BUFFERS_NUM] ;
	private final long[] fences = new long[BACK_BUFFERS_NUM] ;

	private java.nio.FloatBuffer buffer ;
	private int front = 0 ;

	private boolean stable = false ;

	public GLStorage( final Storage _storage )
	{
		final Storage.IData data = _storage.getData() ;

		final int lengthInBytes = data.getLength() ;
		final int lengthInFloats = lengthInBytes / 4 ;

		MGL.glGenBuffers( id.length, id, 0 ) ;

		final ByteBuffer buf = ByteBuffer.allocateDirect( lengthInBytes ) ;
		buf.order( ByteOrder.nativeOrder() ) ;

		buffer = buf.asFloatBuffer() ;

		upload( _storage, front ) ;
	}

	public boolean update( final Storage _storage )
	{
		final int next = ( front + 1 ) % BACK_BUFFERS_NUM ;

		final long sync = fences[next] ;
		if( sync != 0 )
		{
			// We are currently uploading something to the GPU
			// Return false to inform the caller to try again later.
			return false ;
		}

		// The next-buffer is not being used, so we
		// can now upload new content.
		upload( _storage, next ) ;
		return true ;
	}

	private void upload( final Storage _storage, final int _index )
	{
		final Storage.IData data = _storage.getData() ;

		final int lengthInBytes = data.getLength() ;
		final int lengthInFloats = lengthInBytes / 4 ;

		if( buffer.capacity() < lengthInFloats )
		{
			final ByteBuffer buf = ByteBuffer.allocateDirect( lengthInBytes ) ;
			buf.order( ByteOrder.nativeOrder() ) ;

			buffer = buf.asFloatBuffer() ;
		}

		data.serialise( this ) ;
		buffer.position( 0 ) ;

		MGL.glBindBuffer( MGL.GL_SHADER_STORAGE_BUFFER, id[_index] ) ;
		MGL.glBufferData( MGL.GL_SHADER_STORAGE_BUFFER, lengthInBytes, buffer, MGL.GL_DYNAMIC_COPY ) ;

		fences[_index] = MGL.glFenceSync( MGL.GL_SYNC_GPU_COMMANDS_COMPLETE, 0 ) ;
	}

	private void makeFront( final int _next )
	{
		MGL.glDeleteSync( fences[front] ) ;
		fences[front] = 0 ;
		front = _next ;
	}

	private boolean isCompleted( final long _sync )
	{
		return MGL.glClientWaitSync( _sync, 0, 0 ) == MGL.GL_ALREADY_SIGNALED ;
	}

	public boolean hasValidUpload()
	{
		return isCompleted( fences[front] ) ;
	}

	public int getID()
	{
		// Something wants to make use of our buffer, let's
		// check the next-buffer to see if it has anything.
		final int next = ( front + 1 ) % BACK_BUFFERS_NUM ;
		final long sync = fences[next] ;
		if( sync != 0 && isCompleted( sync ) )
		{
			// It's been flagged as having a sync object
			// We'll flip it to our front buffer now.
			makeFront( next ) ;
		}

		return id[front] ;
	}

	public void shutdown()
	{
		MGL.glDeleteBuffers( id.length, id, 0 ) ;
		for( int i = 0; i < fences.length; ++i )
		{
			MGL.glDeleteSync( fences[i] ) ;
		}
	}

	@Override
	public int writeInt( final int _offset, final int _val )
	{
		final int index = _offset / 4 ;
		buffer.put( index, _val ) ;
		return _offset + 4 ;
	}

	@Override
	public int writeFloat( final int _offset, final float _val )
	{
		final int index = _offset / 4 ;
		buffer.put( index, _val ) ;
		return _offset + 4 ;
	}

	@Override
	public int writeFloats( final int _offset, final float[] _val )
	{
		final int index = _offset / 4 ;
		buffer.put( index, _val ) ;
		return _offset + ( _val.length * 4 ) ;
	}

	@Override
	public int writeVec2( final int _offset, final float _x, final float _y )
	{
		int index = _offset / 4 ;

		buffer.put( index, _x ) ;
		buffer.put( ++index, _y ) ;

		return _offset + 8 ;
	}

	@Override
	public int writeVec3( final int _offset, final float _x, final float _y, final float _z )
	{
		int index = _offset / 4 ;

		buffer.put( index, _x ) ;
		buffer.put( ++index, _y ) ;
		buffer.put( ++index, _z ) ;

		return _offset + 12 ;
	}

	@Override
	public int writeVec4( final int _offset, final float _x, final float _y, final float _z, final float _w )
	{
		int index = _offset / 4 ;

		buffer.put( index, _x ) ;
		buffer.put( ++index, _y ) ;
		buffer.put( ++index, _z ) ;
		buffer.put( ++index, _w ) ;

		return _offset + 16 ;
	}
}
