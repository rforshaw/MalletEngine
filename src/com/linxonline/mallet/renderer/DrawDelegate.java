package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.settings.Settings ;

/**
	DrawDelegate provides access for adding/removing Draw objects 
	from the rendering-system.

	On an Add or Remove request the Draw object is added to a queue, 
	and processed by the rendering-system at a later time.
*/
public interface DrawDelegate
{
	public void addTextDraw( final Draw _draw ) ;
	public void addBasicDraw( final Draw _draw ) ;
	public void removeDraw( final Draw _draw ) ;

	/**
		Inform the Renderer to stop accepting requests 
		from delegate and remove all previous requests
		from being rendered.
	*/
	public void shutdown() ;
}