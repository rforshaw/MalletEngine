package com.linxonline.mallet.audio ;

import java.util.Set ;

public interface IGenerator
{
	/**
		Generator deals with the construction and deconstruction of the Audio backend 
	**/
	public boolean start() ;
	public boolean shutdown() ;

	/**
		Creates an AudioSource which can be used to manipulate a Sound buffer.
		An AudioSource can be created multiple times and use the same 
		Sound buffer.
	**/
	public ISource create( final String _file, final StreamType _type ) ;

	public void setListenerPosition( final float _x, final float _y, final float _z ) ;
	
	public void clean( final Set<String> _activeKeys ) ;

	public void clear() ;
}
