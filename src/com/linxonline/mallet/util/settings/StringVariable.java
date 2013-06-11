package com.linxonline.mallet.util.settings ;

/*==============================================================*/
// StringVariable Key-Value that manages stores string value    //
/*==============================================================*/

public final class StringVariable extends VariableInterface
{
	public String value = null ;

	public StringVariable( final String _name, final String _value )
	{
		type = VariableInterface.STRING_TYPE ;
		name = _name ;
		value = _value ;
	}

	public void setString( final String _value )
	{
		value = _value ;
		type = VariableInterface.STRING_TYPE ;
	}
	
	public String toString()
	{
		return value ;
	}
}