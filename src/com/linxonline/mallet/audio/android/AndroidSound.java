package com.linxonline.mallet.audio.android ;

import com.linxonline.mallet.audio.SoundInterface ;

public class AndroidSound implements SoundInterface
{
	private final byte[] buffer ;

	public AndroidSound( final byte[] _buffer )
	{
		buffer = _buffer ;
	}

	public byte[] getBuffer()
	{
		return buffer ;
	}

	/**
		Return the audio buffer size in bytes.
	*/
	@Override
	public long getMemoryConsumption()
	{
		return buffer.length ;
	}

	public void destroy() {}
}
