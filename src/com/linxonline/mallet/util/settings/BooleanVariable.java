package com.linxonline.mallet.util.settings ;

/*==============================================================*/
// BooleanVariable Key-Value that manages stores boolean value  //
/*==============================================================*/

public final class BooleanVariable extends AVariable
{
	public boolean value = false ;

	public BooleanVariable()
	{
		type = AVariable.BOOLEAN_TYPE ;
	}
	
	public BooleanVariable( final String _name, final boolean _value )
	{
		type = AVariable.BOOLEAN_TYPE ;
		name = _name ;
		value = _value ;
	}

	public void setBoolean( final boolean _value )
	{
		value = _value ;
		type = AVariable.BOOLEAN_TYPE ;
	}

	public String toString()
	{
		return value == true ? "true" : "false" ;
	}
}
