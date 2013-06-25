package com.linxonline.mallet.util ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.event.EventUpdater ;

/**
	Needs a better name before it is too late.
	Deals with generic updating & storage of sources, 
	allows the Audio, Animations systems to deal with 
	the important stuff.
**/
public abstract class SystemRoot<T> extends EventUpdater
{
	/**
		The HashMap and ArrayList should be replaced with a more structure 
		that provides effecient iteration & searching capabilites.
	**/
	protected final HashMap<Integer, T> sources = new HashMap<Integer, T>() ;
	protected final ArrayList<T> activeSources = new ArrayList<T>() ;
	protected final ArrayList<T> removeSources = new ArrayList<T>() ;

	public void update( final float _dt )
	{
		updateEvents() ;
		updateSources( _dt ) ;
		removeSources() ;
	}

	protected T getSource( final int _key )
	{
		return sources.get( _key ) ;
	}
	
	protected void storeSource( final T _source, final int _id )
	{
		activeSources.add( _source ) ;
		sources.put( _id, _source ) ;
	}
	
	protected void updateSources( final float _dt )
	{
		final int size = activeSources.size() ;
		for( int i = 0; i < size; ++i )
		{
			updateSource( activeSources.get( i ), _dt ) ;
		}
	}

	protected void removeSources()
	{
		for( final T remove : removeSources )
		{
			destroySource( remove ) ;
			activeSources.remove( remove ) ;
		}

		removeSources.clear() ;
	}

	/**
		Used for custom update code, allows source to be removed from updating, etc.
	**/
	protected abstract void updateSource( final T _source, final float _dt ) ;

	/**
		Used to unregister resources that the source might be accessing.
	**/
	protected abstract void destroySource( final T _source ) ;
}