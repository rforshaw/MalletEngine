package com.linxonline.mallet.audio ;

import com.linxonline.mallet.io.Resource ;

/**
	Container class for storing the access point to a 
	particular sound-buffer.
	Provides the ability for the developer to use their
	preferred sound API.
	Sound used by the Sound Manager to prevent an audio-buffer 
	or an audio-stream from being loaded multiple times.
**/
public final class AudioBuffer<T extends SoundInterface> extends Resource
{
	private final T buffer ;

	public AudioBuffer( final T _buffer )
	{
		buffer = _buffer ;
	}

	public T getBuffer()
	{
		return buffer ;
	}

	@Override
	public long getMemoryConsumption()
	{
		return buffer.getMemoryConsumption() ;
	}

	@Override
	public void destroy()
	{
		buffer.destroy() ;
	}

	@Override
	public String type()
	{
		return "SOUND" ;
	}
}
