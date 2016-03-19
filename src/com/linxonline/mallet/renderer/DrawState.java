package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;

public class DrawState
{
	private final ArrayList<DrawData> toAdd = new ArrayList<DrawData>() ;
	private final ArrayList<DrawData> toRemove = new ArrayList<DrawData>() ;
	private final ArrayList<DrawData> current = new ArrayList<DrawData>() ;

	public synchronized void add( final DrawData _data )
	{
		if( _data != null )
		{
			toAdd.add( _data ) ;
		}
	}

	public synchronized void remove( final DrawData _data )
	{
		if( _data != null )
		{
			toRemove.add( _data ) ;
		}
	}

	public synchronized void draw( final int _diff, final int _iteration )
	{
		addNewDrawData( toAdd ) ;

		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			current.get( i ).draw( _diff, _iteration ) ;
		}

		removeOldDrawData( toRemove ) ;
	}

	public synchronized void sort()
	{
	
	}

	public synchronized void clear()
	{
	
	}

	private void addNewDrawData( final ArrayList<DrawData> _toAdd )
	{
		for( final DrawData add : _toAdd )
		{
			insertNewDrawData( add ) ;
		}
		_toAdd.clear() ;
	}

	private void insertNewDrawData( final DrawData _insert )
	{
		final int order = _insert.getOrder() ;
		final int size = current.size() ;
		if( order < size )
		{
			current.add( order, _insert ) ;
			return ;
		}

		current.add( _insert ) ;
	}

	private void removeOldDrawData( final ArrayList<DrawData> _toRemove )
	{
		for( final DrawData remove : _toRemove )
		{
			current.remove( remove ) ;
		}
		_toRemove.clear() ;
	}
}