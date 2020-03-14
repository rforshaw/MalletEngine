package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.util.Logger ;

/**
	Implements common requirements when using BasicWorld.
	See desktop/android GLWorldState for an implementation.
	
	WorldState allows the multiple BasicWorlds to be added to a 
	rendering-system. A default world must be passed when WorldState is 
	constructed/extended.
*/
public class WorldState<D extends DrawData,
						C extends CameraData,
						W extends BasicWorld<D, C>>
{
	private final BufferedList<W> worlds = new BufferedList<W>() ;
	private W defaultWorld ;

	public WorldState() {}

	public void setDefault( final W _default )
	{
		defaultWorld = _default ;
	}

	/**
		Wrapper function around add, provides greater explanation 
		on what add is being used for.
	*/
	public void addWorld( final W _world )
	{
		worlds.insert( _world, _world.getOrder() ) ;
	}

	/**
		Wrapper function around remove, provides greater explanation 
		on what remove is being used for.
	*/
	public void removeWorld( final W _world )
	{
		worlds.remove( _world ) ;
	}

	/**
		Add the DrawData to the specified World.
		If no world is specified add it to the default world.
	*/
	public void addDraw( final D _draw, final W _world )
	{
		final W world = getWorld( _world ) ;
		world.addDraw( _draw ) ;
	}

	/**
		Remove the DrawData to the specified World.
		If no world is specified remove it from the default world.
	*/
	public void removeDraw( final D _draw )
	{
		final List<W> current = worlds.getCurrentData() ;
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
		world.addCamera( _camera ) ;
	}

	/**
		Remove the CameraData to the specified World.
		If no world is specified remove it from the default world.
	*/
	public void removeCamera( final C _camera )
	{
		final List<W> current = worlds.getCurrentData() ;
		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			final W world = current.get( i ) ;
			world.removeCamera( _camera ) ;
		}
	}

	public C getCamera( final String _id, final W _world )
	{
		final W world = getWorld( _world ) ;
		return world.getCamera( _id ) ;
	}

	public W getWorld( final W _world )
	{
		return _world == null ? defaultWorld : _world ;
	}

	public W getWorld( final String _id )
	{
		final List<W> current = worlds.getCurrentData() ;
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

	public BufferedList<W> getWorlds()
	{
		return worlds ;
	}

	/**
		Sort the worlds based on their order.
		Inform the worlds to also update their draw 
		and camera states.
	*/
	public void sort() 
	{
		final List<W> current = worlds.getCurrentData() ;
		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			current.get( i ).sort() ;
		}
	}

	public void clear()
	{
		final List<W> current = worlds.getCurrentData() ;
		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			current.get( i ).clear() ;
		}
	}
}
