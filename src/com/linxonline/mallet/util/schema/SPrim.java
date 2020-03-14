package com.linxonline.mallet.util.schema ; 

public class SPrim extends SNode
{
	private final Type type ;

	private SPrim( final Type _type )
	{
		type = _type ;
	}

	public static SPrim bool()
	{
		return new SPrim( Type.BOOL ) ;
	}

	public static SPrim integer()
	{
		return new SPrim( Type.INTEGER ) ;
	}
	
	public static SPrim flt()
	{
		return new SPrim( Type.FLOAT ) ;
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
