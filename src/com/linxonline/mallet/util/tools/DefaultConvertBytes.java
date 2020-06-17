package com.linxonline.mallet.util.tools ;

import java.nio.ByteBuffer ;
import java.nio.ByteOrder ;

/**
	Basic implementation that should work on all platforms.
	Web and iOS make extend this class to change certain 
	aspects. JavaScript has a particular issue with bytes to floats 
	and floats to bytes, it doesn't retain the state correctly.
*/
public class DefaultConvertBytes implements IConvertBytes
{
	public DefaultConvertBytes() {}

	/**
		Returns the platforms native Endianese
	*/
	@Override
	public int nativeOrder()
	{
		// If BIG_ENDIAN return BIG_ENDIAN else return LITTLE_ENDIAN
		return ( ByteOrder.BIG_ENDIAN == ByteOrder.nativeOrder() ) ? ConvertBytes.BIG_ENDIAN : ConvertBytes.LITTLE_ENDIAN ;
	}

	/**VARIABLE to BYTE ARRAY**/

	@Override
	public byte[] toBytes( final int _int, final int _endian, final byte[] _fill )
	{
		// Big Endian
		_fill[0] = ( byte )( ( _int >> 24 ) & 0xFF ) ;
		_fill[1] = ( byte )( ( _int >> 16 ) & 0xFF ) ;
		_fill[2] = ( byte )( ( _int >> 8 ) & 0xFF ) ;
		_fill[3] = ( byte )( ( _int >> 0 ) & 0xFF ) ;

		return _fill ;
	}

	@Override
	public byte[] toBytes( final byte _byte, final int _endian, final byte[] _fill )
	{
		_fill[0] = _byte ;
		return _fill ;
	}

	@Override
	public byte[] toBytes( final char _char, final int _endian, final byte[] _fill )
	{
		_fill[0] = ( byte )( _char >> 8 ) ;
		_fill[1] = ( byte )_char ;
		return _fill ;
	}

	@Override
	public byte[] toBytes( final long _long, final int _endian, final byte[] _fill )
	{
		_fill[0] = ( byte )( ( _long >> 56 ) & 0xFF ) ;
		_fill[1] = ( byte )( ( _long >> 48 ) & 0xFF ) ;
		_fill[2] = ( byte )( ( _long >> 40 ) & 0xFF ) ;
		_fill[3] = ( byte )( ( _long >> 32 ) & 0xFF ) ;
		_fill[4] = ( byte )( ( _long >> 24 ) & 0xFF ) ;
		_fill[5] = ( byte )( ( _long >> 16 ) & 0xFF ) ;
		_fill[6] = ( byte )( ( _long >> 8 ) & 0xFF ) ;
		_fill[7] = ( byte )( ( _long >> 0 ) & 0xFF ) ;
		return _fill ;
	}

	@Override
	public byte[] toBytes( final short _short, final int _endian, final byte[] _fill )
	{
		_fill[0] = ( byte )( ( _short >> 8 ) & 0xFF ) ;
		_fill[1] = ( byte )( ( _short >> 0 ) & 0xFF ) ;
		return _fill ;
	}

	@Override
	public byte[] toBytes( final float _float, final int _endian, final byte[] _fill )
	{
		final int temp = Float.floatToRawIntBits( _float ) ; 
		return toBytes( temp, _endian, _fill ) ;
	}

	@Override
	public byte[] toBytes( final double _double, final int _endian, final byte[] _fill )
	{
		final long temp = Double.doubleToRawLongBits( _double ) ;
		toBytes( temp, _endian, _fill ) ;
		return _fill ;
	}

	@Override
	public byte[] toBytes( final boolean _bool, final int _endian, final byte[] _fill )
	{
		_fill[0] = ( byte )( _bool ? 0x01 : 0x00 ) ;
		return _fill ;
	}

	@Override
	public byte[] toBytes( final int _int, final int _endian )
	{
		return toBytes( _int, _endian, new byte[4] ) ;
	}

	@Override
	public byte[] toBytes( final byte _byte, final int _endian )
	{
		return toBytes( _byte, _endian, new byte[1] ) ;
	}

	@Override
	public byte[] toBytes( final char _char, final int _endian )
	{
		return toBytes( _char, _endian, new byte[2] ) ;
	}

	@Override
	public byte[] toBytes( final long _long, final int _endian )
	{
		return toBytes( _long, _endian, new byte[8] ) ;
	}

	@Override
	public byte[] toBytes( final short _short, final int _endian )
	{
		return toBytes( _short, _endian, new byte[2] ) ;
	}

	@Override
	public byte[] toBytes( final float _float, final int _endian )
	{
		return toBytes( _float, _endian, new byte[4] ) ;
	}

	@Override
	public byte[] toBytes( final double _double, final int _endian )
	{
		return toBytes( Double.doubleToRawLongBits( _double ), _endian ) ;
	}

	@Override
	public byte[] toBytes( final boolean _bool, final int _endian )
	{
		return toBytes( _bool, _endian, new byte[1] ) ;
	}

	/**VARIABLE ARRAY to BYTE ARRAY**/

	@Override
	public byte[] toBytes( final int[] _int, final int _endian )
	{
		final int length = _int.length ;
		final ByteBuffer buffer = allocate( length * 4, _endian ) ;
		for( int i = 0; i < length; ++i )
		{
			buffer.putInt( _int[i] ) ;
		}
		return buffer.array() ;
	}

	@Override
	public byte[] toBytes( final char[] _char, final int _endian )
	{
		final int length = _char.length ;
		final ByteBuffer buffer = allocate( length, _endian ) ;
		for( int i = 0; i < length; ++i )
		{
			buffer.putChar( _char[i] ) ;
		}
		return buffer.array() ;
	}

	@Override
	public byte[] toBytes( final short[] _short, final int _endian )
	{
		final int length = _short.length ;
		final ByteBuffer buffer = allocate( length, _endian ) ;
		for( int i = 0; i < length; ++i )
		{
			buffer.putShort( _short[i] ) ;
		}
		return buffer.array() ;
	}

	@Override
	public byte[] toBytes( final long[] _long, final int _endian )
	{
		final int length = _long.length ;
		final ByteBuffer buffer = allocate( length * 8, _endian ) ;
		for( int i = 0; i < length; ++i )
		{
			buffer.putLong( _long[i] ) ;
		}
		return buffer.array() ;
	}

	@Override
	public byte[] toBytes( final float[] _float, final int _endian )
	{
		final int length = _float.length ;
		final ByteBuffer buffer = allocate( length * 4, _endian ) ;
		for( int i = 0; i < length; ++i )
		{
			buffer.putFloat( _float[i] ) ;
		}
		return buffer.array() ;
	}

	@Override
	public byte[] toBytes( final double[] _double, final int _endian )
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

	@Override
	public int toInt( final byte[] _int, final int _offset )
	{
		return ( ( _int[_offset + 0] & 0xFF ) << 24 ) |
				( ( _int[_offset + 1] & 0xFF ) << 16 ) |
				( ( _int[_offset + 2] & 0xFF ) << 8 ) |
				( ( _int[_offset + 3] & 0xFF ) << 0 ) ;
	}

	@Override
	public char toChar( final byte[] _char, final int _offset )
	{
		return ( char )toInt( _char, _offset ) ;
	}

	@Override
	public long toLong( final byte[] _long, final int _offset )
	{
		return ( ( long )( _long[_offset + 0] & 0xFF ) << 56 ) |
				( ( long )( _long[_offset + 1] & 0xFF ) << 48 ) |
				( ( long )( _long[_offset + 2] & 0xFF ) << 40 ) |
				( ( long )( _long[_offset + 3] & 0xFF ) << 32 ) |
				( ( long )( _long[_offset + 4] & 0xFF ) << 24 ) |
				( ( long )( _long[_offset + 5] & 0xFF ) << 16 ) |
				( ( long )( _long[_offset + 6] & 0xFF ) << 8 ) |
				( ( long )( _long[_offset + 7] & 0xFF ) << 0 ) ;
	}

	@Override
	public short toShort( final byte[] _short, final int _offset )
	{
		//return ( short )toInt( _short, _offset, _length ) ;
		return ByteBuffer.wrap( _short, _offset, 2 ).getShort() ;
	}

	@Override
	public float toFloat( final byte[] _float, final int _offset )
	{
		final int toInt = toInt( _float, _offset ) ;
		return Float.intBitsToFloat( toInt ) ;
	}

	@Override
	public double toDouble( final byte[] _double, final int _offset )
	{
		final long toLong = toLong( _double, _offset ) ;
		return Double.longBitsToDouble( toLong ) ;
	}

	@Override
	public boolean toBoolean( final byte[] _bool, final int _offset )
	{
		return ( _bool == null || _bool.length == 0 ) ? false : _bool[_offset] != 0x00;
	}

	@Override
	public byte[] toBytes( final byte[] _bytes, final int _offset, final int _length )
	{
		// Assumes BIG_ENDIAN
		return allocate( _length, ConvertBytes.BIG_ENDIAN ).put( _bytes, _offset, _length ).array() ;
	}

	@Override
	public byte[] toBits( final byte[] _bytes, final int _byteOffset, final int _bitOffset, final int _bitLength )
	{
		final int startOffset = ( _byteOffset * 8 ) + _bitOffset ;
		final int endOffset = startOffset + _bitLength ;

		final int start = ( int )Math.floor( startOffset / 8.0f ) ;
		final int end = ( int )Math.ceil( endOffset / 8.0f ) ;
		final int len = ( _bitLength / 8 ) + ( ( ( _bitLength % 8 > 0 ) ) ? 1 : 0 ) ;

		int j = 0 ;
		final byte[] bytes = new byte[len] ;
		for( int i = startOffset; i < endOffset; ++i )
		{
			setBit( bytes, j++, isBitSet( _bytes, i ) ) ;
		}

		return bytes ;
	}

	@Override
	public byte[] newBytes( final byte[] _bytes, final int _offset, final int _length )
	{
		final byte[] bytes = new byte[_length] ;
		final int size = _offset + _length ;

		//System.out.println( "Start: " + _offset + "Length: " + _length + " Size: " + size ) ;

		int bytesPos = 0 ;
		for( int i = _offset; i < size; ++i )
		{
			bytes[bytesPos++] = _bytes[i] ;
		}

		return bytes ;
	}

	@Override
	public byte[] newInvertBytes( final byte[] _bytes, final int _offset, final int _length )
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

	@Override
	public byte[] newflipEndian( final byte[] _bytes )
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
	@Override
	public byte[] flipEndian( final byte[] _bytes )
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
	@Override
	public byte[] newflipEndian( final byte[] _bytes, final int _offset, final int _length )
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
	@Override
	public byte[] flipEndian( final byte[] _bytes, final int _offset, final int _length )
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
	@Override
	public boolean isBitSet( final byte _byte, final int _position )
	{
		return ( ( _byte >> _position ) & 1 ) == 1 ;
	}

	@Override
	public boolean isBitSet( final byte[] _bytes, final int _position )
	{
		final int index = ( int )Math.floor( _position / 8.0f ) ;
		final int bitPos = ( _position % 8 ) ;

		return ( _bytes[index] >> bitPos & 1 ) == 1 ;
	}

	@Override
	public byte setBit( byte _byte, final int _position, final boolean _set )
	{
		return _byte |= ( ( _set == true ) ? 1 : 0 ) << _position ;
	}

	@Override
	public void setBit( final byte[] _bytes, final int _position, final boolean _set )
	{
		final int index = _position / 8 ;
		final int bitPos = _position % 8 ;
		_bytes[index] |= ( ( _set == true ) ? 1 : 0 ) << bitPos ;
	}

	@Override
	public void printByte( final byte _byte )
	{
		for( int i = 0; i < 8; ++i )
		{
			System.out.print( ( isBitSet( _byte, i ) == true ) ? "1" : "0"  ) ;
		}
		System.out.print( "\n" ) ;
	}

	/**
		Combine the two byte-arrays passed in.
		Creates a new array of a.length + b.length long.
		Copies the arrays sequentially into the new byte-array.
	*/
	@Override
	public byte[] concat( final byte[] _a, final byte[] _b )
	{
		final byte[] c = new byte[_a.length + _b.length] ;
		System.arraycopy( _a, 0, c, 0, _a.length ) ;
		System.arraycopy( _b, 0, c, _a.length, _b.length ) ;
		return c ;
	}

	/**
		Insert _a into _b.
	*/
	@Override
	public byte[] insert( final byte[] _a, final int _aOffset, final int _aLength, final byte[] _b, final int _bOffset )
	{
		for( int i = 0; i < _aLength; i++ )
		{
			_b[_bOffset + i] = _a[_aOffset + i] ;
		}

		return _b ;
	}

	private static ByteBuffer allocate( final int _capacity, final int _endian )
	{
		return ByteBuffer.allocate( _capacity ).order( getByteOrder( _endian ) ) ;
	}

	private static ByteOrder getByteOrder( final int _endian )
	{
		return ( _endian == ConvertBytes.BIG_ENDIAN ) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN ;
	}
}
