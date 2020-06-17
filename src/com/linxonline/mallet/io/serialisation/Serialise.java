package com.linxonline.mallet.io.serialisation ;

/**
	Serialise.Out and Serialise.In work in tandem.

	If Serialise.Out.writeBytes is called and X bytes is 
	passed in, then the subsequent Serialise.Inp will return 
	the same X bytes.

	Serialise.Out.writeBytes( new byte[10] ) ;
	Serialise.Out.writeBytes( new byte[5] ) ;
	Serialise.Out.writeBytes( new byte[8] ) ;

	Serialise.In.readBytes() ; <--- Will return 10
	Serialise.In.readBytes() ; <--- Will return 5
	Serialise.In.readBytes() ; <--- Will return 8

	All implementations should guarantee this. Note must be called 
	in identical order.
**/
public interface Serialise
{
	public interface In
	{
		public int readInt() ;
		public byte readByte() ;
		public char readChar() ;
		public long readLong() ;
		public float readFloat() ;
		public double readDouble() ;
		public String readString() ;
		public boolean readBoolean() ;

		public int[] readInts() ;
		public byte[] readBytes() ;
		public char[] readChars() ;
		public long[] readLongs() ;
		public float[] readFloats() ;
		public double[] readDoubles() ;
		public String[] readStrings() ;
		public boolean[] readBooleans() ;
	}

	public interface Out
	{
		public void writeInt( final int _int ) ;
		public void writeByte( final byte _byte ) ;
		public void writeChar( final char _char ) ;
		public void writeLong( final long _long ) ;
		public void writeFloat( final float _float ) ;
		public void writeDouble( final double _double ) ;
		public void writeString( final String _string ) ;
		public void writeBoolean( final boolean _bool ) ;

		public void writeInts( final int[] _int ) ;
		public void writeBytes( final byte[] _byte ) ;
		public void writeChars( final char[] _char ) ;
		public void writeLongs( final long[] _long ) ;
		public void writeFloats( final float[] _float ) ;
		public void writeDoubles( final double[] _double ) ;
		public void writeStrings( final String[] _string ) ;
		public void writeBooleans( final boolean[] _bool ) ;
	}
}
