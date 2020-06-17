package com.linxonline.mallet.util.tools ;

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

	private static DefaultConvertBytes impl = new DefaultConvertBytes() ;

	private ConvertBytes() {}

	/**
		Returns the platforms native Endianese
	*/
	public static int nativeOrder()
	{
		return impl.nativeOrder() ;
	}

	/**VARIABLE to BYTE ARRAY**/

	public static byte[] toBytes( final int _int, final int _endian, final byte[] _fill )
	{
		return impl.toBytes( _int, _endian, _fill ) ;
	}

	public byte[] toBytes( final byte _byte, final int _endian, final byte[] _fill )
	{
		return impl.toBytes( _byte, _endian, _fill ) ;
	}

	public byte[] toBytes( final char _char, final int _endian, final byte[] _fill )
	{
		return impl.toBytes( _char, _endian, _fill ) ;
	}

	public byte[] toBytes( final long _long, final int _endian, final byte[] _fill )
	{
		return impl.toBytes( _long, _endian, _fill ) ;
	}

	public byte[] toBytes( final short _short, final int _endian, final byte[] _fill )
	{
		return impl.toBytes( _short, _endian, _fill ) ;
	}

	public static byte[] toBytes( final float _float, final int _endian, final byte[] _fill )
	{
		return impl.toBytes( _float, _endian, _fill ) ;
	}

	public byte[] toBytes( final double _double, final int _endian, final byte[] _fill )
	{
		return impl.toBytes( _double, _endian, _fill ) ;
	}

	public byte[] toBytes( final boolean _bool, final int _endian, final byte[] _fill )
	{
		return impl.toBytes( _bool, _endian, _fill ) ;
	}

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

	public static int toInt( final byte[] _int, final int _offset )
	{
		return impl.toInt( _int, _offset ) ;
	}

	public static char toChar( final byte[] _char, final int _offset )
	{
		return impl.toChar( _char, _offset ) ;
	}

	public static long toLong( final byte[] _long, final int _offset )
	{
		return impl.toLong( _long, _offset ) ;
	}

	public static short toShort( final byte[] _short, final int _offset )
	{
		return impl.toShort( _short, _offset ) ;
	}

	public static float toFloat( final byte[] _float, final int _offset )
	{
		return impl.toFloat( _float, _offset ) ;
	}

	public static double toDouble( final byte[] _double, final int _offset )
	{
		return impl.toDouble( _double, _offset ) ;
	}

	public static boolean toBoolean( final byte[] _bool, final int _offset )
	{
		return impl.toBoolean( _bool, _offset ) ;
	}

	public static byte[] toBytes( final byte[] _bytes, final int _offset, final int _length )
	{
		// Assumes BIG_ENDIAN
		return impl.toBytes( _bytes, _offset, _length ) ;
	}

	public static byte[] toBits( final byte[] _bytes, final int _byteOffset, final int _bitOffset, final int _bitLength )
	{
		return impl.toBits( _bytes, _byteOffset, _bitOffset, _bitLength ) ;
	}

	public static byte[] newBytes( final byte[] _bytes, final int _offset, final int _length )
	{
		return impl.newBytes( _bytes, _offset, _length ) ;
	}

	public static byte[] newInvertBytes( final byte[] _bytes, final int _offset, final int _length )
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

	public static byte setBit( final byte _byte, final int _position, final boolean _set )
	{
		return impl.setBit( _byte, _position, _set ) ;
	}

	public static void setBit( final byte[] _bytes, final int _position, final boolean _set )
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
}
