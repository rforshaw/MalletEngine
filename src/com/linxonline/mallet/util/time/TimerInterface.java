package com.linxonline.mallet.util.time ;

public interface TimerInterface
{
	public double getElapsedTimeInNanoSeconds() ;

	public long getTotalElapsedTimeInSeconds() ;
	public double getRemainderInNanoSeconds() ; 

	public long nanoTime() ;
}