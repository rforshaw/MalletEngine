package com.linxonline.mallet.util.inspect ;

import java.util.ArrayList ;

import com.linxonline.mallet.util.sort.QuickSort ;

/**
	Screens contains the information about a specific screen 
	connected to the computer.
**/
public class Screen
{
	private final String id ;
	private final ScreenMode[] modes ;

	public Screen( final String _id, final ScreenMode[] _modes )
	{
		id = _id ;
		modes = _modes ;
	}

	/**
		Returns the unique identifier for this screen
	**/
	public String getID()
	{
		return id ;
	}

	/**
		Returns a list of different modes the screen can be placed in.
	**/
	public ScreenMode[] getScreenModes()
	{
		return modes ;
	}

	public ScreenMode getBestScreenMode()
	{
		ScreenMode[] screens = QuickSort.quicksort( getScreenModes() ) ;
		return screens[screens.length - 1] ;
	}

	@Override
	public String toString()
	{
		String screens = "Name: " + id + "\n" ;
		for( int i = 0; i < modes.length; i++ )
		{
			screens += modes[i].toString() ;
		}
		return screens ;
	}
}