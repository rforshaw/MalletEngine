package com.linxonline.mallet.io.serialisation ;

/**
	SerialiseOutput and SerialiseInput work in tandem.
	
	If SerialiseOutput.writeBytes is called and X bytes is 
	passed in, then the subsequent SerialiseInput will return 
	the same X bytes.

	SerialiseOutput.writeBytes( new byte[10] ) ;
	SerialiseOutput.writeFloat( 0.5f ) ;
	SerialiseOutput.writeString( "Boom Headshot" ) ;

	SerialiseInput.readBytes() ; <--- Will return 10
	SerialiseInput.readFloat() ; <--- Will return 0.5f
	SerialiseInput.readString() ; <--- Will return "Boom Headshot"

	All implementations should guarantee this. Note must be called 
	in identical order.
**/
public interface SerialiseOutput
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