package com.linxonline.mallet.audio.web ;

import com.linxonline.mallet.audio.* ;

public class WebAudioSource implements AudioSource
{
	public WebAudioSource() {}

	public void play() {}
	public void pause() {}
	public void playLoop() {}
	public void stop() {}

	public boolean isPlaying()
	{
		return false ;
	}

	public float getCurrentTime()
	{
		return 0.0f ;
	}

	public float getDuration()
	{
		return 0.0f ;
	}

	public void destroySource() {}
}