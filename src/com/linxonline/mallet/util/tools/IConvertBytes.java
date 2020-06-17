package com.linxonline.mallet.util.tools ;

public interface IConvertBytes
{
	public int nativeOrder() ;

	/**VARIABLE to BYTE ARRAY**/

	public byte[] toBytes( final int _int, final int _endian, final byte[] _fill ) ;
	public byte[] toBytes( final byte _byte, final int _endian, final byte[] _fill ) ;
	public byte[] toBytes( final char _char, final int _endian, final byte[] _fill ) ;
	public byte[] toBytes( final long _long, final int _endian, final byte[] _fill ) ;
	public byte[] toBytes( final short _short, final int _endian, final byte[] _fill ) ;
	public byte[] toBytes( final float _float, final int _endian, final byte[] _fill ) ;
	public byte[] toBytes( final double _double, final int _endian, final byte[] _fill ) ;
	public byte[] toBytes( final boolean _bool, final int _endian, final byte[] _fill ) ;

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

	public int toInt( final byte[] _int, int _offset ) ;
	public char toChar( final byte[] _char, final int _offset ) ;
	public long toLong( final byte[] _long, final int _offset ) ;
	public short toShort( final byte[] _short, final int _offset ) ;
	public float toFloat( final byte[] _float, final int _offset ) ;
	public double toDouble( final byte[] _double, final int _offset ) ;
	public boolean toBoolean( final byte[] _bool, final int _offset ) ;

	public byte[] toBytes( final byte[] _bytes, final int _offset, final int _length ) ;
	public byte[] toBits( final byte[] _bytes, final int _byteOffset, final int _bitOffset, final int _bitLength ) ;

	public byte[] newBytes( final byte[] _bytes, final int _offset, final int _length ) ;
	public byte[] newInvertBytes( final byte[] _bytes, final int _offset, final int _length ) ;

	/**FLIP BYTE ARRAY ENDIAN**/

	public byte[] newflipEndian( final byte[] _bytes ) ;
	public byte[] flipEndian( final byte[] _bytes ) ;

	public byte[] newflipEndian( final byte[] _bytes, final int _offset, final int _length ) ;
	public byte[] flipEndian( final byte[] _bytes, final int _offset, final int _length ) ;

	public boolean isBitSet( final byte _byte, final int _position ) ;
	public boolean isBitSet( final byte[] _bytes, final int _position ) ;

	public byte setBit( final byte _byte, final int _position, final boolean _set ) ;

	public void setBit( final byte[] _bytes, final int _position, final boolean _set ) ;
	public void printByte( final byte _byte ) ;

	public byte[] concat( final byte[] _a, final byte[] _b ) ;
	public byte[] insert( final byte[] _a, final int _aOffset, final int _aLength, final byte[] _b, final int _bOffset ) ;
}
