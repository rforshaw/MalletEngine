package com.linxonline.mallet.util.time ;

import org.teavm.jso.* ;
import org.teavm.jso.browser.* ;

public final class DefaultTimer
{
	private long oldTime = ( long )Performance.now() ;
	private long currentTime = ( long )Performance.now() ;
	private long totalTimeSeconds = 0 ;
	private double nanoseconds = 0.0f ;
	private double seconds = 0.0f ;

	public DefaultTimer() {}

	public double getElapsedTimeInNanoSeconds()
	{
		nanoseconds = ( double )getLongElapsedTime() * 0.001 ;
		seconds += nanoseconds ;		// Accumulate the nanoseconds
		while( seconds >= 1.0f )		// Once it reaches atleast 1 second store it 
		{
			seconds -= 1.0f ;
			++totalTimeSeconds ;
		}

		return nanoseconds ;
	}

	public long getTotalElapsedTimeInSeconds()
	{
		return totalTimeSeconds ;
	}

	public double getRemainderInNanoSeconds()
	{
		return seconds ;
	}

	public long nanoTime()
	{
		final long nano = ( long )Performance.now() * 1000000 ;
		return nano ;
	}

	private long getLongElapsedTime()
	{
		oldTime = currentTime ;
		currentTime = ( long )Performance.now() ;

		return currentTime - oldTime ;
	}
}
