package com.linxonline.mallet.audio.web ;

import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.resources.sound.* ;

public class AudioSourceGenerator implements AudioGenerator<WebSound>
{
	/**
		Generator deals with the construction and deconstruction of the Audio backend 
	**/
	public boolean startGenerator()
	{
		return false ;
	}

	public boolean shutdownGenerator()
	{
		return true ;
	}

	/**
		Creates an AudioSource which can be used to manipulate a Sound buffer.
		An AudioSource can be created multiple times and use the same 
		Sound buffer.
	**/
	public AudioSource createAudioSource( final String _file, final StreamType _type )
	{
		return null ;
	}

	public void clean()
	{
	
	}

	public void clear()
	{
	
	}
}