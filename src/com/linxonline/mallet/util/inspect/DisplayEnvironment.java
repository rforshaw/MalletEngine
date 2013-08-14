package com.linxonline.mallet.util.inspect ;

import java.awt.GraphicsEnvironment ;
import java.awt.GraphicsDevice ;
import java.awt.DisplayMode ;

public class DisplayEnvironment
{
	public DisplayEnvironment() {}

	/**
		Returns the detected screens located on the running device.
	**/
	public Screen[] getScreens()
	{
		final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment() ;
		final GraphicsDevice[] device = env.getScreenDevices() ;
		final Screen[] screens = new Screen[device.length] ;

		for( int i = 0; i < device.length; i++ )
		{
			final DisplayMode[] displayModes = device[i].getDisplayModes() ;
			final ScreenMode[] screenModes = new ScreenMode[displayModes.length] ;

			for( int j = 0; j < displayModes.length; j++ )
			{
				final DisplayMode mode = displayModes[j] ;
				screenModes[j] = new ScreenMode( mode.getWidth(), mode.getHeight(), 
												 mode.getBitDepth(), mode.getRefreshRate() ) ;
			}

			screens[i] = new Screen( device[i].getIDstring(), screenModes ) ;
		}

		return screens ;
	}
	
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer() ;
		Screen[] screens = getScreens() ;
		for( final Screen screen : screens )
		{
			buffer.append( screen.toString() + "\n" ) ;
		}

		return buffer.toString() ;
	}
}