package com.linxonline.mallet.renderer ;

/**
	Implements common requirements for using World.
	Basic World manages draw object state and Camera state.
*/
public class BasicWorld<T extends DrawData, U extends CameraData> implements World
{
	private final String id ;
	private final int order ;
	private final DrawState state = new DrawState() ;			// Objects to be drawn
	private final CameraState cameras = new CameraState() ;		// Camera view portals

	public BasicWorld( final String _id, final int _order, DrawState.RemoveDelegate _remove )
	{
		id = _id ;
		order = _order ;
		state.setRemoveDelegate( _remove ) ;
	}

	/**
		Unique identifier to acquire the world via a DrawDelegate.
	*/
	public String getID()
	{
		return id ;
	}

	/**
		Used to inform the World State what order it should be called in.
	*/
	public int getOrder()
	{
		return order ;
	}

	public void addDraw( final T _data )
	{
		state.add( _data ) ;
	}

	public void removeDraw( final T _data )
	{
		state.remove( _data ) ;
	}

	public void addCamera( final U _camera )
	{
		cameras.add( _camera ) ;
	}

	public void removeCamera( final U _camera )
	{
		cameras.remove( _camera ) ;
	}

	/**
		Update the draw objects position, rotation, and scale.
	*/
	public void upload( final int _diff, final int _iteration )
	{
		state.upload( _diff, _iteration ) ;
	}

	/**
		Update the cameras position, rotation, and scale.
		Call the cameras custom draw interface to begin 
		rendering to the framebuffer.
	*/
	public void draw( final int _diff, final int _iteration )
	{
		cameras.draw( _diff, _iteration ) ;
	}

	public DrawState getDrawState()
	{
		return state ;
	}

	public CameraState getCameraState()
	{
		return cameras ;
	}

	public void sort()
	{
		state.sort() ;
	}

	public void clear()
	{
		state.clear() ;
	}
}