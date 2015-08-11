package com.linxonline.mallet.resources.android ;

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

	public void destroy() {}
}