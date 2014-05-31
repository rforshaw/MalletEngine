package com.linxonline.mallet.resources.android ;

import android.media.* ;

import com.linxonline.mallet.resources.sound.* ;

public class AndroidSound implements SoundInterface
{
	private AudioTrack track = null ;
	private int length = 0 ;
	private boolean initialPlay = true ;

	public AndroidSound( final AudioTrack _track, final int _length )
	{
		track = _track ;
		length = _length ;
	}

	public AudioTrack getAudioTrack()
	{
		return track ;
	}
	
	/*public void play()
	{
		if( initialPlay == false )
		{
			track.stop() ;
			track.reloadStaticData() ;
		}
		else
		{
			initialPlay = false ;
		}

		// Skip the first 44 bytes for WAV header
		track.setPlaybackHeadPosition( 44 ) ;
		track.play() ;
	}

	public void playLoop() {}

	public void pause() {}

	public void stop()
	{
		track.stop() ;
	}*/

	public void destroy()
	{
		track.stop() ;
		track.flush() ;
		track.release() ;
	}
}