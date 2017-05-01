package com.linxonline.mallet.io ;

import com.linxonline.mallet.util.time.ElapsedTimer ;

/**
	Resource is the root class that all resource types 
	should extend from.

	A resource should be handled my an engine sub-system and 
	should not be directly handled by game-logic.
**/
public abstract class Resource
{
	private final long creationTime ;			// When was the resource created

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

	/**
		Return the amount of memory allocated to 
		this resource.

		Returned size in bytes.
	*/
	public abstract long getMemoryConsumption() ;

	/**
		If a Resource is hooked to an underlying system, for example the 
		buffer in an ALSASound, then use destroy to unhook it.
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
