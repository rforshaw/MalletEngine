package com.linxonline.mallet.audio ;

public class RequestType
{
	public static final int CREATE_AUDIO = 10 ;				// Create an Audio instance.
	public static final int MODIFY_EXISTING_AUDIO = 20 ;	// Modify an existing Audio instance.
	public static final int REMOVE_AUDIO = 30 ;				// Remove an Audio instance.
	public static final int GARBAGE_COLLECT_AUDIO = 40 ;	// Cleanup resources used by the Audio System.
}