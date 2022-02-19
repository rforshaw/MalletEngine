package com.linxonline.mallet.audio ;

import com.linxonline.mallet.util.SourceCallback ;

/**
	Defines what the AudioSystem can do to a sound.
**/
public interface ISource
{
	/**
		Play the source from the beginning if stopped,
		or continue playing the source if paused.
	*/
	public boolean play() ;

	/**
		Pause the source, will continue from where it
		was paused if play() is called.
	*/
	public boolean pause() ;

	/**
		Play the source in a continuous loop.
	*/
	public boolean playLoop() ;

	/**
		Stop the source from playing, when play()
		is called again it was start from the beginning.
	*/
	public boolean stop() ;

	/**
		Set the position of the source in 3D space.
		Used when the source is flagged as relative.
		Allows the source to sound quieter or louder
		depending on how far away from the listener it is.
	*/
	public boolean setPosition( final float _x, final float _y, final float _z ) ;

	/**
		Allows the source to be defined in 3D space.
		Used in conjunction with setPosition().
		Allows the source to sound quieter or louder
		depending on how far away from the listener it is.
	*/
	public boolean setRelative( final boolean _relative ) ;

	/**
		Return the current state of the source.
		If the state is playing return PLAYING.
		If the state is paused return PAUSED,
		if the state is stopped return STOPPED,
		and if the state is not known return UNKNOWN.
		NOTE: if the state has not changed since the last
		time getState was called it should return UNCHANGED.
	*/
	public State getState() ;

	public float getCurrentTime() ;
	public float getDuration() ;

	public void setVolume( final int _volume ) ;

	public void setCallback( final SourceCallback _callback ) ;
	public SourceCallback getCallback() ;

	public void destroy() ;

	public enum State
	{
		UNKNOWN,
		PLAYING,
		PAUSED,
		STOPPED,
		UNCHANGED
	}
}
