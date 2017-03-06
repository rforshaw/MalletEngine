package com.linxonline.mallet.resources ;

import com.linxonline.mallet.util.time.ElapsedTimer ;

/**
	Resource is the root class that all resource types 
	should extend from.
	
	It uses a simple Reference Counting system to determine 
	how many objects are using the Resource currently.
	
	A resource should be handled my an engine sub-system and 
	should not be directly handled by game-logic.
**/
public abstract class Resource
{
	private long creationTime = 0L ;			// When was the resource created

	protected Resource()
	{
		creationTime = ElapsedTimer.getTotalElapsedTimeInSeconds() ;
	}

	/**
		How long has the Resource existed
	**/
	public final long getLivingTime()
	{
		return ElapsedTimer.getTotalElapsedTimeInSeconds() - creationTime ;
	}

	/**
		When was the Resource created.
	**/
	public final long getCreationTime()
	{
		return creationTime ;
	}

	public abstract long getMemoryConsumption() ;
	
	/**
		If a Resource is hooked to an underlying system, for example the 
		buffer in an ALSASound, then use destroy to unhook it.
		
		Or if the resource stores other resources, call their 
		respective, unregister().
	**/
	public void destroy() {}

	/**
		Returns a String that denotes the type of resource
		ie: SOUND, TEXTURE, MODEL.
	**/
	public String type()
	{
		return "RESOURCE" ;
	}
}
