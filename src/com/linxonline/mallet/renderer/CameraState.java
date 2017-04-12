package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.ManagedArray ;

public class CameraState<C extends CameraData> extends ManagedArray<C>
{
	public CameraState() {}

	public synchronized void draw( final int _diff, final int _iteration )
	{
		manageState() ;

		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			current.get( i ).draw( _diff, _iteration ) ;
		}
	}

	public void sort() {}

	public void clear() {}

	public synchronized C getCamera( final String _id )
	{
		{
			final int size = current.size() ;
			for( int i = 0; i < size; i++ )
			{
				final C camera = current.get( i ) ;
				final String id = camera.getID() ;
				
				if( _id.equals( id ) == true )
				{
					return camera ;
				}
			}
		}

		{
			final int size = toAdd.size() ;
			for( int i = 0; i < size; i++ )
			{
				final C camera = toAdd.get( i ) ;
				final String id = camera.getID() ;
				
				if( _id.equals( id ) == true )
				{
					return camera ;
				}
			}
		}

		return null ;
	}
}
