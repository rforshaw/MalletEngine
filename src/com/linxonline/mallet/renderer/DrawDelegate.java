package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.settings.Settings ;

/**
	DrawDelegate provides access for adding/removing Draw objects 
	from the rendering-system.

	On an Add or Remove request the Draw object is added to a queue, 
	and processed by the rendering-system at a later time.

	Draw/Camera objects are added to a World, the user can specify 
	the World in which these object are added. If no World is specified 
	then they are added to the default world.

	DrawDelegate can also add Worlds to the rendering-system.
*/
public interface DrawDelegate
{
	/**
		Added to the default world.
		A Draw object should not be added to more than one World.
	*/
	public void addTextDraw( final Draw _draw ) ;
	public void addBasicDraw( final Draw _draw ) ;

	/**
		Added to the default world.
		A Camera object should not be added to more than one World.
	*/
	public void addCamera( final Camera _camera ) ;
	public void removeCamera( final Camera _camera ) ;

	/**
		Add Draw to a specific world.
		A Draw object should not be added to more than one World.
	*/
	public void addTextDraw( final Draw _draw, final World _world ) ;
	public void addBasicDraw( final Draw _draw, final World _world ) ;
	public void removeDraw( final Draw _draw ) ;

	/**
		Add Camera to a specific world.
		A Camera object should not be added to more than one World.
	*/
	public void addCamera( final Camera _camera, final World _world ) ;
	public void removeCamera( final Camera _camera, final World _world ) ;

	/**
		Add a new world to the rendering-system.
		Only create/add a world if you wish to render content separate
		from the default world.
	*/
	public void addWorld( final World _world ) ;
	public void removeWorld( final World _world ) ;

	/**
		Return the Camera associated with the unique _id.
		If World is not specified the camera is assumed to 
		be in the default world.
	*/
	public Camera getCamera( final String _id, final World _world ) ;

	/**
		Return the World associated with the unique _id.
	*/
	public World getWorld( final String _id ) ;

	/**
		Inform the Renderer to stop accepting requests 
		from delegate and remove all previous requests
		from being rendered.
	*/
	public void shutdown() ;
}