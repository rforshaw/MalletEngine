package com.linxonline.mallet.audio ;

import java.util.Set ;

public interface AudioGenerator
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

	public void clean( final Set<String> _activeKeys ) ;

	public void clear() ;
}
