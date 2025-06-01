package com.linxonline.mallet.util.inspect ;

import java.util.Collection ;
import java.util.List ;

import com.jogamp.newt.util.MonitorModeUtil ;
import com.jogamp.newt.MonitorDevice ;
import com.jogamp.newt.MonitorMode ;

import com.linxonline.mallet.util.inspect.* ;

public class DisplayEnvironment
{
	private Screen[] screens = null ;

	public DisplayEnvironment() {}

	public Screen[] getScreens()
	{
		if( screens != null )
		{
			return screens ;
		}

		final Collection<com.jogamp.newt.Screen> newtScreens = com.jogamp.newt.Screen.getAllScreens() ;
		final Screen[] screens = new Screen[newtScreens.size()] ;

		int screenIndex = 0 ;
		for( com.jogamp.newt.Screen s : newtScreens )
		{
			final List<MonitorDevice> mds = s.getMonitorDevices() ;
			final Screen.Monitor[] monitors = new Screen.Monitor[mds.size()] ;

			final int monitorSize = mds.size() ;
			for( int i = 0; i < monitorSize; ++i )
			{
				final MonitorDevice md = mds.get( i ) ;

				final List<MonitorMode> ms = MonitorModeUtil.filterByRotation( md.getSupportedModes(), 0 ) ;
				final Screen.Monitor.Mode[] modes = new Screen.Monitor.Mode[ms.size()] ;

				for( int j = 0; j < modes.length; ++j )
				{
					final MonitorMode mm = ms.get( j ) ;

					final int w = mm.getRotatedWidth() ;
					final int h = mm.getRotatedHeight() ;
					final int bpp = mm.getSurfaceSize().getBitsPerPixel() ;
					final int rr = ( int )mm.getRefreshRate() ;

					modes[j] = new Screen.Monitor.Mode( w, h, bpp, rr ) ;
				}

				final String name = md.getName() ;
				final boolean primary = md.isPrimary() ;

				final float[] ppmm = md.getPixelsPerMM( new float[2] ) ;
				final float dpmm = ( ppmm[0] + ppmm[1] ) * 0.5f ;

				monitors[i] = new Screen.Monitor( name, primary, dpmm, modes ) ;
			}

			screens[screenIndex++] = new Screen( s.getFQName(), monitors ) ;
		}

		return screens ;
	}

	// Return the DPI of the primary monitor.
	public int getDPI()
	{
		final Screen[] screens = getScreens() ;
		final Screen screen = screens[0] ;

		final Screen.Monitor monitor = screen.getPrimaryMonitor() ;
		return monitor.getDPI() ;
	}

	/**
		A screen represents a virtual space that cane be made up of one
		or more monitors.
	*/
	private final com.jogamp.newt.Screen getActiveScreen()
	{
		for( final com.jogamp.newt.Screen screen : com.jogamp.newt.Screen.getAllScreens() )
		{
			// We'll return the first screen and assume it's
			// the active one.
			return screen ;
		}

		return null ;
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
