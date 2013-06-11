package com.linxonline.mallet.util.time ;

public class DefaultTimer implements TimerInterface
{
	private long oldTime = System.nanoTime() ;
	private long currentTime = System.nanoTime() ;
	private long elapsedTime = 0 ;
	private long totalTimeSeconds = 0 ;
	private double nanoseconds = 0.0f ;
	private double seconds = 0.0f ;

	public DefaultTimer() {}

	public final double getElapsedTimeInNanoSeconds()
	{
		nanoseconds = ( double )getLongElapsedTime() * 0.000000001 ;
		seconds += nanoseconds ;
		while( seconds >= 1.0f )
		{
			seconds -= 1.0f ;
			totalTimeSeconds++ ;
		}

		return nanoseconds ;
	}

	public final long getTotalElapsedTimeInSeconds()
	{
		return totalTimeSeconds ;
	}

	private final long getLongElapsedTime()
	{
		oldTime = currentTime ;
		currentTime = System.nanoTime() ;
		
		return elapsedTime = currentTime - oldTime ;
	}
}