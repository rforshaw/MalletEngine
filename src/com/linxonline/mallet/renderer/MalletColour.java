package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

public class MalletColour
{
	public final static int RED = 0 ;
	public final static int GREEN = 1 ;
	public final static int BLUE = 2 ;
	public final static int ALPHA = 3 ;

	public Object colour = null ;
	public byte[] colours = new byte[4] ; 	// red, green, blue, alpha

	public MalletColour( final int _red, final int _green, final int _blue )
	{
		changeColour( _red, _green, _blue, 255 ) ;
	}

	public MalletColour( final int _red, final int _green, final int _blue, final int _alpha )
	{
		changeColour( _red, _green, _blue, _alpha ) ;
	}

	public void changeColour( final int _red, final int _green, final int _blue, final int _alpha )
	{
		colours[RED] = ( byte )_red ;
		colours[GREEN] = ( byte )_green ;
		colours[BLUE] = ( byte )_blue ;
		colours[ALPHA] = ( byte )_alpha ;
		colour = null ;
	}

	public void setColour( final Object _colour )
	{
		colour = _colour ;
	}

	public Object getColour()
	{
		return colour ;
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
