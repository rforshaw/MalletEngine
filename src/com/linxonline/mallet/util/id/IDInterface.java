package com.linxonline.mallet.util.id ;

public interface IDInterface
{
	/**
		Callback for the Object that made the Sound request.
		Returns the AudioSource ID, allowing the Object to make 
		further requests on it to be modified while it's playing.
	**/
	public void recievedID( final int _id ) ;
}