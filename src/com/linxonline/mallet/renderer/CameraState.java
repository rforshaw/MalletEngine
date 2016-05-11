package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;

import com.linxonline.mallet.util.Tuple ;

public class CameraState extends State<CameraData>
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

	/*@Override
	protected void addNewData( final ArrayList<CameraData> _toAdd )
	{
		for( final CameraData add : _toAdd )
		{
			insertNewDrawData( add ) ;
		}
		_toAdd.clear() ;
	}

	private void insertNewDrawData( final CameraData _insert )
	{
		final int order = _insert.getOrder() ;
		final int size = current.size() ;
		if( order < size )
		{
			current.add( order, _insert ) ;
			return ;
		}

		current.add( _insert ) ;
	}*/

	public void sort() {}

	public void clear() {}

	public synchronized Camera getCamera( final String _id )
	{
		{
			final int size = current.size() ;
			for( int i = 0; i < size; i++ )
			{
				final Camera camera = current.get( i ) ;
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
				final Camera camera = toAdd.get( i ) ;
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