package com.linxonline.mallet.util.settings ;

/*==============================================================*/
// ObjectVariable Key-Value that manages stores object value    //
/*==============================================================*/

public final class ObjectVariable<T> extends VariableInterface
{
	public Class<T> clazz ;
	public T value ;

	public ObjectVariable()
	{
		type = VariableInterface.OBJECT_TYPE ;
	}
	
	public ObjectVariable( final String _name, final T _value, final Class<T> _clazz )
	{
		type = VariableInterface.OBJECT_TYPE ;
		name = _name ;
		setObject( _value, _clazz ) ;
	}

	public void setObject( final T _value, final Class<T> _clazz )
	{
		type = VariableInterface.OBJECT_TYPE ;
		value = _value ;
		clazz = _clazz ;
	}
	
	public String toString()
	{
		return value.toString() ;
	}
}
