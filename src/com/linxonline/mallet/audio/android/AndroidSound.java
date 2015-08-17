package com.linxonline.mallet.audio.android ;

import com.linxonline.mallet.resources.sound.* ;

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

	public void destroy() {}
}