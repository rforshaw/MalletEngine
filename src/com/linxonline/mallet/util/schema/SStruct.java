package com.linxonline.mallet.util.schema ;

import com.linxonline.mallet.util.Tuple ;

public class SStruct implements IVar
{
	private final Tuple<String, IVar>[] variables ;

	private SStruct( final Tuple<String, IVar>[] _variables )
	{
		if( _variables == null )
		{
			throw new NullPointerException() ;
		}

		variables = _variables ;
	}

	public static SStruct create( final Tuple<String, IVar>... _variables )
	{
		return new SStruct( _variables ) ;
	}

	public Tuple<String, IVar>[] getVariables()
	{
		return variables ;
	}

	@Override
	public Type getType()
	{
		return Type.STRUCT ;
	}

	@Override
	public int hashCode()
	{
		int hash = 7 ;
		for( Tuple<String, IVar> variable : variables )
		{
			hash = 31 * hash + variable.getLeft().hashCode() ;
		}
		return hash ;
	}

	@Override
	public boolean equals( Object _obj )
	{
		if( _obj instanceof SStruct )
		{
			final SStruct struct = ( SStruct )_obj ;

			final int size = variables.length ;
			if( size != struct.variables.length )
			{
				return false ;
			}

			for( int i = 0; i < size; i++ )
			{
				Tuple left = variables[i] ;
				Tuple right = struct.variables[i] ;

				if( left.equals( right ) == false )
				{
					return false ;
				}
			}

			return true ;
		}

		return false ;
	}
}
