package com.linxonline.mallet.io.serialisation ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

/**
	ByteInput is a simple byte reader.

	It assumes the developer knows in what order to read the
	format. Allowing it to be exceptionally quick and verbose free.

	ByteInput increments automatically by the amount of bytes read.
*/
public class ByteInput implements Serialise.In
{
	private byte[] stream ;
	private int position = 0 ;

	protected ByteInput( final byte[] _stream )
	{
		stream = _stream ;
	}

	public static ByteInput readStream( final byte[] _stream )
	{
		return new ByteInput( _stream ) ;
	}

	/**
		Read a 4 byte integer
	*/
	public int readInt()
	{
		return ConvertBytes.toInt( stream, increment( 4 ) ) ;
	}

	/**
		Read 1 byte
	*/
	public byte readByte()
	{
		return stream[increment( 1 )] ;
	}

	/**
		Read a 2 byte char
	*/
	public char readChar()
	{
		return ConvertBytes.toChar( stream, increment( 2 ) ) ;
	}

	/**
		Read a 8 byte long
	*/
	public long readLong()
	{
		return ConvertBytes.toLong( stream, increment( 8 ) ) ;
	}

	/**
		Read a 4 byte float
	*/
	public float readFloat()
	{
		return ConvertBytes.toFloat( stream, increment( 4 ) ) ;
	}

	/**
		Read a 4 byte double
	*/
	public double readDouble()
	{
		return ConvertBytes.toDouble( stream, increment( 8 ) ) ;
	}

	/**
		Read a 4 byte integer to denote string length.
		Read X bytes and convert to String.
	*/
	public String readString()
	{
		final int length = readInt() ;
		return new String( ConvertBytes.toBytes( stream, increment( length ), length ) ) ;
	}

	/**
		Read a 1 byte boolean
	*/
	public boolean readBoolean()
	{
		return ConvertBytes.toBoolean( stream, increment( 1 ) ) ;
	}

	/**
		Read a 4 byte integer to denote array length.
		Iterate array length convert 4-bytes to an int.
	*/
	public int[] readInts()
	{
		final int length = readInt() ;
		final int[] ints = new int[length] ;

		for( int i = 0; i < length; ++i )
		{
			ints[i] = readInt() ;
		}

		return ints ;
	}

	/**
		Read a 4 byte integer to denote array length.
		return byte array denoted by length.
	*/
	public byte[] readBytes()
	{
		final int length = readInt() ;
		return ConvertBytes.toBytes( stream, increment( length ), length ) ;
	}

	/**
		Read a 4 byte integer to denote array length.
		Iterate array length convert 2-bytes to a char.
		Store results in an array.
	*/
	public char[] readChars()
	{
		final int length = readInt() ;
		final char[] chars = new char[length] ;

		for( int i = 0; i < length; ++i )
		{
			chars[i] = readChar() ;
		}

		return chars ;
	}

	/**
		Read a 4 byte integer to denote array length.
		Iterate array length convert 8-bytes to a long.
		Store results in an array.
	*/
	public long[] readLongs()
	{
		final int length = readInt() ;
		final long[] longs = new long[length] ;

		for( int i = 0; i < length; ++i )
		{
			longs[i] = readLong() ;
		}

		return longs ;
	}

	/**
		Read a 4 byte integer to denote array length.
		Iterate array length convert 4-bytes to a float.
		Store results in an array.
	*/
	public float[] readFloats()
	{
		final int length = readInt() ;
		final float[] floats = new float[length] ;

		for( int i = 0; i < length; ++i )
		{
			floats[i] = readFloat() ;
		}

		return floats ;
	}

	/**
		Read a 4 byte integer to denote array length.
		Iterate array length convert 4-bytes to a double.
		Store results in an array.
	*/
	public double[] readDoubles()
	{
		final int length = readInt() ;
		final double[] doubles = new double[length] ;

		for( int i = 0; i < length; ++i )
		{
			doubles[i] = readDouble() ;
		}

		return doubles ;
	}

	public String[] readStrings()
	{
		final int length = readInt() ;
		final String[] strings = new String[length] ;

		for( int i = 0; i < length; ++i )
		{
			strings[i] = readString() ;
		}

		return strings ;
	}

	/**
		Read a 4 byte integer to denote array length.
		Iterate array length convert 1-byte to a boolean.
		Store results in an array.
	*/
	public boolean[] readBooleans()
	{
		final int length = readInt() ;
		final boolean[] bools = new boolean[length] ;

		for( int i = 0; i < length; ++i )
		{
			bools[i] = readBoolean() ;
		}

		return bools ;
	}

	public boolean isEnd()
	{
		return ( position >= ( stream.length - 1 ) ) ;
	}

	/**
		Increment the position by X, but return the position before adding X.
	**/
	private int increment( final int _x )
	{
		final int pos = position ;
		position += _x ;
		return pos ;
	}
}
