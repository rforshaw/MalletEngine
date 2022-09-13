package com.linxonline.mallet.renderer.desktop.opengl ;

import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.io.serialisation.Serialise ;

import com.jogamp.common.nio.Buffers ;

public final class GLStorage implements Serialise.Out
{
	public final int[] id = new int[1] ;

	private float[] buffer ;
	private java.nio.FloatBuffer floatBuffer ;
	private int offset = 0 ;
	private boolean stable = false ;

	public GLStorage( final Storage _storage )
	{
		final Storage.IData data = _storage.getData() ;
		final int lengthInBytes = data.getLength() ;

		MGL.glGenBuffers( 1, id, 0 ) ;

		buffer = new float[lengthInBytes / 4] ;
		floatBuffer = Buffers.newDirectFloatBuffer​( buffer ) ;
	}

	public boolean update( final Storage _storage )
	{
		stable = false ;

		final Storage.IData data = _storage.getData() ;
		final int lengthInBytes = data.getLength() ;

		if( buffer.length * 4 < lengthInBytes )
		{
			buffer = new float[lengthInBytes / 4] ;
			floatBuffer = Buffers.newDirectFloatBuffer​( buffer ) ;
		}

		offset = 0 ;
		data.serialise( this ) ;
		floatBuffer.put( buffer ) ;
		floatBuffer.position( 0 ) ;

		MGL.glBindBuffer( MGL.GL_SHADER_STORAGE_BUFFER, id[0] ) ;
		MGL.glBufferData( MGL.GL_SHADER_STORAGE_BUFFER, lengthInBytes, floatBuffer, MGL.GL_DYNAMIC_COPY ) ;

		// We successfully updated the buffer, nothing more is need 
		// but to inform the trigger.
		stable = true ;
		return stable ;
	}

	public void shutdown()
	{
		MGL.glDeleteBuffers( id.length, id, 0 ) ;
	}

	public void writeInt( final int _int )
	{
		buffer[offset] = _int ;
		offset += 1 ;
		//ConvertBytes.toBytes( _int, offset, buffer ) ;
		//offset += ConvertBytes.INT_SIZE ;
	}

	public void writeByte( final byte _byte )
	{
		//ConvertBytes.toBytes( _byte, offset, buffer ) ;
		offset += ConvertBytes.BYTE_SIZE ;
	}

	public void writeChar( final char _char )
	{
		//ConvertBytes.toBytes( _char, offset, buffer ) ;
		offset += ConvertBytes.CHAR_SIZE ;
	}

	public void writeLong( final long _long )
	{
		//ConvertBytes.toBytes( _long, offset, buffer ) ;
		offset += ConvertBytes.LONG_SIZE ;
	}

	public void writeFloat( final float _float )
	{
		//System.out.println( _float + " " + offset ) ;
		buffer[offset] = _float ;
		offset += 1 ;
		//ConvertBytes.toBytes( _float, offset, buffer ) ;
		//System.out.println( ConvertBytes.toFloat( buffer, offset ) ) ;
		//offset += ConvertBytes.FLOAT_SIZE ;
	}

	public void writeDouble( final double _double )
	{
		//ConvertBytes.toBytes( _double, offset, buffer ) ;
		offset += ConvertBytes.DOUBLE_SIZE ;
	}

	public void writeString( final String _string )
	{
		//ConvertBytes.toBytes( _string, offset, buffer ) ;
		//offset += ConvertBytes.CHAR_SIZE * _string.length ;
	}

	public void writeBoolean( final boolean _bool )
	{
		//ConvertBytes.toBytes( _bool, offset, buffer ) ;
		offset += ConvertBytes.BOOLEAN_SIZE ;
	}

	public void writeInts( final int[] _int )
	{
		//ConvertBytes.toBytes( _int, offset, buffer ) ;
		//offset += ConvertBytes.INT_SIZE * _ints.length ;
		throw new UnsupportedOperationException() ;
	}

	public void writeBytes( final byte[] _byte )
	{
		//ConvertBytes.toBytes( _byte, offset, buffer ) ;
		//offset += ConvertBytes.BYTE_SIZE * _byte.length ;
		throw new UnsupportedOperationException() ;
	}

	public void writeChars( final char[] _char )
	{
		//ConvertBytes.toBytes( _char, offset, buffer ) ;
		//offset += ConvertBytes.CHAR_SIZE * _char.length ;
		throw new UnsupportedOperationException() ;
	}

	public void writeLongs( final long[] _long )
	{
		//ConvertBytes.toBytes( _long, offset, buffer ) ;
		//offset += ConvertBytes.LONG_SIZE * _long.length ;
		throw new UnsupportedOperationException() ;
	}

	public void writeFloats( final float[] _float )
	{
		final int size = _float.length ;
		System.arraycopy( _float, 0, buffer, offset, size ) ;
		offset += size ;
	}

	public void writeDoubles( final double[] _double )
	{
		//ConvertBytes.toBytes( _double, offset, buffer ) ;
		//offset += ConvertBytes.DOUBLE_SIZE * _double.length ;
		throw new UnsupportedOperationException() ;
	}

	public void writeStrings( final String[] _string )
	{
		//ConvertBytes.toBytes( _string, offset, buffer ) ;
		//offset += ConvertBytes.INT_SIZE ;
		throw new UnsupportedOperationException() ;
	}

	public void writeBooleans( final boolean[] _bool )
	{
		//ConvertBytes.toBytes( _bool, offset, buffer ) ;
		//offset += ConvertBytes.BOOLEAN_SIZE * _bool.length ;
		throw new UnsupportedOperationException() ;
	}
}
