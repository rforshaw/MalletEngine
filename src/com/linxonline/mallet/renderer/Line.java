package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.* ;

public class Line
{
	public Vector2 start = null ;
	public Vector2 end = null ;
	
	public Line()
	{
		start = new Vector2() ;
		end = new Vector2() ;
	}

	public Line( final Vector2 _start, final Vector2 _end )
	{
		start = _start ;
		end = _end ;
	}
}