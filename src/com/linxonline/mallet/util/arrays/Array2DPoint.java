package com.linxonline.mallet.util.arrays ;

public class Array2DPoint
{
	public int i = 0 ;
	public int j = 0 ;

	public Array2DPoint() {}

	public Array2DPoint( final int _i, final int _j )
	{
		i = _i ;
		j = _j ;
	}

	public void setIJ( final int _i, final int _j )
	{
		i = _i ;
		j = _j ;
	}

	public String toString()
	{
		final StringBuffer buffer = new StringBuffer() ;
		buffer.append( "I: " ) ;
		buffer.append( i ) ;
		buffer.append( " J: " ) ;
		buffer.append( j ) ;
		return buffer.toString() ;
	}
}