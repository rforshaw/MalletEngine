package com.linxonline.mallet.renderer ;

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
public interface DrawDelegate<W extends World, D extends Draw>
{
	/**
		Added to the default world.
		A Draw object should not be added to more than one World.
	*/
	public void addTextDraw( final D _draw ) ;
	public void addBasicDraw( final D _draw ) ;

	/**
		Add Draw to a specific world.
		A Draw object should not be added to more than one World.
	*/
	public void addTextDraw( final D _draw, final W _world ) ;
	public void addBasicDraw( final D _draw, final W _world ) ;
	public void removeDraw( final D _draw ) ;

	/**
		Return the Camera associated with the unique _id.
		If World is not specified the camera is assumed to 
		be in the default world.
	*/
	public Camera getCamera( final String _id, final W _world ) ;

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
