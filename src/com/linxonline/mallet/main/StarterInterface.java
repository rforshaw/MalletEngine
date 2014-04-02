package com.linxonline.mallet.main ;

import com.linxonline.mallet.game.GameSystem ;
import com.linxonline.mallet.game.GameLoader ;

import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.mallet.io.filesystem.FileSystem ;

public abstract class StarterInterface
{
	public abstract void init() ;

	protected abstract GameLoader getGameLoader() ;
	protected abstract boolean loadGame( final GameSystem _system, final GameLoader _loader ) ;
	
	protected abstract void loadFileSystem( final FileSystem _fileSystem ) ;
	protected abstract void loadConfig() ;

	protected abstract void setRenderSettings( final SystemInterface _system ) ;
}