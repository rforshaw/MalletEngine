package com.linxonline.mallet.util ;

/**
	Used by systems that can support multiple threads.
*/
public enum Threaded
{
	SINGLE,		// Use the currently active thread
	MULTI		// Split the system activities over multiple threads
}
