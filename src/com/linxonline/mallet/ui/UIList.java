package com.linxonline.mallet.ui ;

import com.linxonline.mallet.renderer.CameraAssist ;
import com.linxonline.mallet.renderer.Camera ;
import com.linxonline.mallet.renderer.WorldAssist ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.audio.AudioDelegate ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

public class UIList extends UILayout
{
	private final Camera camera ;

	public UIList( final Type _type, final float _length )
	{
		super( _type ) ;
		switch( _type )
		{
			case HORIZONTAL :
			{
				setMaximumLength( 0.0f, _length, 0.0f ) ;
				break ;
			}
			case VERTICAL :
			default       :
			{
				setMaximumLength( _length, 0.0f, 0.0f ) ;
				break ;
			}
		}

		final Vector3 position = new Vector3( getPosition() ) ;
		final Vector3 rotation = new Vector3() ;
		final Vector3 scale = new Vector3( 1, 1, 1 ) ;
		camera = CameraAssist.createCamera( "UILIST", position, rotation, scale ) ;

		setWorld( WorldAssist.constructWorld( "UILIST", 1 ) ) ;
		CameraAssist.addCamera( camera, getWorld() ) ;
	}

	@Override
	public void refresh()
	{
		super.refresh() ;

		final Vector3 position = getPosition() ;
		final Vector3 offset = getOffset() ;
		final Vector3 length = getLength() ;

		final int x = ( int )( position.x + offset.x ) ;
		final int y = ( int )( position.y + offset.y ) ;

		final int width = ( int )length.x ;
		final int height = ( int )length.y ;

		CameraAssist.amendOrthographic( camera, 0.0f, height, 0.0f, width, -1000.0f, 1000.0f ) ;
		CameraAssist.amendScreenResolution( camera, width, height ) ;
		CameraAssist.amendScreenOffset( camera, x, y ) ;
	}

	@Override
	public void shutdown()
	{
		super.shutdown() ;
		WorldAssist.removeWorld( getWorld() ) ;
	}
}
