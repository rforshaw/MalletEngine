package com.linxonline.mallet.renderer.opengl ;

import java.util.List ;

import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.renderer.CameraData ;

public final class CameraState<C extends CameraData>
{
	private final IDraw<C> DRAW_DEFAULT = new IDraw<C>()
	{
		@Override
		public void draw( final C _data )
		{
			Logger.println( "Failed to set draw interface for World.", Logger.Verbosity.MAJOR ) ;
		}
	} ;

	private final BufferedList<C> state = new BufferedList<C>() ;
	private IDraw<C> draw = DRAW_DEFAULT ;

	/**
		Extend the draw interface to allow the 
		camera to render to the specified coordinates.

		This should either be constructed and set 
		within your World extension or by the core 
		renderer - though it depends on how you manage 
		your resources.

		For example GLRenderer stores its resources (shaders, 
		matrix cache, textures, GL, etc) in a central location,
		it make sense then to allow our IDraw to access this.

		When a World is created the GLRenderer WorldAssist sets 
		the interfaces accordingly.
	*/
	public CameraState( final IDraw<C> _draw )
	{
		draw = ( _draw == null ) ? DRAW_DEFAULT : _draw ;
	}

	public void add( final C _camera )
	{
		state.add( _camera ) ;
	}

	public void remove( final C _camera )
	{
		state.remove( _camera ) ;
	}

	public void setDisplayDimensions( final int _x, final int _y, final int _width, final int _height )
	{
		final List<C> current = state.getCurrentData() ;

		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			final CameraData camera = current.get( i ) ;
			CameraData.Screen.setScreen( camera.getDisplayScreen(), _x, _y, _width, _height ) ;
		}
	}
	
	public synchronized void update( final int _diff, final int _iteration )
	{
		state.update() ;
		final List<C> current = state.getCurrentData() ;

		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			current.get( i ).update( _diff, _iteration ) ;
		}
	}

	public synchronized void draw()
	{
		final List<C> current = state.getCurrentData() ;

		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			draw.draw( current.get( i ) ) ;
		}
	}

	public void sort() {}

	public void clear() {}

	public synchronized C getCamera( final String _id )
	{
		state.update() ;
		final List<C> current = state.getCurrentData() ;

		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			final C camera = current.get( i ) ;
			final String id = camera.getID() ;
			
			if( _id.equals( id ) == true )
			{
				return camera ;
			}
		}

		return null ;
	}

	public interface IDraw<T extends CameraData>
	{
		public void draw( final T _data ) ;
	}
}
