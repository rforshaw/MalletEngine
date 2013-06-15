package com.linxonline.mallet.audio ;

/**
	Allows the Developer to keep track of the progress of an AudioSource
**/
public interface PlaybackInterface
{
	public final static int FINISHED_PLAYBACK = 0 ;
	public final static int STOP_PLAYBACK = 10 ;
	public final static int PAUSE_PLAYBACK = 20 ;

	/**
		Informs when the AudioSource has begun playing
	**/
	public void startPlayback() ;

	/**
		Informs what position in the AudioSource is currently being played.
	**/
	public void updatePlayback( final float _dt ) ;

	/**
		Informs when the AudioSource has stopped playing
	**/
	public void endPlayback( final int _type ) ;
}