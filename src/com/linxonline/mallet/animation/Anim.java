package com.linxonline.mallet.animation ;

/**
	Simple handler for modifying an Animation.
	Use AnimationAssist to modify the data-structure.
	
	AnimData extends Anim and is currently used by the 
	Animation-System, unless you know what you're doing 
	don't cast to AnimData, the Data-Structure could change 
	depending on what is being animated, images, skeletal 
	based, etc..
*/
public interface Anim<T extends Anim> {}
