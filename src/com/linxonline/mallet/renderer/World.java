package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.IntVector2 ;
import com.linxonline.mallet.util.notification.Notification.Notify ;

/**
	Simple Handler that identifies a World space 
	within the active rendering system.

	A World should render to its own framebuffer 
	and not interfere with other worlds.
*/
public interface World
{
	//public void setRenderDimensions( final int _x, final int _y, final int _width, final int _height ) ;
	public IntVector2 getRenderDimensions( final IntVector2 _fill ) ;

	//public void setDisplayDimensions( final int _x, final int _y, final int _width, final int _height ) ;
	public IntVector2 getDisplayDimensions( final IntVector2 _fill) ;

	public Notify<World> attachRenderNotify( final Notify<World> _notify ) ;
	public void dettachRenderNotify( final Notify<World> _notify ) ;

	public Notify<World> attachDisplayNotify( final Notify<World> _notify ) ;
	public void dettachDisplayNotify( final Notify<World> _notify ) ;
}
