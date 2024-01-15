package com.linxonline.mallet.util.settings ;

import com.linxonline.mallet.io.serialisation.* ;

/*==============================================================*/
// VariableInterface Key-Value that manages different 			//
// variables.													//
// This base class doesn't store the actual value.				//
// Used primarily by the Settings class. Developer should never //
// directly use this.											//
/*==============================================================*/

public abstract class AVariable
{
	public final static int NONE_TYPE = 0 ;
	public final static int INT_TYPE = 1 ;
	public final static int FLOAT_TYPE = 2 ;
	public final static int STRING_TYPE = 3 ;
	public final static int BOOLEAN_TYPE = 4 ;
	public final static int OBJECT_TYPE = 5 ;
	public final static int ENUM_TYPE = 6 ;

	public String name = "NONE" ;
	protected int type = NONE_TYPE ;

	public void setName( final String _name )
	{
		name = _name ;
	}

	public String getName()
	{
		return name ;
	}

	public final int getType()
	{
		return type ;
	}

	public String toString()
	{
		return "NONE" ;
	}
}
