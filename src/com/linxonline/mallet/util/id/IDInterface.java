package com.linxonline.mallet.util.id ;

/**
	Some Systems used in the Mallet Engine require to 
	access other systems. For example the Animation System 
	uses the Rendering System.
	This interface provides enables the calling system to
	retrieve the required id to continue making requests 
	on a specfic Event.
*/
public interface IDInterface
{
	public void recievedID( final int _id ) ;
}