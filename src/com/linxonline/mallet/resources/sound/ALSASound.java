package com.linxonline.mallet.resources.sound ;

import com.jogamp.openal.* ;

public class ALSASound implements SoundInterface
{
	private AL openAL = null ;
	public int[] source = null ;
	public int[] buffer = null ;

	public ALSASound() {}

	public void setOpenAL( final AL _openAL )
	{
		openAL = _openAL ;
	}

	public void play()
	{
		if( openAL != null && source != null )
		{
			openAL.alSourcePlay( source[0] ) ;
		}
	}

	public void playLoop()
	{
		if( openAL != null && source != null )
		{
			openAL.alSourcei( source[0], AL.AL_LOOPING,  AL.AL_TRUE ) ;
			openAL.alSourcePlay( source[0] ) ;
		}
	}

	public void pause()
	{
		openAL.alSourcePause( source[0] ) ;
	}

	public void stop()
	{
		openAL.alSourceStop( source[0] ) ;
	}

	public void destroy()
	{
		stop() ;
		openAL.alSourcei( source[0], AL.AL_BUFFER,  AL.AL_NONE ) ;

		openAL.alDeleteSources( 1, source, 0 ) ;
		openAL.alDeleteBuffers( 1, buffer, 0 ) ;
	}

	public AL getOpenAL()
	{
		return openAL ;
	}
}