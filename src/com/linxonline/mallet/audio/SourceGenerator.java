package com.linxonline.mallet.audio ;

import com.linxonline.mallet.resources.sound.* ;

public interface SourceGenerator
{
	/**
		Generator deals with the construction and deconstruction of the Audio backend 
	**/
	public boolean startGenerator() ;
	public boolean shutdownGenerator() ;

	/**
		Creates an AudioSource which can be to manipulate a Sound buffer.
		An AudioSource can be created multiple times and use the same 
		Sound buffer.
	**/
	public AudioSource createAudioSource( final Sound _sound ) ;
}