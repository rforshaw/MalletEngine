package com.linxonline.mallet.renderer.texture ;

public interface ImageInterface
{
	/**
		Return the amount of memory allocated by 
		this image in bytes.
	*/
	public long getMemoryConsumption() ;
	
	public void destroy() ;
}
