package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.arrays.ManagedArray ;

public class DrawState extends ManagedArray<DrawData>
{
	public synchronized void upload( final int _diff, final int _iteration )
	{
		manageState() ;

		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			current.get( i ).upload( _diff, _iteration ) ;
		}
	}

	public List<DrawData> getActiveDraws()
	{
		return current ;
	}

	@Override
	protected void addNewData( final List<DrawData> _toAdd )
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

	public void sort() {}

	public void clear() {}
}
