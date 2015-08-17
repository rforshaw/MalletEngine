package com.linxonline.mallet.audio ;

public enum StreamType
{
	STATIC,			// Load the entire audio stream into a buffer before playing
					// Used for small audio files that will be used repeatedly.
					// For example: sound effects
	STREAM			// Load a chunk of the audio stream into a buffer before playing
					// Used for large audio files that will be used sparingly.
					// For example: background music
}
