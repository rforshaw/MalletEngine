package com.linxonline.mallet.renderer.android.GL ;

import android.content.Context ;
import android.opengl.GLSurfaceView ;

import com.linxonline.mallet.util.notification.Notification ;

public class GL2DSurfaceView extends GLSurfaceView
{
	private final GL2DRenderer renderer ;

	public GL2DSurfaceView( final Context _context, final Notification.Notify _notify )
	{
		super( _context ) ;
		setEGLContextClientVersion( 3 ) ;
		setEGLConfigChooser( 8, 8, 8, 8, 16, 8 ) ;

		renderer = new GL2DRenderer( _notify ) ;
		setRenderer( renderer ) ;
		setRenderMode( GLSurfaceView.RENDERMODE_WHEN_DIRTY ) ;
	}

	public GL2DRenderer getRenderer()
	{
		return renderer ;
	}
}
