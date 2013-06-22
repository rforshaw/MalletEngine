package com.linxonline.mallet.util.inspect ;

import java.util.ArrayList ;

public class Screen
{
	private final String id ;
	private final ScreenMode[] modes ;

	public Screen( final String _id, final ScreenMode[] _modes )
	{
		id = _id ;
		modes = _modes ;
	}
	
	public ScreenMode[] getScreenModes() { return modes ; }

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