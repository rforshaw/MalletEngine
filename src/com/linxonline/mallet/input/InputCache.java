package com.linxonline.mallet.input ;

import java.util.LinkedList ;

import com.linxonline.mallet.util.time.ElapsedTimer ;

public class InputCache
{
	private final LinkedList<InputTime> cache = new LinkedList<InputTime>() ;
	private float reuseTime = 0.0f ;
	private int currentPos = 0 ;

	// Reduce temporaries
	private InputTime inputTime = null ;

	public InputCache( final float _reuseTime )
	{
		addNewInput( 0, 0.0f, new InputEvent() ) ;
		reuseTime = _reuseTime ;
	}

	public InputEvent getInput()
	{
		if( currentPos >= cache.size() )
		{
			currentPos = 0 ;
		}

		final long timestamp = ElapsedTimer.getTotalElapsedTimeInSeconds() ;
		final double remainder = ElapsedTimer.getRemainderInNanoSeconds() ;

		inputTime = cache.get( currentPos ) ;
		float diff = ( float )( timestamp - inputTime.timestamp ) ;
		diff += ( float )( remainder - inputTime.remainder ) ;

		if( diff >= reuseTime )
		{
			++currentPos ;
			inputTime.timestamp = timestamp ;
			inputTime.remainder = remainder ;
			return inputTime.input ;
		}

		// InputEvent isn't available, create a new one then.
		return insertNewInput( currentPos++, timestamp, remainder, new InputEvent() ).input ;
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

	private InputTime insertNewInput( final int _pos, final long _timestamp, final double _remiander, final InputEvent _input )
	{
		final InputTime inputTime = new InputTime( _timestamp, _remiander, _input ) ;
		cache.add( _pos, inputTime ) ;
		return inputTime ;
	}

	private InputTime addNewInput( final long _timestamp, final double _remiander, final InputEvent _input )
	{
		final InputTime inputTime = new InputTime( _timestamp, _remiander, _input ) ;
		cache.add( inputTime ) ;
		return inputTime ;
	}

	public class InputTime
	{
		public long timestamp ;
		public double remainder ;
		public final InputEvent input ;

		public InputTime( final long _timestamp, final double _remiander, final InputEvent _input )
		{
			timestamp = _timestamp ;
			remainder = _remiander ;
			input = _input ;
		}
	}
}