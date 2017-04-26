package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.util.ManagedArray ;

/**
	Implements common requirements when using BasicWorld.
	See desktop/android GLWorldState for an implementation.
	
	WorldState allows the multiple BasicWorlds to be added to a 
	rendering-system. A default world must be passed when WorldState is 
	constructed/extended.
*/
public abstract class WorldState<D extends DrawData,
								 C extends CameraData,
								 W extends BasicWorld<D, C>> extends ManagedArray<W>
{
	private W defaultWorld ;

	public WorldState() {}

	public void setDefault( final W _default )
	{
		defaultWorld = _default ;
		current.add( defaultWorld ) ;
	}

	/**
		Wrapper function around add, provides greater explanation 
		on what add is being used for.
	*/
	public void addWorld( final W _world )
	{
		add( _world ) ;
	}

	/**
		Wrapper function around remove, provides greater explanation 
		on what remove is being used for.
	*/
	public void removeWorld( final W _world )
	{
		remove( _world ) ;
	}

	/**
		Add the DrawData to the specified World.
		If no world is specified add it to the default world.
	*/
	public void addDraw( final D _draw, final W _world )
	{
		final W world = getWorld( _world ) ;
		synchronized( world )
		{
			world.addDraw( _draw ) ;
		}
	}

	/**
		Remove the DrawData to the specified World.
		If no world is specified remove it from the default world.
	*/
	public void removeDraw( final D _draw )
	{
		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			final W world = current.get( i ) ;
			world.removeDraw( _draw ) ;
		}
	}

	/**
		Add the CameraData to the specified World.
		If no world is specified add it to the default world.
	*/
	public void addCamera( final C _camera, final W _world )
	{
		final W world = getWorld( _world ) ;
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
	public void removeCamera( final C _camera )
	{
		final W world = ( W )_camera.getWorld() ;
		synchronized( world )
		{
			world.removeCamera( _camera ) ;
		}
	}

	public C getCamera( final String _id, final W _world )
	{
		final W world = getWorld( _world ) ;
		return ( C )world.getCameraState().getCamera( _id ) ;
	}

	public W getWorld( final W _world )
	{
		return _world == null ? defaultWorld : _world ;
	}

	public W getWorld( final String _id )
	{
		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			final W world = current.get( i ) ;
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
	protected void addNewData( final List<W> _toAdd )
	{
		if( _toAdd.isEmpty() == false )
		{
			final int size = _toAdd.size() ;
			for( int i = 0; i < size; i++ )
			{
				final W add = _toAdd.get( i ) ;
				insertNewDrawData( add ) ;
			}
			_toAdd.clear() ;
		}
	}

	private void insertNewDrawData( final W _insert )
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
