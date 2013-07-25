package com.linxonline.mallet.util ;

/**
	Provides relative information about the state of a source.
	This callback interface is used in the Animation and Audio systems
	to provide running commentary on active sources.
	
	This callback should not be used to do any specfic logic processing. 
	It should at most set a few flags or send an Event. Logic processing 
	should be done during an Entity's or the Game States update cycle.
*/
public interface SourceCallback
{
	public void recieveID( final int _id ) ;		// Passes source ID
	public void callbackRemoved() ;				// Informs that callback has been removed from source

	public void start() ;						// Informs that source has started playing - via request
	public void pause() ;						// Informs that source has been paused - via request
	public void stop() ;							// Informs that source has been stopped - via request

	public void update( final float _dt ) ;		// Informs what position source is at of playing
	public void finished() ;						// Informs that source has finished - naturally ending
}