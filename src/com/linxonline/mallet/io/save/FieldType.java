package com.linxonline.mallet.io.save ;

import java.lang.Class ;
import java.lang.reflect.Field ;

enum FieldType
{
	OBJECT,
	PRIMITIVE ;

	public static FieldType getType( final Field _field )
	{
		final Class clazz = _field.getType() ;
		return clazz.isPrimitive() ? FieldType.PRIMITIVE : FieldType.OBJECT ;
	}
}
