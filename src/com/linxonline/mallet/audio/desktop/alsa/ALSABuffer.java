package com.linxonline.mallet.audio.desktop.alsa ;

import com.jogamp.openal.* ;

import com.linxonline.mallet.io.Resource ;

/**
	Stores the required information to access the audio-buffer
	and to destroy the it when eventually not in use.
*/
public final class ALSABuffer extends Resource
{
	private final AL openAL ;			// Used to destroy buffer
	private final int[] buffer ;		// Buffer id to audio-stream
	private final int consumption ;		// Buffer size

	public ALSABuffer( final int[] _buffer, final int _consumption, final AL _openAL )
	{
		openAL = _openAL ;
		buffer = _buffer ;
		consumption = _consumption ;
	}

	public int[] getBufferID()
	{
		return buffer ;
	}

	/**
		Return the audio buffer size in bytes.
	*/
	@Override
	public long getMemoryConsumption()
	{
		return consumption ;
	}

	/**
		Destroy OpenAL buffer.
		Shouldn't be called if sources are still active.
	**/
	@Override
	public void destroy()
	{
		openAL.alDeleteBuffers( 1, buffer, 0 ) ;
	}

	@Override
	public String type()
	{
		return "SOUND" ;
	}
}
