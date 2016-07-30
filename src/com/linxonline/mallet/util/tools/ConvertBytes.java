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

	private static ConvertBytes.Interface impl = new DefaultConvertBytes() ;

	private ConvertBytes() {}

	public static void setImplementation( ConvertBytes.Interface _impl )
	{
		impl = _impl ;
	}

	/**
		Returns the platforms native Endianese
	*/
	public static int nativeOrder()
	{
		return impl.nativeOrder() ;
	}

	/**VARIABLE to BYTE ARRAY**/

	public static byte[] toBytes( final int _int, final int _endian )
	{
		return impl.toBytes( _int, _endian ) ;
	}

	public static byte[] toBytes( final byte _byte, final int _endian )
	{
		return impl.toBytes( _byte, _endian ) ;
	}

	public static byte[] toBytes( final char _char, final int _endian )
	{
		return impl.toBytes( _char, _endian ) ;
	}

	public static byte[] toBytes( final long _long, final int _endian )
	{
		return impl.toBytes( _long, _endian ) ;
	}

	public static byte[] toBytes( final short _short, final int _endian )
	{
		return impl.toBytes( _short, _endian ) ;
	}

	public static byte[] toBytes( final float _float, final int _endian )
	{
		return impl.toBytes( _float, _endian ) ;
	}

	public static byte[] toBytes( final double _double, final int _endian )
	{
		return impl.toBytes( _double, _endian ) ;
	}

	public static byte[] toBytes( final boolean _bool, final int _endian )
	{
		return impl.toBytes( _bool, _endian ) ;
	}

	/**VARIABLE ARRAY to BYTE ARRAY**/

	public static byte[] toBytes( final int[] _int, final int _endian )
	{
		return impl.toBytes( _int, _endian ) ;
	}

	public static byte[] toBytes( final char[] _char, final int _endian )
	{
		return impl.toBytes( _char, _endian ) ;
	}

	public static byte[] toBytes( final short[] _short, final int _endian )
	{
		return impl.toBytes( _short, _endian ) ;
	}

	public static byte[] toBytes( final long[] _long, final int _endian )
	{
		return impl.toBytes( _long, _endian ) ;
	}

	public static byte[] toBytes( final float[] _float, final int _endian )
	{
		return impl.toBytes( _float, _endian ) ;
	}

	public static byte[] toBytes( final double[] _double, final int _endian )
	{
		return impl.toBytes( _double, _endian ) ;
	}

	/**BYTE ARRAY to VARIABLE**/

	public static int toInt( final byte[] _int, int _offset, int _length )
	{
		return impl.toInt( _int, _offset, _length ) ;
	}

	public static char toChar( final byte[] _char, final int _offset, int _length )
	{
		return impl.toChar( _char, _offset, _length ) ;
	}

	public static long toLong( final byte[] _long, final int _offset, int _length )
	{
		return impl.toLong( _long, _offset, _length ) ;
	}

	public static short toShort( final byte[] _short, final int _offset, int _length )
	{
		return impl.toShort( _short, _offset, _length ) ;
	}

	public static float toFloat( final byte[] _float, final int _offset, int _length )
	{
		return impl.toFloat( _float, _offset, _length ) ;
	}

	public static double toDouble( final byte[] _double, final int _offset, int _length )
	{
		return impl.toDouble( _double, _offset, _length ) ;
	}

	public static boolean toBoolean( final byte[] _bool, final int _offset, int _length )
	{
		return impl.toBoolean( _bool, _offset, _length ) ;
	}

	public static byte[] toBytes( final byte[] _bytes, final int _offset, int _length )
	{
		// Assumes BIG_ENDIAN
		return impl.toBytes( _bytes, _offset, _length ) ;
	}

	public static byte[] toBits( final byte[] _bytes, final int _byteOffset, final int _bitOffset, final int _bitLength )
	{
		return impl.toBits( _bytes, _byteOffset, _bitOffset, _bitLength ) ;
	}

	public static byte[] newBytes( final byte[] _bytes, int _offset, final  int _length )
	{
		return impl.newBytes( _bytes, _offset, _length ) ;
	}

	public static byte[] newInvertBytes( final byte[] _bytes, int _offset, final  int _length )
	{
		return impl.newInvertBytes( _bytes, _offset, _length ) ;
	}

	/**FLIP BYTE ARRAY ENDIAN**/

	public static byte[] newflipEndian( final byte[] _bytes )
	{
		return impl.newflipEndian( _bytes ) ;
	}

	/**
		Modifies passed in byte-stream, returns the same stream.
	*/
	public static byte[] flipEndian( final byte[] _bytes )
	{
		return impl.flipEndian( _bytes ) ;
	}

	/**
		Allows you to flip the endian of a defined part of the byte-stream.
	*/
	public static byte[] newflipEndian( final byte[] _bytes, final int _offset, final int _length )
	{
		return impl.newflipEndian( _bytes, _offset, _length ) ;
	}

	/**
		Modifies passed in byte-stream, returns the same stream.
		Allows you to flip the endian of a defined part of the byte-stream.
	*/
	public static byte[] flipEndian( final byte[] _bytes, final int _offset, final int _length )
	{
		return impl.flipEndian( _bytes, _offset, _length ) ;
	}

	/**
		0 - 7 positions are acceptable.
	*/
	public static boolean isBitSet( final byte _byte, final int _position )
	{
		return impl.isBitSet( _byte, _position ) ;
	}

	public static boolean isBitSet( final byte[] _bytes, final int _position )
	{
		return impl.isBitSet( _bytes, _position ) ;
	}

	public static byte setBit( byte _byte, final int _position, final boolean _set )
	{
		return impl.setBit( _byte, _position, _set ) ;
	}

	public static void setBit( byte[] _bytes, final int _position, final boolean _set )
	{
		impl.setBit( _bytes, _position, _set ) ;
	}

	public static void printByte( final byte _byte )
	{
		impl.printByte( _byte ) ;
	}

	/**
		Combine the two byte-arrays passed in.
		Creates a new array of a.length + b.length long.
		Copies the arrays sequentially into the new byte-array.
	*/
	public static byte[] concat( final byte[] _a, final byte[] _b )
	{
		return impl.concat( _a, _b ) ;
	}

	/**
		Insert _a into _b.
	*/
	public static byte[] insert( final byte[] _a, final int _aOffset, final int _aLength, final byte[] _b, final int _bOffset )
	{
		return impl.insert( _a, _aOffset, _aLength, _b, _bOffset ) ;
	}

	/**
		Basic implementation that should work on all platforms.
		Web and iOS make extend this class to chnage certain 
		aspects. JavaScript has a particular issue with bytes to floats 
		and floats to bytes, it doesn't retain the state correctly.
	*/
	public static class DefaultConvertBytes implements ConvertBytes.Interface
	{
		public DefaultConvertBytes() {}

		/**
			Returns the platforms native Endianese
		*/
		public int nativeOrder()
		{
			// If BIG_ENDIAN return BIG_ENDIAN else return LITTLE_ENDIAN
			return ( ByteOrder.BIG_ENDIAN == ByteOrder.nativeOrder() ) ? ConvertBytes.BIG_ENDIAN : ConvertBytes.LITTLE_ENDIAN ;
		}

		/**VARIABLE to BYTE ARRAY**/

		public byte[] toBytes( final int _int, final int _endian )
		{
			// Big Endian
			return new byte[]
			{ 
				( byte )( ( _int >> 24 ) & 0xFF ),
				( byte )( ( _int >> 16 ) & 0xFF ),
				( byte )( ( _int >> 8 ) & 0xFF ),
				( byte )( ( _int >> 0 ) & 0xFF )
			} ;
			//return allocate( 4, _endian ).putInt( _int ).array() ;
		}

		public byte[] toBytes( final byte _byte, final int _endian )
		{
			final byte[] bytes = new byte[1] ;
			bytes[0] = _byte ;
			return bytes ;
			//return allocate( 1, _endian ).put( _byte ).array() ;
		}

		public byte[] toBytes( final char _char, final int _endian )
		{
			return new byte[]
			{
				( byte )( _char >> 8 ),
				( byte )_char
			} ;
			//return allocate( 2, _endian ).putChar( _char ).array() ;
		}

		public byte[] toBytes( final long _long, final int _endian )
		{
			return new byte[]
			{
				( byte )( ( _long >> 56 ) & 0xFF ),
				( byte )( ( _long >> 48 ) & 0xFF ),
				( byte )( ( _long >> 40 ) & 0xFF ),
				( byte )( ( _long >> 32 ) & 0xFF ),
				( byte )( ( _long >> 24 ) & 0xFF ),
				( byte )( ( _long >> 16 ) & 0xFF ),
				( byte )( ( _long >> 8 ) & 0xFF ),
				( byte )( ( _long >> 0 ) & 0xFF )
			} ;
			//return allocate( 8, _endian ).putLong( _long ).array() ;
		}

		public byte[] toBytes( final short _short, final int _endian )
		{
			return new byte[]
			{
				( byte )( ( _short >> 8 ) & 0xFF ),
				( byte )( ( _short >> 0 ) & 0xFF )
			} ;
			//return allocate( 2, _endian ).putShort( _short ).array() ;
		}

		public byte[] toBytes( final float _float, final int _endian )
		{
			return toBytes( Float.floatToRawIntBits( _float ), _endian ) ;
			//return allocate( 4, _endian ).putFloat( _float ).array() ;
		}

		public byte[] toBytes( final double _double, final int _endian )
		{
			return toBytes( Double.doubleToRawLongBits( _double ), _endian ) ;
			//return allocate( 8, _endian ).putDouble( _double ).array() ;
		}

		public byte[] toBytes( final boolean _bool, final int _endian )
		{
			return new byte[] { ( byte )( _bool ? 0x01 : 0x00 ) } ; // bool -> { 1 byte }
		}

		/**VARIABLE ARRAY to BYTE ARRAY**/

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

		public int toInt( final byte[] _int, int _offset, int _length )
		{
			return ( ( _int[_offset + 0] & 0xFF ) << 24 ) |
				( ( _int[_offset + 1] & 0xFF ) << 16 ) |
				( ( _int[_offset + 2] & 0xFF ) << 8 ) |
				( ( _int[_offset + 3] & 0xFF ) << 0 ) ;

			//return ByteBuffer.wrap( _int, _offset, _length ).getInt() ;
		}

		public char toChar( final byte[] _char, final int _offset, int _length )
		{
			return ( char )toInt( _char, _offset, _length ) ;
			//return ByteBuffer.wrap( _char, _offset, _length ).getChar() ;
		}

		public long toLong( final byte[] _long, final int _offset, int _length )
		{
			return ( ( _long[_offset + 0] & 0xFF ) << 56 ) |
				( ( _long[_offset + 1] & 0xFF ) << 48 ) |
				( ( _long[_offset + 2] & 0xFF ) << 40 ) |
				( ( _long[_offset + 3] & 0xFF ) << 32 ) |
				( ( _long[_offset + 4] & 0xFF ) << 24 ) |
				( ( _long[_offset + 5] & 0xFF ) << 16 ) |
				( ( _long[_offset + 6] & 0xFF ) << 8 ) |
				( ( _long[_offset + 7] & 0xFF ) << 0 ) ;
			//return ByteBuffer.wrap( _long, _offset, _length ).getLong() ;
		}

		public short toShort( final byte[] _short, final int _offset, int _length )
		{
			//return ( short )toInt( _short, _offset, _length ) ;
			return ByteBuffer.wrap( _short, _offset, _length ).getShort() ;
		}

		public float toFloat( final byte[] _float, final int _offset, int _length )
		{
			final int toInt = toInt( _float, _offset, _length ) ;
			return Float.intBitsToFloat( toInt ) ;
			//return ByteBuffer.wrap( _float, _offset, _length ).getFloat() ;
		}

		public double toDouble( final byte[] _double, final int _offset, int _length )
		{
			return Double.longBitsToDouble( toLong( _double, _offset, _length ) ) ;
			//return ByteBuffer.wrap( _double, _offset, _length ).getDouble() ;
		}

		public boolean toBoolean( final byte[] _bool, final int _offset, int _length )
		{
			return ( _bool == null || _bool.length == 0 ) ? false : _bool[_offset + _length] != 0x00;
		}

		public byte[] toBytes( final byte[] _bytes, final int _offset, int _length )
		{
			// Assumes BIG_ENDIAN
			return allocate( _length, ConvertBytes.BIG_ENDIAN ).put( _bytes, _offset, _length ).array() ;
		}

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

		public byte[] newBytes( final byte[] _bytes, int _offset, final  int _length )
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

		public byte[] newInvertBytes( final byte[] _bytes, int _offset, final  int _length )
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
		public boolean isBitSet( final byte _byte, final int _position )
		{
			return ( ( _byte >> _position ) & 1 ) == 1 ;
		}

		public boolean isBitSet( final byte[] _bytes, final int _position )
		{
			final int index = ( int )Math.floor( _position / 8.0f ) ;
			final int bitPos = ( _position % 8 ) ;

			return ( _bytes[index] >> bitPos & 1 ) == 1 ;
		}

		public byte setBit( byte _byte, final int _position, final boolean _set )
		{
			return _byte |= ( ( _set == true ) ? 1 : 0 ) << _position ;
		}

		public void setBit( byte[] _bytes, final int _position, final boolean _set )
		{
			final int index = _position / 8 ;
			final int bitPos = _position % 8 ;
			_bytes[index] |= ( ( _set == true ) ? 1 : 0 ) << bitPos ;
		}

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

	public static interface Interface
	{
		public int nativeOrder() ;

		/**VARIABLE to BYTE ARRAY**/

		public byte[] toBytes( final int _int, final int _endian ) ;
		public byte[] toBytes( final byte _byte, final int _endian ) ;
		public byte[] toBytes( final char _char, final int _endian ) ;
		public byte[] toBytes( final long _long, final int _endian ) ;
		public byte[] toBytes( final short _short, final int _endian ) ;
		public byte[] toBytes( final float _float, final int _endian ) ;
		public byte[] toBytes( final double _double, final int _endian ) ;
		public byte[] toBytes( final boolean _bool, final int _endian ) ;

		/**VARIABLE ARRAY to BYTE ARRAY**/

		public byte[] toBytes( final int[] _int, final int _endian ) ;
		public byte[] toBytes( final char[] _char, final int _endian ) ;
		public byte[] toBytes( final short[] _short, final int _endian ) ;
		public byte[] toBytes( final long[] _long, final int _endian ) ;
		public byte[] toBytes( final float[] _float, final int _endian ) ;
		public byte[] toBytes( final double[] _double, final int _endian ) ;

		/**BYTE ARRAY to VARIABLE**/

		public int toInt( final byte[] _int, int _offset, int _length ) ;
		public char toChar( final byte[] _char, final int _offset, int _length ) ;
		public long toLong( final byte[] _long, final int _offset, int _length ) ;
		public short toShort( final byte[] _short, final int _offset, int _length ) ;
		public float toFloat( final byte[] _float, final int _offset, int _length ) ;
		public double toDouble( final byte[] _double, final int _offset, int _length ) ;
		public boolean toBoolean( final byte[] _bool, final int _offset, int _length ) ;

		public byte[] toBytes( final byte[] _bytes, final int _offset, int _length ) ;
		public byte[] toBits( final byte[] _bytes, final int _byteOffset, final int _bitOffset, final int _bitLength ) ;

		public byte[] newBytes( final byte[] _bytes, int _offset, final  int _length ) ;
		public byte[] newInvertBytes( final byte[] _bytes, int _offset, final  int _length ) ;

		/**FLIP BYTE ARRAY ENDIAN**/

		public byte[] newflipEndian( final byte[] _bytes ) ;
		public byte[] flipEndian( final byte[] _bytes ) ;

		public byte[] newflipEndian( final byte[] _bytes, final int _offset, final int _length ) ;
		public byte[] flipEndian( final byte[] _bytes, final int _offset, final int _length ) ;

		public boolean isBitSet( final byte _byte, final int _position ) ;
		public boolean isBitSet( final byte[] _bytes, final int _position ) ;

		public byte setBit( byte _byte, final int _position, final boolean _set ) ;

		public void setBit( byte[] _bytes, final int _position, final boolean _set ) ;
		public void printByte( final byte _byte ) ;

		public byte[] concat( final byte[] _a, final byte[] _b ) ;
		public byte[] insert( final byte[] _a, final int _aOffset, final int _aLength, final byte[] _b, final int _bOffset ) ;
	}
}