package com.linxonline.mallet.resources.sound ;

/**
	Implement Sound Interface for storing different 
	types of audio-buffer and for use with different 
	sound API's.
*/
public interface SoundInterface<T>
{
	public void destroy() ;			// Allows the audio-buffer/stream to be destroyed
}