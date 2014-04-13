package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;

import com.linxonline.mallet.maths.* ;

public class Shape
{
	public final int[] indicies ;
	public final Vector2[] points ;
	public final MalletColour[] colours ;

	private int indexIncrement = 0 ;
	private int pointIncrement = 0 ;
	private int colourIncrement = 0 ;

	public Shape( final int _indexSize, final int _pointSize, final int _colourSize )
	{
		indicies = new int[_indexSize] ;
		points = new Vector2[_pointSize] ;
		colours = new MalletColour[_colourSize] ;
	}

	public Shape( final int _indexSize, final int _pointSize )
	{
		indicies = new int[_indexSize] ;
		points = new Vector2[_pointSize] ;
		colours = null ;
	}

	public void addIndex( final int _index )
	{
		if( indexIncrement < indicies.length )
		{
			indicies[indexIncrement++] = _index ;
		}
	}

	public void addPoint( final Vector2 _point )
	{
		if( pointIncrement < points.length )
		{
			points[pointIncrement++] = _point ;
		}
	}
	
	public void addColour( final MalletColour _colour )
	{
		if( colourIncrement < colours.length )
		{
			colours[colourIncrement++] = _colour ;
		}
	}
}