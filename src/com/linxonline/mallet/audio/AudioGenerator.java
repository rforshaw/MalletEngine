package com.linxonline.mallet.audio ;

import com.linxonline.mallet.resources.sound.* ;

public interface AudioGenerator<T extends SoundInterface>
{
	/**
		Generator deals with the construction and deconstruction of the Audio backend 
	**/
	public boolean startGenerator() ;
	public boolean shutdownGenerator() ;

	/**
		Creates an AudioSource which can be used to manipulate a Sound buffer.
		An AudioSource can be created multiple times and use the same 
		Sound buffer.
	**/
	public AudioSource createAudioSource( final String _file, final StreamType _type ) ;

	public void clean() ;

	public void clear() ;
}