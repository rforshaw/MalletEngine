package com.linxonline.mallet.util.time ;

import org.teavm.jso.* ;
import org.teavm.jso.browser.* ;

import com.linxonline.mallet.util.time.* ;

public class DefaultTimer implements ITimer
{
	private long oldTime = ( long )Performance.now() ;
	private long currentTime = ( long )Performance.now() ;
	private long totalTimeSeconds = 0 ;
	private double nanoseconds = 0.0f ;
	private double seconds = 0.0f ;

	public DefaultTimer() {}

	@Override
	public final double getElapsedTimeInNanoSeconds()
	{
		nanoseconds = ( double )getLongElapsedTime() * 0.000000001 ;
		seconds += nanoseconds ;		// Accumulate the nanoseconds
		while( seconds >= 1.0f )		// Once it reaches atleast 1 second store it 
		{
			seconds -= 1.0f ;
			++totalTimeSeconds ;
		}

		return nanoseconds ;
	}

	@Override
	public final long getTotalElapsedTimeInSeconds()
	{
		return totalTimeSeconds ;
	}

	@Override
	public final double getRemainderInNanoSeconds()
	{
		return seconds ;
	}

	@Override
	public long nanoTime()
	{
		return ( long )Performance.now() ;
	}

	private final long getLongElapsedTime()
	{
		oldTime = currentTime ;
		currentTime = ( long )Performance.now() ;

		return currentTime - oldTime ;
	}
}
