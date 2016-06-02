package com.linxonline.mallet.util.worker ;

import java.util.List ;

public interface Worker<T>
{
	/**
		Called by the WorkerThread.
		Once the WorkerThread knowns what subset of 
		data it will be processing.
		This function will potentially be called by multiple 
		threads. Function scope variables will not cause concurrency 
		issues, however class scope upwards will.
	*/
	public ExecType exec( final T _data ) ;

	/**
		Return the entire list that the threads 
		will work on.
		WorkerThreads will call this function and 
		loop over a subset.
	*/
	public List<T> getDataSet() ;
}