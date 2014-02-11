package com.linxonline.mallet.util.time ;

/**
	Should contain functions in relation to processing Time.
	Placing them in appropriate formats, etc.

	A Developer wishing to use the Total Elapsed Time should use this 
	class, to reduce the possibility of corrupting the ElapsedTimer 
	used by the engine.
**/
public class Time
{
	public Time() {}

	public static final long getTotalElapsedTimeInSeconds()
	{
		return ElapsedTimer.getTotalElapsedTimeInSeconds() ;
	}

	public static final double getRemainderInNanoSeconds()
	{
		return ElapsedTimer.getRemainderInNanoSeconds() ;
	}
}