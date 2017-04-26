package com.linxonline.mallet.renderer ;

/**
	Implements common requirements for using World.
	Basic World manages draw object state and Camera state.
*/
public class BasicWorld<D extends DrawData, C extends CameraData> implements World
{
	private final String id ;
	private final int order ;
	private final DrawState<D> state = new DrawState<D>() ;				// Objects to be drawn
	private final CameraState<C> cameras = new CameraState<C>() ;		// Camera view portals

	public BasicWorld( final String _id, final int _order )
	{
		id = _id ;
		order = _order ;
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

	public void addDraw( final D _data )
	{
		state.add( _data ) ;
	}

	public void removeDraw( final D _data )
	{
		state.remove( _data ) ;
	}

	public void addCamera( final C _camera )
	{
		cameras.add( _camera ) ;
	}

	public void removeCamera( final C _camera )
	{
		cameras.remove( _camera ) ;
	}

	/**
		Update the draw objects position, rotation, and scale.
		This will eventually call DrawData.upload
	*/
	public void update( final int _diff, final int _iteration )
	{
		state.update( _diff, _iteration ) ;
		cameras.update( _diff, _iteration ) ;
	}

	/**
		Update the cameras position, rotation, and scale.
		Call the cameras custom draw interface to begin 
		rendering to the framebuffer.
	*/
	public void draw()
	{
		cameras.draw() ;
	}

	public DrawState<D> getDrawState()
	{
		return state ;
	}

	public CameraState<C> getCameraState()
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

	public String toString()
	{
		return id ;
	}
}
