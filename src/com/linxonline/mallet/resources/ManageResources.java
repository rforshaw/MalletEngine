package com.linxonline.mallet.resources ;

import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import com.linxonline.mallet.util.MalletList ;

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
	public static List<String> findUnwantedResources( final Map<String, ? extends Resource> _resources )
	{
		//Resource resource = null ;
		//final Set<String> keys = _resources.keySet() ;
		final List<String> remove = MalletList.<String>newList() ;

		/*for( final String key : keys )
		{
			resource = _resources.get( key ) ;
			if( resource != null )
			{
				if( ( resource.getReferenceCount() == 0 ) &&
					( resource.getLivingTime() > DEFAULT_LIVING_TIME ) )
				{
					remove.add( key ) ;
				}
			}
		}*/

		return remove ;
	}

	public static void removeUnwantedResources( final Map<String, ? extends Resource> _resources )
	{
		removeUnwantedResources( _resources, findUnwantedResources( _resources ) ) ;
	}

	/**
		Iterates over their names and removes them from the Map
	**/
	public static void removeUnwantedResources( final Map<String, ? extends Resource> _resources, 
												final List<String> _remove )
	{
		Resource resource = null ;
		for( final String key : _remove )
		{
			resource = ( Resource )_resources.get( key ) ;
			resource.destroy() ;							// Destroy the resource

			//System.out.println( "Removed: " + key ) ;
			_resources.remove( key ) ;						// Remove from Map
		}

		_remove.clear() ;
	}
}
