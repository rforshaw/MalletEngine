package com.linxonline.mallet.util ;

import java.util.List ;
import java.util.Map ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.event.EventUpdater ;

/**
	Needs a better name before it is too late.
	Deals with generic updating & storage of sources, 
	allows the Audio, Animations systems to deal with 
	the important stuff.
	A source should only ever be removed is specifically 
	instructed to do so. Else the system should continue 
	to update.
*/
public abstract class SystemRoot<T> extends EventUpdater
{
	/**
		The Map and List should be replaced with a more structure 
		that provides effecient iteration & searching capabilites.
	*/
	protected final Map<Integer, T> sources = MalletMap.<Integer, T>newMap() ;
	protected final List<T> activeSources = MalletList.<T>newList() ;
	protected final List<RemoveSource> removeSources = MalletList.<RemoveSource>newList() ;

	public void update( final float _dt )
	{
		updateEvents() ;
		updateSources( _dt ) ;
		removeSources() ;
	}

	/**
		Remove all sources.
	*/
	public void clear()
	{
		removeSources() ;						// Remove the dead sources
		final int size = activeSources.size() ;	// Remove the active sources 
		for( int i = 0; i < size; ++i )
		{
			destroySource( activeSources.get( i ) ) ;
		}
		activeSources.clear() ;
		sources.clear() ;
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

	/**
		Remove unwanted sources.
	*/
	protected void removeSources()
	{
		final int size = removeSources.size() ;
		RemoveSource remove = null ;
		for( int i = 0; i < size; i++ )
		{
			remove = removeSources.get( i ) ;
			destroySource( remove.source ) ;
			activeSources.remove( remove.source ) ;
			sources.remove( remove.id ) ;
		}

		removeSources.clear() ;
	}

	/**
		Used for custom update code, allows source to be removed from updating, etc.
	*/
	protected abstract void updateSource( final T _source, final float _dt ) ;

	/**
		Used to unregister custom resources that the source might be accessing.
	*/
	protected abstract void destroySource( final T _source ) ;

	protected class RemoveSource
	{
		public int id ;
		public T source ;

		public RemoveSource( final int _id, final T _source )
		{
			id = _id ;
			source = _source ;
		}
	}
}
