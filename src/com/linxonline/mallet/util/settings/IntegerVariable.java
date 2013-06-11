package com.linxonline.mallet.util.settings ;

/*==============================================================*/
// IntegerVariable Key-Value that manages stores integer value  //
/*==============================================================*/

public final class IntegerVariable extends VariableInterface
{
	public int value = 0 ;

	public IntegerVariable()
	{
		type = VariableInterface.INT_TYPE ;
	}
	
	public IntegerVariable( final String _name, final int _value )
	{
		type = VariableInterface.INT_TYPE ;
		name = _name ;
		value = _value ;
	}

	public void setInteger( final int _value )
	{
		value = _value ;
		type = VariableInterface.INT_TYPE ;
	}
	
	public String toString()
	{
		return Integer.toString( value ) ;
	}
}