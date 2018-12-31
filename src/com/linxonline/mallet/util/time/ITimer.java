package com.linxonline.mallet.util.time ;

public interface ITimer
{
	public double getElapsedTimeInNanoSeconds() ;

	public long getTotalElapsedTimeInSeconds() ;
	public double getRemainderInNanoSeconds() ; 

	public long nanoTime() ;
}
