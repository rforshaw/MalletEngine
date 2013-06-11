package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;

public class QuickSort
{
	/**
		Recursive Quicksort algorithm.
		Used to sort RenderContainers based on their LAYER.
		-10, -4, 0, 1, 5, 11,
	**/
	public static ArrayList<RenderContainer> quicksort( ArrayList<RenderContainer> _contents )
	{
		final int size = _contents.size() ;
		if( size <= 1 )
		{
			return _contents ;
		}
		
		int layer = 0 ;
		final int pivot = _contents.get( size / 2 ).settings.getInteger( "LAYER" ) ;
		RenderContainer pivotContainer = _contents.get( size / 2 ) ;
		_contents.remove( pivot ) ;
		
		ArrayList<RenderContainer> less = new ArrayList<RenderContainer>() ;
		ArrayList<RenderContainer> greater = new ArrayList<RenderContainer>() ;
		
		for( RenderContainer container : _contents )
		{
			layer = container.settings.getInteger( "LAYER" ) ;
			if( layer <= pivot )
			{
				less.add( container ) ;
			}
			else
			{
				greater.add( container ) ;
			}
		}

		less = quicksort( less ) ;
		greater = quicksort( greater ) ;

		less.add( pivotContainer ) ;
		less.addAll( greater ) ;
		return less ;
	}
}
