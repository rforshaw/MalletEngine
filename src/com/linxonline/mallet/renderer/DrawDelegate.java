package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.settings.Settings ;

public interface DrawDelegate
{
	public void addTextDraw( final DrawData _draw ) ;
	public void addBasicDraw( final DrawData _draw ) ;
	public void removeDrawData( final DrawData _draw ) ;

	/**
		Inform the Renderer to start accept requests
		from the delegate.
	*/
	public void start() ;

	/**
		Inform the Renderer to stop accepting requests 
		from delegate and remove all previous requests
		from being rendered.
	*/
	public void shutdown() ;
}