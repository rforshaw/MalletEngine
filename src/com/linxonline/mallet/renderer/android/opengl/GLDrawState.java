package com.linxonline.mallet.renderer.android.opengl ;

import java.util.List ;

import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.renderer.BasicDraw ;

public final class GLDrawState
{
	private final BufferedList<GLDraw> state = new BufferedList<GLDraw>() ;
	private final GLGeometryUploader uploader ;

	public GLDrawState( final GLGeometryUploader _uploader )
	{
		uploader = _uploader ;
		state.setRemoveListener( new BufferedList.RemoveListener<GLDraw>()
		{
			@Override
			public void remove( final GLDraw _data )
			{
				uploader.remove( _data ) ;
			}
		} ) ;
	}

	public void update( final int _diff, final int _iteration )
	{
		state.update() ;
		final List<GLDraw> current = state.getCurrentData() ;
		
		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			final GLDraw draw = current.get( i ) ;
			final BasicDraw<GLProgram> basic = draw.getBasicDraw() ;

			// Check to see if the object has been flagged to change.
			if( basic.toUpdate() == true )
			{
				// If it has then we update, if it hasn't we don't update!
				basic.update( _diff, _iteration ) ;
				upload( draw ) ;
			}
		}
	}

	private void upload( final GLDraw _data )
	{
		if( GLRenderer.loadProgram( _data ) == false )
		{
			return ;
		}

		uploader.upload( _data ) ;
	}

	public List<GLDraw> getActiveDraws()
	{
		state.update() ;
		return state.getCurrentData() ;
	}

	public void add( final GLDraw _draw )
	{
		final BasicDraw basic = _draw.getBasicDraw() ;
		// The objects current position, rotation, and scale 
		// may not be what's set for its future. Update the to ensure 
		// it's current state is the latest/future.
		basic.update( 1, 1 ) ;
		state.insert( _draw, basic.getOrder() ) ;
	}

	public void addAll( final List<GLDraw> _draws )
	{
		for( final GLDraw draw : _draws )
		{
			final BasicDraw basic = draw.getBasicDraw() ;
			// The objects current position, rotation, and scale 
			// may not be what's set for its future. Update the to ensure 
			// it's current state is the latest/future.
			basic.update( 1, 1 ) ;
			state.insert( draw, basic.getOrder() ) ;
		}
	}

	public void remove( final GLDraw _draw )
	{
		state.remove( _draw ) ;
	}

	public void sort() {}

	public void clear() {}
}
