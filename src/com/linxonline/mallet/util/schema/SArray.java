package com.linxonline.mallet.util.schema ; 

public class SArray extends SNode
{
	private final SNode contains ;
	private final int length ;

	public SArray( final SNode _contains )
	{
		this( _contains, 0 ) ;
	}

	public SArray( final SNode _contains, final int _length )
	{
		contains = _contains ;
		contains.setParent( this ) ;

		length = _length ;
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
		return length == 0 ;
	}

	/**
		Return the length of the array, it will return 
		0, if the array is of an unspecified size.
	*/
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
