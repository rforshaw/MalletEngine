package com.linxonline.mallet.io.serialisation ;

/**
	SerialiseOutput and SerialiseInput work in tandem.

	If SerialiseOutput.writeBytes is called and X bytes is 
	passed in, then the subsequent SerialiseInput will return 
	the same X bytes.

	SerialiseOutput.writeBytes( new byte[10] ) ;
	SerialiseOutput.writeBytes( new byte[5] ) ;
	SerialiseOutput.writeBytes( new byte[8] ) ;

	SerialiseInput.readBytes() ; <--- Will return 10
	SerialiseInput.readBytes() ; <--- Will return 5
	SerialiseInput.readBytes() ; <--- Will return 8

	All implementations should guarantee this. Note must be called 
	in identical order.
**/
public interface SerialiseInput
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