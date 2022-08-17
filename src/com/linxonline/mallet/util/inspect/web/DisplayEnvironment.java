package com.linxonline.mallet.util.inspect ;

import com.linxonline.mallet.util.inspect.* ;
import com.linxonline.mallet.maths.* ;

public class DisplayEnvironment
{
	public DisplayEnvironment() {}

	public Screen[] getScreens()
	{
		final ScreenMode[] screenModes = new ScreenMode[1] ;
		screenModes[0] = new ScreenMode( 1280, 720, 32, 60 ) ;

		final Screen[] screens = new Screen[1] ;
		screens[0] = new Screen( "Default", screenModes ) ;

		return screens ;
	}

	public int getDPI()
	{
		return 90 ;
	}

	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer() ;
		final Screen[] screens = getScreens() ;
		for( final Screen screen : screens )
		{
			buffer.append( screen.toString() + "\n" ) ;
		}

		return buffer.toString() ;
	}
}
