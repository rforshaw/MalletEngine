package com.linxonline.mallet.util ;

public interface SourceCallback
{
	public void recieveID( final int _id ) ;		// Passes source ID
	public void callbackRemoved() ;				// Informs that callback has been removed from source

	public void start() ;						// Informs that source has started playing
	public void pause() ;						// Informs that source has been paused
	public void stop() ;							// Informs that source has been stopped

	public void update( final float _dt ) ;		// Informs what position source is at of playing
	public void finished() ;						// Informs that source has finished
}