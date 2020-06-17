package com.linxonline.mallet.io.serialisation ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

/**
	ByteOutput is a simple writer.
	
	It allows the developer to quickly write different variable types 
	to the stream designated by ByteOutput.
	
	The size of the stream increases as variables are written. Current 
	implementation is ineffecient and generates a new byte array for each 
	write call.
*/
public class ByteOutput implements Serialise.Out
{
	private byte[] stream = new byte[0] ;

	/**
		Add 4-byte int to stream. 
	*/
	public void writeInt( final int _int )
	{
		stream = concat( stream, ConvertBytes.toBytes( _int, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	/**
		Add 2-byte short to stream. 
	*/
	public void writeShort( final short _short )
	{
		stream = concat( stream, ConvertBytes.toBytes( _short, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	/**
		Add a byte to stream. 
	*/
	public void writeByte( final byte _byte )
	{
		stream = concat( stream, ConvertBytes.toBytes( _byte, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	/**
		Add 2-byte char to stream. 
	*/
	public void writeChar( final char _char )
	{
		stream = concat( stream, ConvertBytes.toBytes( _char, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	/**
		Add 8-byte long to stream. 
	*/
	public void writeLong( final long _long )
	{
		stream = concat( stream, ConvertBytes.toBytes( _long, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	/**
		Add 4-byte float to stream. 
	*/
	public void writeFloat( final float _float )
	{
		stream = concat( stream, ConvertBytes.toBytes( _float, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	/**
		Add 8-byte double to stream. 
	*/
	public void writeDouble( final double _double )
	{
		stream = concat( stream, ConvertBytes.toBytes( _double, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	/**
		Add 4-byte int matching String byte length. 
		Add string byte array to stream.
	*/
	public void writeString( final String _string )
	{
		final byte[] array = _string.getBytes() ;
		writeInt( array.length ) ;					// Use the Bytes array length
		stream = concat( stream, array ) ;
	}

	/**
		Add 1-byte boolean to stream.
	*/
	public void writeBoolean( final boolean _bool )
	{
		stream = concat( stream, ConvertBytes.toBytes( _bool, ConvertBytes.BIG_ENDIAN) ) ;
	}

	/**
		Add 4-byte int matching int-array length. 
		Add int array to stream.
	*/
	public void writeInts( final int[] _int )
	{
		writeInt( _int.length ) ;
		stream = concat( stream, ConvertBytes.toBytes( _int, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	/**
		Add 4-byte int matching byte-array length. 
		Add byte array to stream.
	*/
	public void writeBytes( final byte[] _byte )
	{
		writeInt( _byte.length ) ;
		stream = concat( stream, _byte ) ;
	}

	/**
		Add 4-byte int matching char-array length. 
		Add char array to stream.
	*/
	public void writeChars( final char[] _char )
	{
		writeInt( _char.length ) ;
		stream = concat( stream, ConvertBytes.toBytes( _char, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	/**
		Add 4-byte int matching long-array length. 
		Add long array to stream.
	*/
	public void writeLongs( final long[] _long )
	{
		writeInt( _long.length ) ;
		stream = concat( stream, ConvertBytes.toBytes( _long, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	/**
		Add 4-byte int matching float-array length. 
		Add string byte array to stream.
	*/
	public void writeFloats( final float[] _float )
	{
		writeInt( _float.length ) ;
		stream = concat( stream, ConvertBytes.toBytes( _float, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	/**
		Add 4-byte int matching double-array length. 
		Add double array to stream.
	*/
	public void writeDoubles( final double[] _double )
	{
		writeInt( _double.length ) ;
		stream = concat( stream, ConvertBytes.toBytes( _double, ConvertBytes.BIG_ENDIAN ) ) ;
	}

	/**
		Add 4-byte int matching String-array length. 
		Add string byte arrays to stream.
	*/
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
		Add 4-byte int matching boolean-array length. 
		Add boolean array to stream.
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

	/**
		Combine the two byte-arrays passed in.
		Creates a new array of a.length + b.length long.
		Copies the arrays sequentially into the new byte-array.
	*/
	private byte[] concat( final byte[] _a, final byte[] _b )
	{
		final byte[] c = new byte[_a.length + _b.length] ;
		System.arraycopy( _a, 0, c, 0, _a.length ) ;
		System.arraycopy( _b, 0, c, _a.length, _b.length ) ;
		return c ;
	}

	/**
		Returns the ByteOuput's byte-stream.
	**/
	public byte[] getBytes()
	{
		return stream ;
	}
}
