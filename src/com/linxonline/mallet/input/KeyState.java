package com.linxonline.mallet.input ;

public final class KeyState
{
	public boolean changed = false ;
	public InputEvent input = null ;
	public long pressedTimeStamp = 0L ;
	public long releasedTimeStamp = 0L ;

	public KeyState( final InputEvent _input, final boolean _changed )
	{
		input = _input ;
		changed = _changed ;
	} 
}