package com.linxonline.mallet.util.schema ; 

public class SPrim extends SNode
{
	private final Type type ;
	private final int length ;

	/**
		Specify the type of primitive and the length in bytes
		of the primitive.
	*/
	private SPrim( final Type _type, final int _length )
	{
		type = _type ;
		length = _length ;
	}

	public static SPrim bool()
	{
		return new SPrim( Type.BOOL, 1 ) ;
	}

	public static SPrim integer()
	{
		return new SPrim( Type.INTEGER, 4 ) ;
	}
	
	public static SPrim flt()
	{
		return new SPrim( Type.FLOAT, 4 ) ;
	}

	@Override
	public int getLength()
	{
		return length ;
	}

	@Override
	public Type getType()
	{
		return type ;
	}

	@Override
	public int hashCode()
	{
		return type.hashCode() ;
	}

	@Override
	public boolean equals( Object _obj )
	{
		if( _obj instanceof SPrim )
		{
			SPrim primitive = ( SPrim )_obj ;
			return type.equals( primitive.type ) ;
		}

		return false ;
	}
}
