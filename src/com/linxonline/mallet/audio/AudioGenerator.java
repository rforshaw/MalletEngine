package com.linxonline.mallet.audio ;

import com.linxonline.mallet.resources.* ;

public interface AudioGenerator<T extends SoundInterface>
{
	/**
		Generator deals with the construction and deconstruction of the Audio backend 
	**/
	public boolean startGenerator() ;
	public boolean shutdownGenerator() ;

	public AudioBuffer<T> createAudioBuffer( final String _file ) ;

	/**
		Creates an AudioSource which can be to manipulate a Sound buffer.
		An AudioSource can be created multiple times and use the same 
		Sound buffer.
	**/
	public AudioSource createAudioSource( final AudioBuffer<T> _sound ) ;
}