package com.linxonline.mallet.renderer ;

import java.util.Arrays ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

/**
	Define a colour with 8888 RGBA
*/
public class MalletColour
{
	public final static byte RED   = 3 ;
	public final static byte GREEN = 2 ;
	public final static byte BLUE  = 1 ;
	public final static byte ALPHA = 0 ;

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
		ConvertBytes.toBytes( _colour, colours ) ;
	}

	public void changeColour( final float _colour )
	{
		ConvertBytes.toBytes( _colour, colours ) ;
	}

	public void changeColour( final byte _red, final byte _green, final byte _blue, final byte _alpha )
	{
		colours[RED]   = _red ;
		colours[GREEN] = _green ;
		colours[BLUE]  = _blue ;
		colours[ALPHA] = _alpha ;
	}

	public void changeColour( final MalletColour _colour )
	{
		changeColour( _colour.colours[RED], _colour.colours[GREEN], _colour.colours[BLUE], _colour.colours[ALPHA] ) ;
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

	public byte getRedAsByte()
	{
		return colours[RED] ;
	}

	public byte getGreenAsByte()
	{
		return colours[GREEN] ;
	}

	public byte getBlueAsByte()
	{
		return colours[BLUE] ;
	}

	public byte getAlphaAsByte()
	{
		return colours[ALPHA] ;
	}

	public int toInt()
	{
		return ConvertBytes.toInt( colours, 0 ) ;
	}

	public float toFloat()
	{
		return ConvertBytes.toFloat( colours, 0 ) ;
	}

	@Override
	public boolean equals( final Object _obj )
	{
		if( this == _obj )
		{
			return true ;
		}

		if( _obj == null )
		{
			return false ;
		}

		if( _obj instanceof MalletColour )
		{
			final MalletColour col = ( MalletColour )_obj ;
			return colours[RED]   == col.colours[RED]   &&
				   colours[GREEN] == col.colours[GREEN] &&
				   colours[BLUE]  == col.colours[BLUE]  &&
				   colours[ALPHA] == col.colours[ALPHA] ;
		}

		return false ;
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( colours ) ;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder buffer = new StringBuilder() ;
		buffer.append( "R: " ) ; buffer.append( getRed() ) ;
		buffer.append( " G: " ) ; buffer.append( getGreen() ) ;
		buffer.append( " B: " ) ; buffer.append( getBlue() ) ;
		buffer.append( " A: " ) ; buffer.append( getAlpha() ) ;
		return buffer.toString() ;
	}

	public static final MalletColour parseColour( final String _text )
	{
		if( _text == null )
		{
			return null ;
		}

		if( _text.isEmpty() == true )
		{
			return null ;
		}

		final String[] split = _text.split( "," ) ;
		int red    = 255 ;
		int green  = 255 ;
		int blue   = 255 ;
		int alpha  = 255 ;

		if( split.length >= 3 )
		{
			red   = Integer.parseInt( split[0].trim() ) ;
			green = Integer.parseInt( split[1].trim() ) ;
			blue  = Integer.parseInt( split[2].trim() ) ;
		}

		if( split.length >= 4 )
		{
			alpha = Integer.parseInt( split[3].trim() ) ;
		}

		return new MalletColour( red, green, blue, alpha ) ;
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
