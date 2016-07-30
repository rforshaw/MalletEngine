package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

/**
	Define a colour with 8888 RGBA
*/
public class MalletColour
{
	public final static byte RED   = 0 ;
	public final static byte GREEN = 1 ;
	public final static byte BLUE  = 2 ;
	public final static byte ALPHA = 3 ;

	public final byte[] colours = new byte[4] ; 	// red, green, blue, alpha

	public MalletColour()
	{
		this( 0, 0, 0 ) ;
	}

	public MalletColour( final int _red, final int _green, final int _blue )
	{
		changeColour( ( byte )_red, ( byte )_green, ( byte )_blue, ( byte )255 ) ;
	}

	public MalletColour( final int _red, final int _green, final int _blue, final int _alpha )
	{
		changeColour( ( byte )_red, ( byte )_green, ( byte )_blue, ( byte )_alpha ) ;
	}

	public MalletColour( final byte _red, final byte _green, final byte _blue  )
	{
		changeColour( _red, _green, _blue, ( byte )255 ) ;
	}

	public MalletColour( final byte _red, final byte _green, final byte _blue, final byte _alpha )
	{
		changeColour( _red, _green, _blue, _alpha ) ;
	}

	public MalletColour( final int _colour )
	{
		changeColour( _colour ) ;
	}
	
	public MalletColour( final float _colour )
	{
		changeColour( _colour ) ;
	}

	public MalletColour( final MalletColour _colour )
	{
		changeColour( _colour.colours[RED], _colour.colours[GREEN], _colour.colours[BLUE], _colour.colours[ALPHA] ) ;
	}

	public void changeColour( final int _colour )
	{
		final byte[] c = ConvertBytes.toBytes( _colour, ConvertBytes.nativeOrder() ) ;
		changeColour( c[0], c[1], c[2], c[3] ) ;
	}

	public void changeColour( final float _colour )
	{
		final byte[] c = ConvertBytes.toBytes( _colour, ConvertBytes.nativeOrder() ) ;
		changeColour( c[0], c[1], c[2], c[3] ) ;
	}

	public void changeColour( final byte _red, final byte _green, final byte _blue, final byte _alpha )
	{
		colours[RED]   = _red ;
		colours[GREEN] = _green ;
		colours[BLUE]  = _blue ;
		colours[ALPHA] = _alpha ;
	}

	public int getRed()
	{
		return colours[RED] & 0xFF ;
	}

	public int getGreen()
	{
		return colours[GREEN] & 0xFF ;
	}

	public int getBlue()
	{
		return colours[BLUE] & 0xFF ;
	}

	public int getAlpha()
	{
		return colours[ALPHA] & 0xFF ;
	}

	public int toInt()
	{
		return ConvertBytes.toInt( colours, 0, 4 ) ;
	}

	public float toFloat()
	{
		return ConvertBytes.toFloat( colours, 0, 4 ) ;
	}

	public String toString()
	{
		final StringBuilder buffer = new StringBuilder() ;
		buffer.append( "R: " ) ; buffer.append( getRed() ) ;
		buffer.append( " G: " ) ; buffer.append( getGreen() ) ;
		buffer.append( " B: " ) ; buffer.append( getBlue() ) ;
		buffer.append( " A: " ) ; buffer.append( getAlpha() ) ;
		return buffer.toString() ;
	}
	
	public static MalletColour white()
	{
		return new MalletColour( 255, 255, 255 ) ;
	}

	public static MalletColour black()
	{
		return new MalletColour( 0, 0, 0 ) ;
	}

	public static MalletColour red()
	{
		return new MalletColour( 255, 0, 0 ) ;
	}

	public static MalletColour green()
	{
		return new MalletColour( 0, 255, 0 ) ;
	}

	public static MalletColour blue()
	{
		return new MalletColour( 0, 0, 255 ) ;
	}
}
