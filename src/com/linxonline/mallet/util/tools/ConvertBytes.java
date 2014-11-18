package com.linxonline.mallet.util.tools ;

import java.nio.ByteBuffer ;
import java.nio.ByteOrder ;

/**
	Byte = 1 Byte    | Char = 2 Bytes
	Int = 4 Bytes    | Float = 4 Bytes
	Double = 8 Bytes | Long = 8 Bytes.

	When converting a variable to a byte-stream, you must define the endianese. 
	This will detrmine the byte-order of the returned byte-stream.

	When converting a byte-stream to a variable it doe snot convert it to the 
	appropriate endianese of the system.

	Use flipEndian to change the endianese of a byte-stream or sub-set of a byte-stream.
	If the byte-stream contains multiple variables, then the variable positions will also
	be invereted.
*/
public final class ConvertBytes
{
	public static final int LITTLE_ENDIAN = 1 ;
	public static final int BIG_ENDIAN = 2 ;

	private ConvertBytes() {}

	/**
		Returns the platforms native Endianese
	*/
	public static int nativeOrder()
	{
		// If BIG_ENDIAN return BIG_ENDIAN else return LITTLE_ENDIAN
		return ( ByteOrder.BIG_ENDIAN == ByteOrder.nativeOrder() ) ? BIG_ENDIAN : LITTLE_ENDIAN ;
	}

	/**VARIABLE to BYTE ARRAY**/

	public static byte[] toBytes( final int _int, final int _endian )
	{
		return allocate( 4, _endian ).putInt( _int ).array() ;
	}

	public static byte[] toBytes( final byte _byte, final int _endian )
	{
		return allocate( 1, _endian ).put( _byte ).array() ;
	}

	public static byte[] toBytes( final char _char, final int _endian )
	{
		return allocate( 2, _endian ).putChar( _char ).array() ;
	}

	public static byte[] toBytes( final long _long, final int _endian )
	{
		return allocate( 8, _endian ).putLong( _long ).array() ;
	}

	public static byte[] toBytes( final short _short, final int _endian )
	{
		return allocate( 2, _endian ).putShort( _short ).array() ;
	}

	public static byte[] toBytes( final float _float, final int _endian )
	{
		return allocate( 4, _endian ).putFloat( _float ).array() ;
	}

	public static byte[] toBytes( final double _double, final int _endian )
	{
		return allocate( 8, _endian ).putDouble( _double ).array() ;
	}

	public static byte[] toBytes( final boolean _bool, final int _endian )
	{
		return new byte[] { ( byte )( _bool ? 0x01 : 0x00 ) } ; // bool -> { 1 byte }
	}

	/**VARIABLE ARRAY to BYTE ARRAY**/

	public static byte[] toBytes( final int[] _int, final int _endian )
	{
		final int length = _int.length ;
		final ByteBuffer buffer = allocate( length * 4, _endian ) ;
		for( int i = 0; i < length; ++i )
		{
			buffer.putInt( _int[i] ) ;
		}
		return buffer.array() ;
	}

	public static byte[] toBytes( final char[] _char, final int _endian )
	{
		final int length = _char.length ;
		final ByteBuffer buffer = allocate( length, _endian ) ;
		for( int i = 0; i < length; ++i )
		{
			buffer.putChar( _char[i] ) ;
		}
		return buffer.array() ;
	}

	public static byte[] toBytes( final short[] _short, final int _endian )
	{
		final int length = _short.length ;
		final ByteBuffer buffer = allocate( length, _endian ) ;
		for( int i = 0; i < length; ++i )
		{
			buffer.putShort( _short[i] ) ;
		}
		return buffer.array() ;
	}

	public static byte[] toBytes( final long[] _long, final int _endian )
	{
		final int length = _long.length ;
		final ByteBuffer buffer = allocate( length * 8, _endian ) ;
		for( int i = 0; i < length; ++i )
		{
			buffer.putLong( _long[i] ) ;
		}
		return buffer.array() ;
	}

	public static byte[] toBytes( final float[] _float, final int _endian )
	{
		final int length = _float.length ;
		final ByteBuffer buffer = allocate( length * 4, _endian ) ;
		for( int i = 0; i < length; ++i )
		{
			buffer.putFloat( _float[i] ) ;
		}
		return buffer.array() ;
	}

	public static byte[] toBytes( final double[] _double, final int _endian )
	{
		final int length = _double.length ;
		final ByteBuffer buffer = allocate( length * 8, _endian ) ;
		for( int i = 0; i < length; ++i )
		{
			buffer.putDouble( _double[i] ) ;
		}
		return buffer.array() ;
	}

	/**BYTE ARRAY to VARIABLE**/

	public static int toInt( final byte[] _int, final int _offset, int _length )
	{
		return ByteBuffer.wrap( _int, _offset, _length ).getInt() ;
	}

	public static char toChar( final byte[] _char, final int _offset, int _length )
	{
		return ByteBuffer.wrap( _char, _offset, _length ).getChar() ;
	}

	public static long toLong( final byte[] _long, final int _offset, int _length )
	{
		return ByteBuffer.wrap( _long, _offset, _length ).getLong() ;
	}

	public static short toShort( final byte[] _short, final int _offset, int _length )
	{
		return ByteBuffer.wrap( _short, _offset, _length ).getShort() ;
	}

	public static float toFloat( final byte[] _float, final int _offset, int _length )
	{
		return ByteBuffer.wrap( _float, _offset, _length ).getFloat() ;
	}

	public static double toDouble( final byte[] _double, final int _offset, int _length )
	{
		return ByteBuffer.wrap( _double, _offset, _length ).getDouble() ;
	}

	public static boolean toBoolean( final byte[] _bool, final int _offset, int _length )
	{
		return ( _bool == null || _bool.length == 0 ) ? false : _bool[_offset + _length] != 0x00;
	}

	public static byte[] toBytes( final byte[] _bytes, final int _offset, int _length )
	{
		// Assumes BIG_ENDIAN
		return allocate( _length, BIG_ENDIAN ).put( _bytes, _offset, _length ).array() ;
	}

	public static byte[] toBits( final byte[] _bytes, final int _byteOffset, final int _bitOffset, final int _bitLength )
	{
		final int startOffset = ( _byteOffset * 8 ) + _bitOffset ;
		final int endOffset = startOffset + _bitLength ;
		final int byteLength = ( int )Math.ceil( _bitLength / 8.0f ) ;

		final int start = ( int )Math.floor( startOffset / 8.0f ) ;
		final int end = ( int )Math.ceil( endOffset / 8.0f ) ;
		final int len = end - start ;
		
		final byte[] temp = ConvertBytes.newInvertBytes( _bytes, start, len ) ;
		{
			printByte( _bytes[start] ) ;
			printByte( temp[0] ) ;
		}

		final int subStart = startOffset % 8 ;
		final int subEnd = endOffset % 8 ;
		final int subLength = subStart + _bitLength ;

		final byte[] bytes = new byte[len] ;

		int j = 0 ;//( temp.length * 8 ) - 1 ;
		for( int i = subLength - 1; i >= subStart ; --i )
		{
			setBit( bytes, j++, isBitSet( temp, i ) ) ;
		}

		return bytes ;
	}

	public static byte[] newInvertBytes( final byte[] _bytes, int _offset, final  int _length )
	{
		final byte[] bytes = new byte[_length] ;
		final int size = _offset + _length ;
		
		//System.out.println( "Start: " + _offset + "Length: " + _length + " Size: " + size ) ;
		
		int bytesPos = 0 ;
		for( int i = _offset; i < size; ++i )
		{
			//System.out.print( "Byte: " ) ; printByte( _bytes[i] ) ;
			for( int bitPos = 0; bitPos < 8; ++bitPos )
			{
				setBit( bytes, bytesPos + ( 7 - bitPos ), isBitSet( _bytes[i], bitPos ) ) ;
			}
			bytesPos += 8 ;
		}
		
		return bytes ;
	}
	
	/**FLIP BYTE ARRAY ENDIAN**/

	public static byte[] newflipEndian( final byte[] _bytes )
	{
		final int length = _bytes.length ;
		final byte[] bytes = new byte[length] ;
		
		for( int i = 0; i < length; ++i )
		{
			bytes[length - i - 1] = _bytes[i] ;
		}

		return bytes ;
	}

	/**
		Modifies passed in byte-stream, returns the same stream.
	*/
	public static byte[] flipEndian( final byte[] _bytes )
	{
		final int length = _bytes.length ;
		final int halfLength = length / 2 ;
		for( int i = 0; i < halfLength; ++i )
		{
			final byte h = _bytes[length - i - 1] ;
			_bytes[length - i - 1] = _bytes[i] ;
			_bytes[i] = h ;
		}

		return _bytes ;
	}

	/**
		Allows you to flip the endian of a defined part of the byte-stream.
	*/
	public static byte[] newflipEndian( final byte[] _bytes, final int _offset, final int _length )
	{
		final byte[] bytes = new byte[_length] ;
		for( int i = 0; i < _length; ++i )
		{
			bytes[_length - i - 1] = _bytes[_offset + i] ;
		}

		return bytes ;
	}

	/**
		Modifies passed in byte-stream, returns the same stream.
		Allows you to flip the endian of a defined part of the byte-stream.
	*/
	public static byte[] flipEndian( final byte[] _bytes, final int _offset, final int _length )
	{
		final int halfLength = _length / 2 ;
		for( int i = 0; i < halfLength; ++i )
		{
			final byte h = _bytes[_offset + _length - i - 1] ;
			_bytes[_offset + _length - i - 1] = _bytes[_offset + i] ;
			_bytes[_offset + i] = h ;
		}
		
		return _bytes ;
	}

	/**
		0 - 7 positions are acceptable.
	*/
	public static boolean isBitSet( final byte _byte, final int _position )
	{
		return ( ( _byte >> _position ) & 1 ) == 1 ;
	}

	public static boolean isBitSet( final byte[] _bytes, final int _position )
	{
		final int index = ( int )Math.floor( _position / 8.0f ) ;
		final int bitPos = ( _position % 8 ) ;

		/*System.out.println( _position ) ;
		System.out.println( "Index: " + index + " BitPos: " + bitPos ) ;
		System.out.print( "Byte: " ) ; printByte( _bytes[index] ) ;*/

		return ( _bytes[index] >> bitPos & 1 ) == 1 ;
	}

	public static byte setBit( byte _byte, final int _position, final boolean _set )
	{
		return _byte |= ( ( _set == true ) ? 1 : 0 ) << _position ;
	}

	public static void setBit( byte[] _bytes, final int _position, final boolean _set )
	{
		final int index = _position / 8 ;
		final int bitPos = _position % 8 ;
		_bytes[index] |= ( ( _set == true ) ? 1 : 0 ) << bitPos ;
	}

	public static void printByte( final byte _byte )
	{
		for( int i = 0; i < 8; ++i )
		{
			System.out.print( ( isBitSet( _byte, i ) == true ) ? "1" : "0"  ) ;
		}
		System.out.print( "\n" ) ;
	}
	
	private static ByteBuffer allocate( final int _capacity, final int _endian )
	{
		return ByteBuffer.allocate( _capacity ).order( getByteOrder( _endian ) ) ;
	}

	private static ByteOrder getByteOrder( final int _endian )
	{
		return ( _endian == BIG_ENDIAN ) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN ;
	}
	
	/**OLD CONVERSION FUNCTIONS - SHOULD BE DEPRECATED**/

	/**
		Requires at length 4 bytes
	*/
	public static int toInteger( final byte[] _bytes )
	{
		return _bytes[0] << 24 | 
			 ( _bytes[1] & 0xFF ) << 16 |
			 ( _bytes[2] & 0xFF ) << 8 |
			 ( _bytes[3] & 0xFF ) ;
	}

	/**
		Requires at length 2 bytes
	*/
	public static short toShort( final byte[] _bytes )
	{
		return ( short )( ( _bytes[0] << 8 ) | ( _bytes[1] ) ) ;
	}

	public static short[] toShorts( final byte[] _bytes )
	{
		final short[] shorts = new short[_bytes.length / 2] ;
		int j = 0 ;

		for( int i = 0; i < _bytes.length; i += 2 )
		{
			shorts[j++] = ( short )( ( _bytes[i] << 8 ) | ( _bytes[i + 1] ) ) ;
		}

		return shorts ;
	}
}