package com.linxonline.mallet.renderer ;

public interface IRender
{
	/**
		Called on application initialisation by the SystemInterface
		implementation for the defined platform.
	*/
	public void start() ;
	public void shutdown() ;

	/**
		Defines the window dimensions that the framebuffer will displayed to.
		The framebuffer and display do not have to be the same dimensions.
		The framebuffer will be upscaled or downscaled accordingly.
	*/
	public void setDisplayDimensions( final int _width, final int _height ) ;

	/**
		Update the current render state - this operates at 
		the currently active GameState update rate.
	*/
	public void updateState( final float _dt ) ;

	/**
		Draw to the render buffer the current render state.
		Most implementations should handle some form of 
		interpolation between frames.
	*/
	public void draw( final float _dt ) ;

	public void sort() ;
	public void clear() ;
	public void clean() ;
}
