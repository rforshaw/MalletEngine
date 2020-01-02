package com.linxonline.mallet.util.schema ; 

public class SArray implements IVar
{
	private final IVar contains ;
	private final int length ;

	public SArray( IVar _contains, final int _length )
	{
		contains = _contains ;
		length = _length ;
	}

	public IVar getSupportedType()
	{
		return contains ;
	}

	public int getLength()
	{
		return length ;
	}

	@Override
	public Type getType()
	{
		return Type.ARRAY ;
	}

	@Override
	public int hashCode()
	{
		int hash = 7 ;
		hash = 31 * hash + contains.hashCode() ;
		hash = 31 * hash + length ;
		return hash ;
	}

	@Override
	public boolean equals( Object _obj )
	{
		if( _obj instanceof SArray )
		{
			final SArray array = ( SArray )_obj ;
			if( length != array.length )
			{
				return false ;
			}

			return contains.equals( array.contains ) ;
		}

		return false ;
	}
}
