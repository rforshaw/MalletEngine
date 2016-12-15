package com.linxonline.mallet.audio ;

/**
	Defines what the AudioSystem can do to a sound.
**/
public interface AudioSource
{
	public void play() ;
	public void pause() ;
	public void playLoop() ;
	public void stop() ;

	public boolean isPlaying() ;

	public float getCurrentTime() ;
	public float getDuration() ;

	public void setVolume( final int _volume ) ;

	public void destroySource() ;
}
