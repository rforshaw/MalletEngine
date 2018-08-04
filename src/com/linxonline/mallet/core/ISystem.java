package com.linxonline.mallet.core ;

import com.linxonline.mallet.audio.AudioGenerator ;
import com.linxonline.mallet.input.IInputSystem ;
import com.linxonline.mallet.event.IEventSystem ;
import com.linxonline.mallet.renderer.IRender ;
import com.linxonline.mallet.io.filesystem.FileSystem ;

/**
	To initialise low-level/Operating specific systems that the 
	game requires.
	It's responsibility entails creating a window, hooking-up 
	the input-system, initialising the rendering system, and 
	initialising the audio-system.
*/
public interface ISystem<F extends FileSystem,
						 S extends ISystem.ShutdownDelegate,
						 R extends IRender,
						 A extends AudioGenerator,
						 I extends IInputSystem,
						 E extends IEventSystem,
						 G extends IGameSystem>
{
	public void initSystem() ;		// Intialise systems
	public void startSystem() ;		// Run the systems if required
	public void stopSystem() ;		// Stop any independent systems - threaded for instance
	public void shutdownSystem() ;	// Shutdown systems and begin the clean-up job

	public F getFileSystem() ;

	/**
		It is impossible for the Game State to know when 
		the player has decided to close the application 
		externally.
		This allows the developer to register things that 
		should be shutdown correctly before the application 
		is forcefully closed.
	*/
	public S getShutdownDelegate() ;

	/**
		RENDER - convience methods.
		The root renderer, typically one state should render at a time.
	*/
	public R getRenderer() ;

	/**
		AUDIO GENERATOR
	*/
	public A getAudioGenerator() ;

	/**
		Return the main input system for the running application.
		The root Input-system, typically one state will be hooked.
	*/
	public I getInput() ;

	/**
		The root Event-system, typically one state will be hooked.
		Allows the state to be informed of external events. For 
		example: shutdown, minimise, layout change requests that 
		the state may be interested in.
		It also enables the state to make O/S specific requests 
		that only work on certain implementations. For example:
		Displaying virtual-keyboard, opening a web browser, etc.
	*/
	public E getEventSystem() ;

	public G getGameSystem() ;

	public boolean update( final float _dt ) ;		// Update the System

	public void sleep( final long _millis ) ;		// Cause the Thread the System is running on to sleep for the specified duration

	public void draw( final float _dt ) ;

	/**
		Implement this interface if DefaultShutdown is 
		too limited for your use case.
		Register things that need to be correctly dealt 
		with before the application has been closed. 
		This may include saving the current game-state 
		or clearing special resources.
	*/
	public static interface ShutdownDelegate
	{
		public void addShutdownCallback( final Callback _callback ) ;
		public void removeShutdownCallback( final Callback _callback ) ;

		public void shutdown() ;

		/**
			Implement this callback to handle the 
			specific task upon the application being shutdown.
		*/
		public static interface Callback
		{
			public void shutdown() ;
		}
	}
}
