package com.linxonline.mallet.util.settings ;

/*==============================================================*/
// ObjectVariable Key-Value that manages stores object value    //
/*==============================================================*/

public final class ObjectVariable extends VariableInterface
{
	public Object value ;

	public ObjectVariable()
	{
		type = VariableInterface.OBJECT_TYPE ;
	}
	
	public ObjectVariable( final String _name, 
						   final Object _value )
	{
		type = VariableInterface.OBJECT_TYPE ;
		name = _name ;
		setObject( _value ) ;
	}

	public void setObject( final Object _value )
	{
		type = VariableInterface.OBJECT_TYPE ;
		value = _value ;
	}
	
	public String toString()
	{
		return value.toString() ;
	}
}
