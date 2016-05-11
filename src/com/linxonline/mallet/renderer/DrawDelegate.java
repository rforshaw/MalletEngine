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
	/**
		Add Draw to the default world.
	*/
	public void addTextDraw( final Draw _draw ) ;
	public void addBasicDraw( final Draw _draw ) ;

	public void addCamera( final Camera _camera ) ;
	public void removeCamera( final Camera _camera ) ;

	/**
		Add Draw to a specific world.
	*/
	public void addTextDraw( final Draw _draw, final World _world ) ;
	public void addBasicDraw( final Draw _draw, final World _world ) ;
	public void removeDraw( final Draw _draw ) ;

	public void addCamera( final Camera _camera, final World _world ) ;
	public void removeCamera( final Camera _camera, final World _world ) ;

	public void addWorld( final World _world ) ;
	public void removeWorld( final World _world ) ;

	public Camera getCamera( final String _id, final World _world ) ;
	public World getWorld( final String _id ) ;

	/**
		Inform the Renderer to stop accepting requests 
		from delegate and remove all previous requests
		from being rendered.
	*/
	public void shutdown() ;
}