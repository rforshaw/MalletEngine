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
public abstract class BasicSystem<F extends FileSystem,
								  S extends SystemInterface.ShutdownDelegate,
								  R extends RenderInterface,
								  A extends AudioGenerator,
								  I extends InputSystemInterface,
								  E extends EventSystemInterface> implements SystemInterface<F, S, R, A, I, E>
{
	protected String title = "Mallet Engine" ;
	private final S shutdownDelegate ;

	private final R renderer ;
	private final A audioGenerator ;

	private final E eventSystem ;
	private final I inputSystem ;
	private final F fileSystem ;

	public BasicSystem( final S _shutdown,
						final R _renderer,
						final A _audio,
						final E _event,
						final I _input,
						final F _fileSystem )
	{
		shutdownDelegate = _shutdown ;
		renderer = _renderer ;
		audioGenerator = _audio ;
		eventSystem = _event ;
		inputSystem = _input ;
		fileSystem = _fileSystem ;
	}

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
	public F getFileSystem()
	{
		return fileSystem ;
	}

	@Override
	public S getShutdownDelegate()
	{
		return shutdownDelegate ;
	}

	@Override
	public R getRenderer()
	{
		return renderer ;
	}

	@Override
	public A getAudioGenerator()
	{
		return audioGenerator ;
	}

	@Override
	public I getInput()
	{
		return inputSystem ;
	}

	@Override
	public E getEventSystem()
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
