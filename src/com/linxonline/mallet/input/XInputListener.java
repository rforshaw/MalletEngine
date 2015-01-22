package com.linxonline.mallet.input ;

public interface XInputListener
{
	// A, B, X, Y, R1, R2, START, & SELECT
	public void keyPressed( final XInputDevice.Event _event ) ;
	public void keyReleased( final XInputDevice.Event _event ) ;

	// D-Pad, Joysticks, L2, R2
	public void analogue( final XInputDevice.Event _event ) ;
	
	public void start() ;		
	public void end() ;
}