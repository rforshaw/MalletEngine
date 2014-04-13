package com.linxonline.mallet.util.inspect ;

import com.linxonline.mallet.maths.Ratio ;

import com.linxonline.mallet.util.sort.SortInterface ;

/**
	Store the information about a particular state a screen can cope with.
	With vanilla Java refreshRate of 0, means unknown, and bitDepth of -1 
	means either multi-depth supported, or unknown.
**/
public class ScreenMode implements SortInterface
{
	private final int width ;
	private final int height ;
	private final int bitDepth ;
	private final int refreshRate ;

	public ScreenMode( final int _width, final int _height, final int _bitDepth, final int _refreshRate )
	{
		width = _width ;
		height = _height ;
		bitDepth = _bitDepth ;
		refreshRate = _refreshRate ;
	}

	public int getWidth() { return width ; }
	public int getHeight() { return height ; }

	public int getBitDepth() { return bitDepth ; }
	public int getRefreshRate() { return refreshRate ; }

	public Ratio getRatio()
	{
		return Ratio.calculateRatio( width, height ) ;
	}

	@Override
	public int sortValue()
	{
		return width * height ;
	}

	@Override
	public String toString()
	{
		return "Width: " + width + "\nHeight: " + height + "\nBitDepth: " + bitDepth + "\nRefreshRate: " + refreshRate + "\n" ;
	}
}