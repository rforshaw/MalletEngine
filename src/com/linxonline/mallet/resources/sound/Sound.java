package com.linxonline.mallet.resources.sound ;

import com.linxonline.mallet.resources.Resource ;

/**
	Sound has the ability to play the Resource it contains.
	This should be considered deprecated and not used.
**/
public final class Sound extends Resource
{
	private static final String type = "SOUND" ;

	private final SoundInterface buffer ;

	public Sound( SoundInterface _buffer )
	{
		buffer = _buffer ;
	}

	public <T> T getSoundBuffer( final Class<T> _type )
	{
		return _type.cast( buffer ) ;
	}

	@Override
	public void destroy()
	{
		buffer.destroy() ;
	}

	@Override
	public String type()
	{
		return type ;
	}
}