package com.linxonline.mallet.system ;

import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.system.* ;

import com.linxonline.mallet.io.filesystem.FileSystem ;

/**
	Basic implementation of the System Interface.
	Defines what must be needed by a System and implements 
	the boiler plate functions that are most consistent 
	between implementations.
	Look at GLDefaultSystem for an example on how to use 
	this class.
*/
public abstract class BasicSystem implements SystemInterface
{
	protected String title = "Mallet Engine" ;
	protected ShutdownDelegate shutdownDelegate ;

	protected RenderInterface renderer ;
	protected AudioGenerator audioGenerator ;

	protected EventSystemInterface eventSystem ;
	protected InputSystemInterface inputSystem ;
	protected FileSystem fileSystem ;

	public BasicSystem() {}

	@Override
	public abstract void initSystem() ;

	@Override
	public abstract void startSystem() ;

	@Override
	public abstract void stopSystem() ;

	@Override
	public void shutdownSystem()
	{
		audioGenerator.shutdownGenerator() ;
		renderer.shutdown() ;
		shutdownDelegate.shutdown() ;
	}

	@Override
	public FileSystem getFileSystem()
	{
		return fileSystem ;
	}

	@Override
	public ShutdownDelegate getShutdownDelegate()
	{
		return shutdownDelegate ;
	}

	@Override
	public RenderInterface getRenderInterface()
	{
		return renderer ;
	}

	@Override
	public AudioGenerator getAudioGenerator()
	{
		return audioGenerator ;
	}

	@Override
	public InputSystemInterface getInputInterface()
	{
		return inputSystem ;
	}

	@Override
	public EventSystemInterface getEventInterface()
	{
		return eventSystem ;
	}

	@Override
	public void sleep( final long _millis )
	{
		try
		{
			Thread.sleep( _millis ) ;
			Thread.yield() ;
		}
		catch( InterruptedException ex )
		{
			Thread.currentThread().interrupt() ;
			//ex.printStackTrace() ;
		}
	}

	@Override
	public boolean update( final float _dt )
	{
		renderer.updateState( _dt ) ;
		inputSystem.update() ;
		eventSystem.update() ;		// Pass the Events to the interested Backend Systems
		return true ;
	}

	@Override
	public void draw( final float _dt )
	{
		renderer.draw( _dt ) ;
	}
}
