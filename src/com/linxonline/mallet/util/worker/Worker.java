package com.linxonline.mallet.util.thread ;

import java.util.List ;

public abstract class Worker<T>
{
	public final ExecType exec( final List<T> _dataset, final int _start, final int _end )
	{
		for( int i = _start; i < _end; i++ )
		{
			if( exec( i, _dataset.get( i ) ) == ExecType.FINISH ) 		// Does the hard execution work
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

	public enum ExecType
	{
		CONTINUE,
		FINISH
	}
}
