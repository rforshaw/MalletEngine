package com.linxonline.mallet.renderer.android.GL ;

import android.content.Context ;
import android.opengl.GLSurfaceView ;

import com.linxonline.mallet.util.notification.Notification ;

public class GL2DSurfaceView extends GLSurfaceView
{
	public final GL2DRenderer renderer ;

	public GL2DSurfaceView( Context _context, final Notification.Notify _notify )
	{
		super( _context ) ;
		setEGLContextClientVersion( 2 ) ;
		setEGLConfigChooser( 8, 8, 8, 8, 16, 8 ) ;

		//setPreserveEGLContextOnPause( true ) ;
		renderer = new GL2DRenderer( _notify ) ;

		setRenderer( renderer ) ;
		setRenderMode( GLSurfaceView.RENDERMODE_WHEN_DIRTY ) ;
	}
}