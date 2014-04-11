package com.linxonline.mallet.resources ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.resources.* ;

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

	@Override
	protected AudioBuffer createResource( final String _file )
	{
		return generator.createAudioBuffer( _file ) ;
	}

	public void shutdown() {}
}