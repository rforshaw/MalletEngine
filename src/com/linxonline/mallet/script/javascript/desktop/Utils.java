package com.linxonline.mallet.script.javascript ;

import java.util.Set ;

import java.lang.reflect.Method ;

import com.linxonline.mallet.script.JavaInterface ;

public final class Utils
{
	public static String getSimpleName( final Class _class )
	{
		if( _class == null )
		{
			return "" ;
		}

		if( _class.isArray() )
		{
			return "" ;
		}

		final String name = _class.getSimpleName() ;
		if( name.isEmpty() == false )
		{
			return name ;
		}

		return getSimpleName( _class.getSuperclass() ) ;
	}

	public static String getCanonicalName( final Class _class )
	{
		if( _class == null )
		{
			return "" ;
		}

		if( _class.isArray() )
		{
			return "" ;
		}

		final String name = _class.getCanonicalName() ;
		if( name.isEmpty() == false )
		{
			return name ;
		}

		return getCanonicalName( _class.getSuperclass() ) ;
	}

	public static Set<Class> getScriptInterfaces( final Class _class, final Set<Class> _fill )
	{
		if( _class == null )
		{
			return _fill ;
		}
	
		if( _class.isArray() )
		{
			return _fill ;
		}

		final Class[] interfaces = _class.getInterfaces() ;
		for( final Class inter : interfaces )
		{
			if( inter.isAnnotationPresent( JavaInterface.class ) )
			{
				_fill.add( inter ) ;
			}
		}

		final Class parent = _class.getSuperclass() ;
		return getScriptInterfaces( parent, _fill ) ;
	}
}
