package com.linxonline.mallet.animation ;

public class AnimRequestType
{
	public static final int CREATE_ANIMATION = 10 ;				// Create a new Animation.
	public static final int MODIFY_EXISTING_ANIMATION = 20 ;	// Modify an existing Animation: play, pause, stop.
	public static final int REMOVE_ANIMATION = 30 ;				// Remove the Animation from running.
	public static final int GARBAGE_COLLECT_ANIMATION = 40 ; 	// Clean-up resources and animations that are not being used anymore.
}
