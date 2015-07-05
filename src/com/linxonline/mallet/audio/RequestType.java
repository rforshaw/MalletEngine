package com.linxonline.mallet.audio ;

public enum RequestType
{
	CREATE_AUDIO,				// Create an Audio instance.
	MODIFY_EXISTING_AUDIO,		// Modify an existing Audio instance.
	REMOVE_AUDIO,				// Remove an Audio instance.
	GARBAGE_COLLECT_AUDIO		// Cleanup resources used by the Audio System.
}