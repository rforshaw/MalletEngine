package com.linxonline.mallet.util.schema ; 

public class SArray extends SNode
{
	private final SNode contains ;
	private final int arraylength ;

	public SArray( final SNode _contains )
	{
		this( _contains, 0 ) ;
	}

	public SArray( final SNode _contains, final int _length )
	{
		contains = _contains ;
		contains.setParent( this ) ;

		arraylength = _length ;
	}

	public SNode getSupportedType()
	{
		return contains ;
	}

	/**
		An array without a fixed length (a length 
		greater than 0) is considered dynamic. 
	*/
	public boolean isDynamic()
	{
		return arraylength == 0 ;
	}

	/**
		Return the length of the array, it will return 
		0, if the array is of an unspecified size.
	*/
	public int getArrayLength()
	{
		return arraylength ;
	}
	
	/**
		Return the length of the array in bytes, it 
		will return 0, if the array is of an unspecified size.
	*/
	@Override
	public int getLength()
	{
		return arraylength * contains.getLength() ;
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
		hash = 31 * hash + arraylength ;
		return hash ;
	}

	@Override
	public boolean equals( Object _obj )
	{
		if( _obj instanceof SArray )
		{
			final SArray array = ( SArray )_obj ;
			if( arraylength != array.arraylength )
			{
				return false ;
			}

			return contains.equals( array.contains ) ;
		}

		return false ;
	}
}
