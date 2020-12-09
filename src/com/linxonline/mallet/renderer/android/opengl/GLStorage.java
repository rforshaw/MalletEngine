package com.linxonline.mallet.renderer.android.opengl ;

import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.io.serialisation.Serialise ;

public class GLStorage implements Serialise.Out
{
	public final int[] id = new int[1] ;

	private java.nio.ByteBuffer byteBuffer ;
	private byte[] buffer ;
	private int offset = 0 ;
	private boolean stable = false ;

	public GLStorage( final Storage _storage )
	{
		final Storage.IData data = _storage.getData() ;
		final int lengthInBytes = data.getLength() ;

		MGL.glGenBuffers( 1, id, 0 ) ;

		buffer = new byte[lengthInBytes] ;
		byteBuffer = java.nio.ByteBuffer.allocateDirect( lengthInBytes ) ;
		byteBuffer.order( java.nio.ByteOrder.nativeOrder() ) ;
	}

	public boolean update( final Storage _storage )
	{
		stable = false ;

		final Storage.IData data = _storage.getData() ;
		final int lengthInBytes = data.getLength() ;

		offset = 0 ;
		if( buffer.length < lengthInBytes )
		{
			buffer = new byte[lengthInBytes] ;
			byteBuffer = java.nio.ByteBuffer.allocateDirect( lengthInBytes ) ;
			byteBuffer.order( java.nio.ByteOrder.nativeOrder() ) ;
		}

		data.serialise( this ) ;

		byteBuffer.position( 0 ) ;
		byteBuffer.put( buffer ) ;
		byteBuffer.position( 0 ) ;

		MGL.glBindBuffer( MGL.GL_SHADER_STORAGE_BUFFER, id[0] ) ;
		MGL.glBufferData( MGL.GL_SHADER_STORAGE_BUFFER, lengthInBytes, byteBuffer, MGL.GL_DYNAMIC_DRAW ) ;

		// We successfully updated the buffer, nothing more is need 
		// but to inform the trigger.
		stable = true ;
		return stable ;
	}

	public void writeInt( final int _int )
	{
		ConvertBytes.toBytes( _int, offset, buffer ) ;
		offset += ConvertBytes.INT_SIZE ;
	}

	public void writeByte( final byte _byte )
	{
		ConvertBytes.toBytes( _byte, offset, buffer ) ;
		offset += ConvertBytes.BYTE_SIZE ;
	}

	public void writeChar( final char _char )
	{
		ConvertBytes.toBytes( _char, offset, buffer ) ;
		offset += ConvertBytes.CHAR_SIZE ;
	}

	public void writeLong( final long _long )
	{
		ConvertBytes.toBytes( _long, offset, buffer ) ;
		offset += ConvertBytes.LONG_SIZE ;
	}

	public void writeFloat( final float _float )
	{
		ConvertBytes.toBytes( _float, offset, buffer ) ;
		offset += ConvertBytes.FLOAT_SIZE ;
	}

	public void writeDouble( final double _double )
	{
		ConvertBytes.toBytes( _double, offset, buffer ) ;
		offset += ConvertBytes.DOUBLE_SIZE ;
	}

	public void writeString( final String _string )
	{
		//ConvertBytes.toBytes( _string, offset, buffer ) ;
		//offset += ConvertBytes.CHAR_SIZE * _string.length ;
	}

	public void writeBoolean( final boolean _bool )
	{
		ConvertBytes.toBytes( _bool, offset, buffer ) ;
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
		//ConvertBytes.toBytes( _float, offset, buffer ) ;
		//offset += ConvertBytes.INT_SIZE * _float.length ;
		throw new UnsupportedOperationException() ;
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
