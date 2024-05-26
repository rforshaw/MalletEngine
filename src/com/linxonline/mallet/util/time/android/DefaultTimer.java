package com.linxonline.mallet.util.time ;

public final class DefaultTimer
{
	private long oldTime = System.nanoTime() ;
	private long currentTime = System.nanoTime() ;
	private long totalTimeSeconds = 0 ;
	private double nanoseconds = 0.0f ;
	private double seconds = 0.0f ;

	public DefaultTimer() {}

	public double getElapsedTimeInNanoSeconds()
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
		return System.nanoTime() ;
	}

	private long getLongElapsedTime()
	{
		oldTime = currentTime ;
		currentTime = System.nanoTime() ;

		return currentTime - oldTime ;
	}
}
