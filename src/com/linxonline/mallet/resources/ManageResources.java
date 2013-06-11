package com.linxonline.mallet.resources ;

import java.util.* ;

/*==================================================*/
// The default Resource cleaner when AbstractManagers
// clean() is called.
// If a Resource has a reference count of 0, and it 
// has existed for longer than 10 seconds, then the 
// resources is identified for removal.
/*==================================================*/
public abstract class ManageResources
{
	private static final int DEFAULT_LIVING_TIME = 10 ;

	ManageResources() {}

	/**
		returns the names of all unused rescources
	**/
	public static ArrayList<String> findUnwantedResources( final HashMap _resources )
	{
		Resource resource = null ;
		final Set<String> keys = ( Set<String> )_resources.keySet() ;
		final ArrayList<String> remove = new ArrayList<String>() ;

		for( final String key : keys )
		{
			resource = ( Resource )_resources.get( key ) ;
			if( resource.getLivingTime() > DEFAULT_LIVING_TIME )
			{
				if( resource.getReferenceCount() == 0 )
				{
					remove.add( key ) ;
				}
			}
		}

		return remove ;
	}

	public static void removeUnwantedResources( final HashMap _resources )
	{
		removeUnwantedResources( _resources, findUnwantedResources( _resources ) ) ;
	}

	/**
		Iterates over their names and removes them from the HashMap
	**/
	public static void removeUnwantedResources( final HashMap _resources, 
												final ArrayList<String> _remove )
	{
		Resource resource = null ;
		for( final String key : _remove )
		{
			resource = ( Resource )_resources.get( key ) ;
			resource.destroy() ;							// Destroy the resource

			//System.out.println( "Removed: " + key ) ;
			_resources.remove( key ) ;						// Remove from HashMap
		}

		_remove.clear() ;
	}
}