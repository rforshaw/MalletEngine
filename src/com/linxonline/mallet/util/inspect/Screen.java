package com.linxonline.mallet.util.inspect ;

import com.linxonline.mallet.maths.Ratio ;

import com.linxonline.mallet.util.ISort ;
import com.linxonline.mallet.util.QuickSort ;

/**
	Screen represents a virtual space that may include
	one or more monitors.
**/
public class Screen
{
	private final String id ;
	private final Monitor[] monitors ;

	public Screen( final String _id, final Monitor[] _monitors )
	{
		id = _id ;
		monitors = _monitors ;
	}

	/**
		Returns the unique identifier for this screen
	**/
	public String getID()
	{
		return id ;
	}

	public Monitor[] getMonitors()
	{
		return monitors ;
	}

	public Monitor getPrimaryMonitor()
	{
		for( final Monitor monitor : monitors )
		{
			if( monitor.isPrimary() )
			{
				return monitor ;
			}
		}

		return monitors[0] ;
	}

	@Override
	public String toString()
	{
		String screens = "Name: " + id + "\n" ;
		for( int i = 0; i < monitors.length; i++ )
		{
			screens += monitors[i].toString() ;
		}
		return screens ;
	}

	/**
		A monitor represents a physical device.
		TODO: Return the monitors current mode.
	*/
	public static class Monitor
	{
		private final String id ;
		private final Mode[] modes ;

		private final boolean primary ;
		private final float dpmm ;

		public Monitor(
			final String _id,
			final boolean _primary,
			final float _dpmm,
			final Mode[] _modes )
		{
			id = _id ;
			modes = _modes ;
			primary = _primary ;
			dpmm = _dpmm ;
		}

		public boolean isPrimary()
		{
			return primary ;
		}

		public float getDPMM()
		{
			return dpmm ;
		}

		public int getDPI()
		{
			return ( int )Math.ceil( dpmm * 25.4f ) ;
		}

		/**
			Returns a list of different modes the screen can be placed in.
		**/
		public Mode[] getModes()
		{
			return modes ;
		}

		public Mode getBestMode()
		{
			final Mode[] m = QuickSort.quicksort( getModes() ) ;
			return ( m.length > 0 ) ? m[m.length - 1] : null ;
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

		/**
			Store the information about a particular state a screen can cope with.
			Use a bitDepth of -1 to represent an unknown value.
			Use a refreshRate of 0 to denote an unknown value.
		**/
		public static class Mode implements ISort
		{
			private final int width ;
			private final int height ;
			private final int bitDepth ;
			private final int refreshRate ;

			public Mode( final int _width, final int _height, final int _bitDepth, final int _refreshRate )
			{
				width = _width ;
				height = _height ;
				bitDepth = _bitDepth ;
				refreshRate = _refreshRate ;
			}

			public int getWidth()
			{
				return width ;
			}

			public int getHeight()
			{
				return height ;
			}

			public int getBitDepth()
			{
				return bitDepth ;
			}

			public int getRefreshRate()
			{
				return refreshRate ;
			}

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
	}
}
