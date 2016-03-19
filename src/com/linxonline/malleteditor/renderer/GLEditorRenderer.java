package com.linxonline.malleteditor.renderer ;

import javax.media.opengl.* ;

import com.linxonline.mallet.renderer.desktop.GL.GLRenderer ;
import com.linxonline.mallet.maths.* ;

public class GLEditorRenderer extends GLRenderer
{
	public GLEditorRenderer() {}

	@Override
	public void reshape( GLAutoDrawable _drawable, int _x, int _y, int _width, int _height )
	{
		getRenderInfo().setDisplayDimensions( new Vector2( _width, _height ) ) ;
		getRenderInfo().setRenderDimensions( new Vector2( _width, _height ) ) ;
		resize() ;
	}
}