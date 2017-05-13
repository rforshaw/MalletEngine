package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.Set ;
import java.util.List ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.renderer.* ;

/**
	Provides the ability to manage multiple OpenGL 
	states as seperate worlds.
*/
public class GLWorldState extends WorldState<GLDrawData, CameraData, GLWorld>
{
	public GLWorldState() {}

	/**
		Iterate over each GLWorld and update/upload its GL state.
		GL state will be updated based on GLDrawData flagged 
		for updating.
	*/
	public void upload( final int _diff, final int _iteration )
	{
		initAddedWorlds() ;
		manageState() ;

		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			final GLWorld world = current.get( i ) ;
			world.update( _diff, _iteration ) ;
			world.draw() ;
		}
	}

	public void clean( final Set<String> _activeKeys )
	{
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
		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			current.get( i ).shutdown() ;
		}
	}

	/**
		Worlds that have just been added to the World state 
		have yet to be fully initialised.
		This is the first opportunity they have to initialise 
		any OpenGL context related configurations in a 
		thread safe manner.
	*/
	private void initAddedWorlds()
	{
		final List<GLWorld> init = getToAddData() ;
		if( init.isEmpty() == false )
		{
			final int size = init.size() ;
			for( int i = 0; i < size; i++ )
			{
				init.get( i ).init() ;
			}
		}
	}
}
