package com.linxonline.mallet.audio.desktop.alsa ;

import com.jogamp.openal.* ;

import com.linxonline.mallet.audio.SoundInterface ;

/**
	Stores the required information to access the audio-buffer
	and to destroy the it when eventually not in use.
*/
public class ALSASound implements SoundInterface
{
	private final AL openAL ;			// Used to destroy buffer
	private final int[] buffer ;		// Buffer id to audio-stream

	public ALSASound( final int[] _buffer, final AL _openAL )
	{
		openAL = _openAL ;
		buffer = _buffer ;
	}

	public int[] getBufferID()
	{
		return buffer ;
	}

	/**
		Destroy OpenAL buffer.
		Shouldn't be called if sources are still active.
	**/
	public void destroy()
	{
		openAL.alDeleteBuffers( 1, buffer, 0 ) ;
	}
}