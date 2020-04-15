package com.linxonline.mallet.renderer.opengl ;

import java.util.List ;

import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.renderer.DrawData ;

public final class DrawState<D extends DrawData>
{
	private final IUpload<D> UPLOAD_DEFAULT = new IUpload<D>()
	{
		@Override
		public void upload( final D _data )
		{
			Logger.println( "Failed to set upload interface for World.", Logger.Verbosity.MAJOR ) ;
		}
	} ;

	private final BufferedList<D> state = new BufferedList<D>() ;
	private final IUpload<D> upload ;

	public DrawState( final IUpload<D> _upload, final BufferedList.RemoveListener<D> _remove )
	{
		state.setRemoveListener( _remove ) ;
		upload = ( _upload == null ) ? UPLOAD_DEFAULT : _upload ;
	}

	public void update( final int _diff, final int _iteration )
	{
		state.update() ;
		final List<D> current = state.getCurrentData() ;

		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			final D draw = current.get( i ) ;
			draw.update( _diff, _iteration ) ;
			if( draw.toUpdate() == true )
			{
				upload.upload( draw ) ;
			}
		}
	}

	public List<D> getActiveDraws()
	{
		state.update() ;
		return state.getCurrentData() ;
	}

	public void add( final D _draw )
	{
		state.insert( _draw, _draw.getOrder() ) ;
	}
	
	public void addAll( final List<D> _draws )
	{
		for( final D draw : _draws )
		{
			state.insert( draw, draw.getOrder() ) ;
		}
	}

	public void remove( final D _draw )
	{
		state.remove( _draw ) ;
	}

	public void sort() {}

	public void clear() {}

	/**
		Extend the upload interface to allow the 
		Draw object to be uploaded to the World.

		This should either be constructed and set 
		within your World extension or by the core 
		renderer - though it depends on how you manage 
		your resources.

		For example GLRenderer stores its resources (shaders, 
		matrix cache, textures, GL, etc) in a central location,
		it make sense then to allow our IUpload to access this.

		When a World is created the GLRenderer WorldAssist sets 
		the interfaces accordingly.
	*/
	public interface IUpload<T extends DrawData>
	{
		public void upload( final T _data ) ;
	}
}
