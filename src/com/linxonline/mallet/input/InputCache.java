package com.linxonline.mallet.input ;

import java.util.LinkedList ;

import com.linxonline.mallet.util.time.ElapsedTimer ;

public class InputCache
{
	private final LinkedList<InputTime> cache = new LinkedList<InputTime>() ;
	private long reuseTime = 0L ;
	private int currentPos = 0 ;

	public InputCache( final int _seconds )
	{
		addNewInput( ElapsedTimer.getTotalElapsedTimeInSeconds(), new InputEvent() ) ;
		reuseTime = _seconds ;
	}

	public InputEvent getInput()
	{
		System.out.println( cache.size() ) ;
		if( currentPos >= cache.size() )
		{
			currentPos = 0 ;
		}

		final long timestamp = ElapsedTimer.getTotalElapsedTimeInSeconds() ;
		final InputTime inputTime = cache.get( currentPos ) ;
		final long diff = timestamp - inputTime.timestamp ;

		System.out.println( "Diff: " + diff ) ;
		
		if( diff >= reuseTime  )
		{
			++currentPos ;
			inputTime.timestamp = timestamp ;
			return inputTime.input ;
		}

		return insertNewInput( currentPos++, timestamp, new InputEvent() ).input ;
	}

	public void trimToSize( final int _trim )
	{
		/*final int size = cache.size() ;
		if( _trim < size )
		{
			cache.removeRange( _trim + 1, size ) ;
		}*/
	}

	public int size()
	{
		return cache.size() ;
	}

	private InputTime insertNewInput( final int _pos, final long _timestamp, final InputEvent _input )
	{
		final InputTime inputTime = new InputTime( _timestamp, _input ) ;
		cache.add( _pos, inputTime ) ;
		return inputTime ;
	}

	private InputTime addNewInput( final long _timestamp, final InputEvent _input )
	{
		final InputTime inputTime = new InputTime( _timestamp, _input ) ;
		cache.add( inputTime ) ;
		return inputTime ;
	}

	public class InputTime
	{
		public long timestamp ;
		public final InputEvent input ;

		public InputTime( final long _timestamp, final InputEvent _input )
		{
			timestamp = _timestamp ;
			input = _input ;
		}
	}
}