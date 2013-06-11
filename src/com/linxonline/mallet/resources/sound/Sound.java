package com.linxonline.mallet.resources.sound ;

import com.linxonline.mallet.resources.Resource ;

/**
	Sound has the ability to play the Resource it contains.
	This should be considered deprecated and not used.
**/
public final class Sound extends Resource
{
	private static final String type = "SOUND" ;

	public SoundInterface sound = null ;

	public Sound( SoundInterface _sound )
	{
		sound = _sound ;
	}

	public final void play()
	{
		sound.play() ;
	}
	
	public final void playLoop()
	{
		sound.playLoop() ;
	}
	
	public final void pause()
	{
		sound.pause() ;
	}
	
	public final void stop()
	{
		sound.stop() ;
	}

	@Override
	public void destroy()
	{
		sound.destroy() ;
	}

	@Override
	public String type()
	{
		return type ;
	}
}