package com.linxonline.mallet.util.schema ; 

public class SPrim implements IVar
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
	
	public static SPrim matrix4()
	{
		return new SPrim( Type.MATRIX4 ) ;
	}
	
	public static SPrim vec2()
	{
		return new SPrim( Type.VECTOR2 ) ;
	}
	
	public static SPrim vec3()
	{
		return new SPrim( Type.VECTOR3 ) ;
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
