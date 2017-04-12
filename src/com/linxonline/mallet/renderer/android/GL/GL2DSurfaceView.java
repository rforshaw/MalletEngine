package com.linxonline.mallet.renderer.android.GL ;

import android.content.Context ;
import android.opengl.GLSurfaceView ;

import com.linxonline.mallet.util.notification.Notification ;

public class GL2DSurfaceView extends GLSurfaceView
{
	public GL2DSurfaceView( final Context _context, final GL2DRenderer _renderer )
	{
		super( _context ) ;
		setEGLContextClientVersion( 3 ) ;
		setEGLConfigChooser( 8, 8, 8, 8, 16, 8 ) ;

		setRenderer( _renderer ) ;
		setRenderMode( GLSurfaceView.RENDERMODE_WHEN_DIRTY ) ;
	}
}
