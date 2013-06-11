package com.linxonline.mallet.io.serialisation ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

/**
	ByteInput is a simple byte reader.
	
	It assumes the developer knows in what order to read the
	format. Allowing it to be exceptionally quick and verbose free.
**/
public class ByteInput implements SerialiseInput
{
	private byte[] stream = null ;
	private int position = 0 ;

	protected ByteInput( final byte[] _stream )
	{
		stream = _stream ;
	}

	public static ByteInput readStream( final byte[] _stream )
	{
		return new ByteInput( _stream ) ;
	}

	public int readInt()
	{
		return ConvertBytes.toInt( stream, increment( 4 ), 4 ) ;
	}

	public byte readByte()
	{
		final byte[] b = new byte[1] ;
		return b[0] ;
	}

	public char readChar()
	{
		return ConvertBytes.toChar( stream, increment( 2 ), 2 ) ;
	}

	public long readLong()
	{
		return ConvertBytes.toLong( stream, increment( 8 ), 8 ) ;
	}

	public float readFloat()
	{
		return ConvertBytes.toFloat( stream, increment( 4 ), 4 ) ;
	}

	public double readDouble()
	{
		return ConvertBytes.toDouble( stream, increment( 8 ), 8 ) ;
	}

	public String readString()
	{
		final int length = readInt() ;
		return new String( ConvertBytes.toBytes( stream, increment( length ), length ) ) ;
	}

	public boolean readBoolean()
	{
		return ConvertBytes.toBoolean( stream, increment( 1 ), 0 ) ;
	}

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

	public byte[] readBytes()
	{
		final int length = readInt() ;
		return ConvertBytes.toBytes( stream, increment( length ), length ) ;
	}

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

	/**
		Increment the position by X, but return the position before adding X.
	**/
	private int increment( int _x )
	{
		final int pos = position ;
		position += _x ;
		return pos ;
	}
}