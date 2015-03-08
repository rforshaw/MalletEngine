package com.linxonline.mallet.renderer.android.GL ;

import android.content.Context ;
import android.opengl.GLSurfaceView ;

public class GL2DSurfaceView extends GLSurfaceView
{
	public final GL2DRenderer renderer ;

	public GL2DSurfaceView( Context _context, final GL2DRenderer.ResumeInitialisation _resume )
	{
		super( _context ) ;
		setEGLContextClientVersion( 1 ) ;
		renderer = new GL2DRenderer( _resume ) ;

		setRenderer( renderer ) ;
		setRenderMode( GLSurfaceView.RENDERMODE_WHEN_DIRTY ) ;
	}

	public void draw( final float _dt )
	{
		renderer.draw( _dt ) ;
		requestRender() ;
	}
}