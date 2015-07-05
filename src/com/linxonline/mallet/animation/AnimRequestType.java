package com.linxonline.mallet.animation ;

public enum AnimRequestType
{
	CREATE_ANIMATION,				// Create a new Animation.
	MODIFY_EXISTING_ANIMATION,	// Modify an existing Animation: play, pause, stop.
	REMOVE_ANIMATION,				// Remove the Animation from running.
	GARBAGE_COLLECT_ANIMATION 	// Clean-up resources and animations that are not being used anymore.
}
