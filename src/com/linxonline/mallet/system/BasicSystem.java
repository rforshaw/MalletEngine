package com.linxonline.mallet.system ;

import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.system.* ;

public abstract class BasicSystem implements SystemInterface
{
	protected String title = "Mallet Engine" ;
	protected ShutdownDelegate shutdownDelegate ;

	protected RenderInterface renderer ;
	protected AudioGenerator audioGenerator ;

	protected EventSystemInterface eventSystem ;
	protected InputSystemInterface inputSystem ;

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

	/*AUDIO SOURCE GENERATOR*/
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