package com.linxonline.mallet.util.time ;

/*==================================================*/
// Elapsed Timer is used to calculate how long the 
// application has been running for. And the delta 
// time between Elapsed calls.
/*==================================================*/
public final class ElapsedTimer
{
	private static final DefaultTimer time = new DefaultTimer() ;

	private ElapsedTimer() {}

	/**
		Must only be called by game loop
	*/
	public static final double getElapsedTimeInNanoSeconds()
	{
		return time.getElapsedTimeInNanoSeconds() ;
	}

	public static final long getTotalElapsedTimeInSeconds()
	{
		return time.getTotalElapsedTimeInSeconds() ;
	}

	public static final long nanoTime()
	{
		return time.nanoTime() ;
	}
	
	public static final double getRemainderInNanoSeconds()
	{
		return time.getRemainderInNanoSeconds() ;
	}
}
