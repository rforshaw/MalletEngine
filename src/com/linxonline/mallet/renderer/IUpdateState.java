package com.linxonline.mallet.renderer ;

/**
	Define a series of render buffers
	that can be updated.
*/
public sealed interface IUpdateState permits
	ABuffer,
	Storage
{}
