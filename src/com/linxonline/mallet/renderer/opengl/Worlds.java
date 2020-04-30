package com.linxonline.mallet.renderer.opengl ;

import java.util.Set ;
import java.util.List ;

import com.linxonline.mallet.util.BufferedList ;

import com.linxonline.mallet.renderer.BasicWorld ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.CameraData ;
import com.linxonline.mallet.renderer.WorldState ;

public class Worlds<D extends Draw,
					C extends CameraData,
					W extends BasicWorld<D, C>> extends WorldState<D, C, W>
{
	public Worlds()
	{
		final BufferedList<W> worlds = getWorlds() ;
		worlds.setAddListener( new BufferedList.AddListener<W>()
		{
			public void add( final W _world )
			{
				// Worlds that have just been added to the World state 
				// have yet to be fully initialised.
				// This is the first opportunity they have to initialise 
				// any OpenGL context related configurations in a 
				// thread safe manner.
				_world.init() ;
			}
		} ) ;
	}

	/**
		Iterate over each W and update/upload its GL state.
		GL state will be updated based on GLDraw flagged 
		for updating.
	*/
	public void upload( final int _diff, final int _iteration )
	{
		final BufferedList<W> worlds = getWorlds() ;
		worlds.update() ;

		final List<W> current = worlds.getCurrentData() ;
		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			final W world = current.get( i ) ;
			world.update( _diff, _iteration ) ;
			world.draw() ;
		}
	}

	public void clean( final Set<String> _activeKeys )
	{
		final BufferedList<W> worlds = getWorlds() ;
		worlds.update() ;

		final List<W> current = worlds.getCurrentData() ;
		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			current.get( i ).clean( _activeKeys ) ;
		}
	}

	/**
		Shutdown each world and clean up the World/GL state.
	*/
	public void shutdown()
	{
		final BufferedList<W> worlds = getWorlds() ;
		worlds.update() ;

		final List<W> current = worlds.getCurrentData() ;
		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			current.get( i ).shutdown() ;
		}
	}
}
