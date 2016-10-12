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

	@Override
	public int hashCode()
	{
		int hash = 17 ;
		hash = hash * 31 + i ;
		hash = hash * 31 + j ;
		return hash ;
	}

	@Override
	public boolean equals( final Object _obj )
	{
		if( _obj instanceof Array2DPoint )
		{
			final Array2DPoint point = ( Array2DPoint )_obj ;
			if( this == _obj )
			{
				return true ;
			}
			else if( i == point.i && j == point.j )
			{
				return true ;
			}
		}

		return false ;
	}

	@Override
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
