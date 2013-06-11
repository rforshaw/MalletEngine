package com.linxonline.mallet.system ;

import com.linxonline.mallet.audio.SourceGenerator ;
import com.linxonline.mallet.input.InputHandler ;
import com.linxonline.mallet.event.EventHandler ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.renderer.RenderInterface ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

/*===========================================*/
// SystemInterface
// Used to hook up Engine to OS
// Central Location for Events, Input, 
// and Rendering
/*===========================================*/

public interface SystemInterface
{
	public void initSystem() ;
	public void startSystem() ;
	public void stopSystem() ;
	public void shutdownSystem() ;

	/*INPUT HOOK*/
	public void addInputHandler( final InputHandler _handler ) ;
	public void removeInputHandler( final InputHandler _handler ) ;

	/*EVENT HOOK*/
	public void addEvent( final Event _event ) ;
	public void addEventHandler( final EventHandler _handler ) ;
	public void removeEventHandler( final EventHandler _handler ) ;

	/*RENDER*/

	public void setCameraPosition( final Vector3 _camera ) ;
	public void setDisplayDimensions( final Vector2 _display ) ;
	public void setRenderDimensions( final Vector2 _display ) ;

	public RenderInterface getRenderInterface() ;

	/*AUDIO SOURCE GENERATOR*/
	public SourceGenerator getSourceGenerator() ;

	public void clear() ;
	public void clearInputs() ;
	public void clearEvents() ;

	public boolean update() ;
	public void draw() ;
}
