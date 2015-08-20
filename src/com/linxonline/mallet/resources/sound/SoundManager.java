package com.linxonline.mallet.resources.sound ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.resources.* ;

import com.linxonline.mallet.util.settings.Settings ;

public class SoundManager extends AbstractManager<AudioBuffer>
{
	private AudioGenerator generator ;

	public SoundManager() {}

	public SoundManager( final AudioGenerator _generator )
	{
		setAudioGenerator( _generator ) ;
	}

	public void setAudioGenerator( final AudioGenerator _generator )
	{
		generator = _generator ;
	}

	public void shutdown() {}
}
