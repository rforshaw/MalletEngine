package com.linxonline.mallet.system ;

import com.linxonline.mallet.audio.SourceGenerator ;
import com.linxonline.mallet.input.InputHandler ;
import com.linxonline.mallet.event.EventHandler ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.renderer.RenderInterface ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

/**
	To initialise low-level/Operating specific systems that the 
	game requires.
	It's responsibility entails creating a window, hooking-up 
	the input-system, initialising the rendering system, and 
	initialising the audio-system.
	It also handles the responsibility of creating O/S specific 
	locks that the game requires.
*/
public interface SystemInterface
{
	public void initSystem() ;		// Intialise systems
	public void startSystem() ;		// Run the systems if required
	public void stopSystem() ;		// Stop any independent systems - threaded for instance
	public void shutdownSystem() ;	// Shutdown systems and begin the clean-up job

	/**
		INPUT HOOK - convience methods.
		The root Input-system, typically one state will be hooked.
	*/
	public void addInputHandler( final InputHandler _handler ) ;
	public void removeInputHandler( final InputHandler _handler ) ;

	/**
		EVENT HOOK - convience methods.
		The root Event-system, typically one state will be hooked.
		Allows the state to be informed of external events. For 
		example: shutdown, minimise, layout change requests that 
		the state may be interested in.
		It also enables the state to make O/S specific requests 
		that only work on certain implementations. For example:
		Displaying virtual-keyboard, opening a web browser, etc.
	*/
	public void addEvent( final Event _event ) ;
	public void addEventHandler( final EventHandler _handler ) ;
	public void removeEventHandler( final EventHandler _handler ) ;

	/**
		RENDER - convience methods.
		The root renderer, typically one state should render at a time.
	*/
	public void setCameraPosition( final Vector3 _camera ) ;
	public void setDisplayDimensions( final Vector2 _display ) ;
	public void setRenderDimensions( final Vector2 _display ) ;

	public RenderInterface getRenderInterface() ;

	/*AUDIO SOURCE GENERATOR*/
	public SourceGenerator getSourceGenerator() ;

	public boolean update() ;
	public void draw( final float _dt ) ;
}
