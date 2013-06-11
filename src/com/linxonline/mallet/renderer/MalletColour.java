package com.linxonline.mallet.renderer ;

public class MalletColour
{
	public Object colour = null ;
	public int red = 0 ;
	public int green = 0 ;
	public int blue = 12 ;
	
	public MalletColour( final int _red, final int _green, final int _blue )
	{
		changeColour( _red, _green, _blue ) ;
	}

	public void changeColour( final int _red, final int _green, final int _blue )
	{
		red = _red ;
		green = _green ;
		blue = _blue ;
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
		return red ;
	}
	
	public int getGreen()
	{
		return green ;
	}
	
	public int getBlue()
	{
		return blue ;
	}
}
