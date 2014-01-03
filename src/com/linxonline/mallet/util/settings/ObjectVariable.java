package com.linxonline.mallet.util.settings ;

/*==============================================================*/
// ObjectVariable Key-Value that manages stores object value    //
/*==============================================================*/

public final class ObjectVariable<T> extends VariableInterface
{
	public T value ;

	public ObjectVariable()
	{
		type = VariableInterface.OBJECT_TYPE ;
	}
	
	public ObjectVariable( final String _name, final T _value )
	{
		type = VariableInterface.OBJECT_TYPE ;
		name = _name ;
		setObject( _value ) ;
	}

	public void setObject( final T _value )
	{
		type = VariableInterface.OBJECT_TYPE ;
		value = _value ;		// Even if _value is null set value to it.
	}
	
	public T getObject()
	{
		return value ;
	}
	
	public String toString()
	{
		return value.toString() ;
	}
}
