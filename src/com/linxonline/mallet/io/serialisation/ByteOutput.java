package com.linxonline.mallet.io.serialisation ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

public class ByteOutput implements SerialiseOutput
{
	private byte[] stream = new byte[0] ;

	public void writeInt( final int _int )
	{
		stream = concat( stream, ConvertBytes.toBytes( _int, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	public void writeByte( final byte _byte )
	{
		stream = concat( stream, ConvertBytes.toBytes( _byte, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	public void writeChar( final char _char )
	{
		stream = concat( stream, ConvertBytes.toBytes( _char, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	public void writeLong( final long _long )
	{
		stream = concat( stream, ConvertBytes.toBytes( _long, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	public void writeFloat( final float _float )
	{
		stream = concat( stream, ConvertBytes.toBytes( _float, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	public void writeDouble( final double _double )
	{
		stream = concat( stream, ConvertBytes.toBytes( _double, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	public void writeString( final String _string )
	{
		final byte[] array = _string.getBytes() ;
		writeInt( array.length ) ;					// Use the Bytes array length
		stream = concat( stream, array ) ;
	}

	public void writeBoolean( final boolean _bool )
	{
		stream = concat( stream, ConvertBytes.toBytes( _bool, ConvertBytes.BIG_ENDIAN) ) ;
	}

	public void writeInts( final int[] _int )
	{
		writeInt( _int.length ) ;
		stream = concat( stream, ConvertBytes.toBytes( _int, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	public void writeBytes( final byte[] _byte )
	{
		writeInt( _byte.length ) ;
		stream = concat( stream, _byte ) ;
	}

	public void writeChars( final char[] _char )
	{
		writeInt( _char.length ) ;
		stream = concat( stream, ConvertBytes.toBytes( _char, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	public void writeLongs( final long[] _long )
	{
		writeInt( _long.length ) ;
		stream = concat( stream, ConvertBytes.toBytes( _long, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	public void writeFloats( final float[] _float )
	{
		writeInt( _float.length ) ;
		stream = concat( stream, ConvertBytes.toBytes( _float, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	public void writeDoubles( final double[] _double )
	{
		writeInt( _double.length ) ;
		stream = concat( stream, ConvertBytes.toBytes( _double, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	public void writeStrings( final String[] _string )
	{
		final int length = _string.length ;
		writeInt( length ) ;
		for( int i = 0; i < length; ++i )
		{
			writeString( _string[i] ) ;
		}
	}

	/**
		Horribly ineffecient, DONT USE.
	**/
	public void writeBooleans( final boolean[] _bool )
	{
		final int length = _bool.length ;
		writeInt( length ) ;
		for( int i = 0; i < length; ++i )
		{
			writeBoolean( _bool[i] ) ;
		}
	}

	private byte[] concat( final byte[] _a, final byte[] _b )
	{
		final byte[] c = new byte[_a.length + _b.length] ;
		System.arraycopy( _a, 0, c, 0, _a.length ) ;
		System.arraycopy( _b, 0, c, _a.length, _b.length ) ;
		return c ;
	}

	/**
		Return the byte stream that represents the serialised form of this object/s.
	**/
	public byte[] getBytes()
	{
		return stream ;
	}
}