package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;

/**
	Implements common requirements when using BasicWorld.
	See desktop/android GLWorldState for an implementation.
*/
public abstract class WorldState<T extends BasicWorld> extends State<T>
{
	private T defaultWorld ;

	public WorldState() {}

	public void setDefault( final T _default )
	{
		defaultWorld = _default ;
		current.add( defaultWorld ) ;
	}

	/**
		Add the DrawData to the specified World.
		If no world is specified add it to the default world.
	*/
	public void addDraw( final DrawData _draw, final T _world )
	{
		final BasicWorld world = getWorld( _world ) ;
		_draw.setWorld( world ) ;

		synchronized( world )
		{
			world.addDraw( _draw ) ;
		}
	}

	/**
		Remove the DrawData to the specified World.
		If no world is specified remove it from the default world.
	*/
	public void removeDraw( final DrawData _draw )
	{
		final BasicWorld world = ( BasicWorld )_draw.getWorld() ;
		synchronized( world )
		{
			world.removeDraw( _draw ) ;
		}
	}

	/**
		Add the CameraData to the specified World.
		If no world is specified add it to the default world.
	*/
	public void addCamera( final CameraData _camera, final T _world )
	{
		final BasicWorld world = getWorld( _world ) ;
		_camera.setWorld( world ) ;

		synchronized( world )
		{
			world.addCamera( _camera ) ;
		}
	}

	/**
		Remove the CameraData to the specified World.
		If no world is specified remove it from the default world.
	*/
	public void removeCamera( final CameraData _camera )
	{
		final BasicWorld world = ( BasicWorld )_camera.getWorld() ;
		synchronized( world )
		{
			world.removeCamera( _camera ) ;
		}
	}

	public CameraData getCamera( final String _id, final T _world )
	{
		final BasicWorld world = getWorld( _world ) ;
		return ( CameraData )world.getCameraState().getCamera( _id ) ;
	}

	public BasicWorld getWorld( final BasicWorld _world )
	{
		return _world == null ? defaultWorld : _world ;
	}

	public BasicWorld getWorld( final String _id )
	{
		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			final T world = current.get( i ) ;
			if( _id.equals( world.getID() ) == true )
			{
				return world ;
			}
		}

		return null ;
	}

	/**
		Sort the worlds based on their order.
		Inform the worlds to also update their draw 
		and camera states.
	*/
	public void sort() 
	{
		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			current.get( i ).sort() ;
		}
	}

	public void clear()
	{
		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			current.get( i ).clear() ;
		}
	}

	@Override
	protected void addNewData( final ArrayList<T> _toAdd )
	{
		for( final T add : _toAdd )
		{
			insertNewDrawData( add ) ;
		}
		_toAdd.clear() ;
	}

	private void insertNewDrawData( final T _insert )
	{
		final int order = _insert.getOrder() ;
		final int size = current.size() ;
		if( order < size )
		{
			current.add( order, _insert ) ;
			return ;
		}

		current.add( _insert ) ;
	}
}