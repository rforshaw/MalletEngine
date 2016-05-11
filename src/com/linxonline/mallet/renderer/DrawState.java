package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;

public class DrawState extends State<DrawData>
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

	public ArrayList<DrawData> getActiveDraws()
	{
		return current ;
	}

	@Override
	protected void addNewData( final ArrayList<DrawData> _toAdd )
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