package com.linxonline.mallet.util.settings ;

/*==============================================================*/
// FloatVariable Key-Value that manages stores float value      //
/*==============================================================*/

public final class FloatVariable extends VariableInterface
{
	public float value = 0 ;

	public FloatVariable()
	{
		type = VariableInterface.FLOAT_TYPE ;
	}

	public FloatVariable( final String _name, final float _value )
	{
		type = VariableInterface.FLOAT_TYPE ;
		name = _name ;
		value = _value ;
	}

	public void setFloat( final float _value )
	{
		value = _value ;
		type = VariableInterface.FLOAT_TYPE ;
	}
	
	public String toString()
	{
		return Float.toString( value ) ;
	}
}