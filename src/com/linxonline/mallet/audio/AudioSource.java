package com.linxonline.mallet.audio ;

import com.linxonline.mallet.util.SourceCallback ;

/**
	Defines what the AudioSystem can do to a sound.
**/
public interface AudioSource
{
	public void play() ;
	public void pause() ;
	public void playLoop() ;
	public void stop() ;

	public void setPosition( final float _x, final float _y, final float _z ) ;
	public void setRelative( final boolean _relative ) ;

	public State getState() ;

	public float getCurrentTime() ;
	public float getDuration() ;

	public void setVolume( final int _volume ) ;

	public void setCallback( final SourceCallback _callback ) ;
	public SourceCallback getCallback() ;
	
	public void destroySource() ;
	
	public enum State
	{
		UNKNOWN,
		PLAYING,
		PAUSED,
		STOPPED
	}
}
