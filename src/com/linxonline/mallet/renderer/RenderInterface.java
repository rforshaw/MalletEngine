package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.event.EventHandler ;
import com.linxonline.mallet.event.EventController ;
import com.linxonline.mallet.maths.Vector3 ;

public interface RenderInterface
{
	public void start() ;
	public void shutdown() ;

	public void initAssist() ;					// Ensure the Font/Texture Assist is correctly implemented

	public void setRenderDimensions( final int _width, final int _height ) ;
	public void setDisplayDimensions( final int _width, final int _height ) ;

	public void setCameraPosition( final Vector3 _position ) ;

	public void updateState( final float _dt ) ;
	public void draw( final float _dt ) ;			// Draw the current Render State

	public RenderInfo getRenderInfo() ;
	public EventController getEventController() ;

	public void sort() ;
	public void clear() ;
	public void clean() ;
}