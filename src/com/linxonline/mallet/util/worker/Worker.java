package com.linxonline.mallet.util.worker ;

import java.util.List ;

public abstract class Worker<T>
{
	public final ExecType exec( final int _start, final int _end )
	{
		final List<T> list = getDataSet() ;
		for( int i = _start; i < _end; i++ )
		{
			if( exec( i, list.get( i ) ) == ExecType.FINISH ) 		// Does the hard execution work
			{
				return ExecType.FINISH ;
			}
		}

		return ExecType.FINISH ;
	}

	/**
		Called by the WorkerThread.
		Once the WorkerThread knowns what subset of 
		data it will be processing.
		This function will potentially be called by multiple 
		threads. Function scope variables will not cause concurrency 
		issues, however class scope upwards will.
	*/
	public abstract ExecType exec( final int _index, final T _data ) ;

	/**
		Return the entire list that the threads 
		will work on.
		WorkerThreads will call this function and 
		loop over a subset.
	*/
	public abstract List<T> getDataSet() ;

	public enum ExecType
	{
		CONTINUE,
		FINISH
	}
}
