package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.event.EventHandler ;
import com.linxonline.mallet.maths.Vector3 ;

public interface RenderInterface extends EventHandler
{
	public void setRenderDimensions( final int _width, final int _height ) ;
	public void setDisplayDimensions( final int _width, final int _height ) ;
	public void setCameraPosition( final Vector3 _position ) ;

	public void draw() ;

	public void sort() ;
	public void clear() ;
}