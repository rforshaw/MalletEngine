package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

public class MalletColour
{
	public final static byte RED = 0 ;
	public final static byte GREEN = 1 ;
	public final static byte BLUE = 2 ;
	public final static byte ALPHA = 3 ;

	public final byte[] colours = new byte[4] ; 	// red, green, blue, alpha

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

	public void changeColour( final byte _red, final byte _green, final byte _blue, final byte _alpha )
	{
		colours[RED] = _red ;
		colours[GREEN] = _green ;
		colours[BLUE] = _blue ;
		colours[ALPHA] = _alpha ;
	}

	public int getRed()
	{
		return colours[RED] ;
	}

	public int getGreen()
	{
		return colours[GREEN] ;
	}

	public int getBlue()
	{
		return colours[BLUE] ;
	}

	public int getAlpha()
	{
		return colours[ALPHA] ;
	}

	public int toRGBA()
	{
		return ConvertBytes.toInt( colours, 0, 4 ) ;
	}
}
