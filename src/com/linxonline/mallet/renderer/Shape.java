package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;

import com.linxonline.mallet.maths.* ;

public class Shape
{
	public final ArrayList<Integer> indicies = new ArrayList<Integer>() ;
	public final ArrayList<Vector2> points = new ArrayList<Vector2>() ;

	public Shape() {}

	public void addIndex( final int _index )
	{
		indicies.add( _index ) ;
	}

	public void addPoint( final Vector2 _point )
	{
		points.add( _point ) ;
	}
}