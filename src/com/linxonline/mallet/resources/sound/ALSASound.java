package com.linxonline.mallet.resources.sound ;

import com.jogamp.openal.* ;

public class ALSASound implements SoundInterface
{
	private final AL openAL ;
	private final int[] buffer ;

	public ALSASound( final int[] _buffer, final AL _openAL )
	{
		openAL = _openAL ;
		buffer = _buffer ;
	}

	public int[] getBufferID()
	{
		return buffer ;
	}

	public void destroy()
	{
		openAL.alDeleteBuffers( 1, buffer, 0 ) ;
	}

	public AL getOpenAL()
	{
		return openAL ;
	}
}