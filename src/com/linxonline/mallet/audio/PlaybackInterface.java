package com.linxonline.mallet.audio ;

/**
	Allows the Developer to keep track of the progress of an AudioSource
**/
public interface PlaybackInterface
{
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
	public void endPlayback() ;
}