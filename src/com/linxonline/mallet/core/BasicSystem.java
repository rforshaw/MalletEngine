package com.linxonline.mallet.core ;

import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.io.filesystem.FileSystem ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

import com.linxonline.mallet.util.Logger ;

/**
	Basic implementation of the System Interface.
	Defines what must be needed by a System and implements 
	the boiler plate functions that are most consistent 
	between implementations.
	Look at GLDefaultSystem for an example on how to use 
	this class.
*/
public abstract class BasicSystem<F extends FileSystem,
								  S extends ISystem.ShutdownDelegate,
								  R extends IRender,
								  A extends IGenerator,
								  I extends IInputSystem,
								  E extends IEventSystem,
								  G extends IGameSystem> implements ISystem<F, S, R, A, I, E, G>
{
	protected final EventController controller = new EventController() ;

	private final S shutdownDelegate ;

	private final R renderer ;
	private final A audioGenerator ;

	private final E eventSystem ;
	private final I inputSystem ;
	private final F fileSystem ;

	private final G gameSystem ;

	public BasicSystem( final S _shutdown,
						final R _renderer,
						final A _audio,
						final E _event,
						final I _input,
						final F _fileSystem,
						final G _gameSystem )
	{
		shutdownDelegate = _shutdown ;
		renderer = _renderer ;
		audioGenerator = _audio ;
		eventSystem = _event ;
		inputSystem = _input ;
		fileSystem = _fileSystem ;
		gameSystem = _gameSystem ;

		initFileSystem() ;
	}

	private void initFileSystem()
	{
		Logger.println( "Finalising filesystem.", Logger.Verbosity.MINOR ) ;
		GlobalFileSystem.setFileSystem( fileSystem ) ;

		Logger.println( "Mapping Base directory.", Logger.Verbosity.MINOR ) ;
		if( GlobalFileSystem.mapDirectory( "base" ) == false )				// Map base-folder for faster access
		{
			Logger.println( "Failed to map base directory.", Logger.Verbosity.MINOR ) ;
		}

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
		shutdownDelegate.shutdown() ;
		audioGenerator.shutdown() ;
		renderer.shutdown() ;
	}

	@Override
	public final F getFileSystem()
	{
		return fileSystem ;
	}

	@Override
	public final S getShutdownDelegate()
	{
		return shutdownDelegate ;
	}

	@Override
	public final R getRenderer()
	{
		return renderer ;
	}

	@Override
	public final A getAudioGenerator()
	{
		return audioGenerator ;
	}

	@Override
	public final I getInput()
	{
		return inputSystem ;
	}

	@Override
	public final E getEventSystem()
	{
		return eventSystem ;
	}

	@Override
	public final G getGameSystem()
	{
		return gameSystem ;
	}

	@Override
	public final EventController getEventController()
	{
		return controller ;
	}
}
