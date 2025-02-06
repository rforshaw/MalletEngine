package com.linxonline.mallet.script.javascript ;

import java.util.Set ;
import java.util.HashSet ;
import java.util.Collection ;

import java.lang.reflect.Array ;
import java.lang.reflect.Method ;
import java.lang.reflect.Proxy ;

import com.linxonline.mallet.script.Script ;

public final class JSObject
{
	private static final Set<Class<?>> PRIMITIVE_TYPES = createPrimitiveTypes() ;
	private static final Set<Class<?>> ITERABLE_TYPES = createIterableTypes() ;

	private final String name ;
	private final Object source ;
	private final Object proxy ;

	private JSObject( final String _name, final Object _source, final Object _proxy )
	{
		name = _name ;
		source = _source ;
		proxy = _proxy ;
	}

	public static JSObject create( final Script.Register _reg )
	{
		final Object source = _reg.getObject() ;
		final Object proxy = createProxy( source ) ;
		return new JSObject( _reg.getName(), source, proxy ) ;
	}

	private static Object createProxy( final Object _source )
	{
		final Class clazz = _source.getClass() ;
		final Set<Class> interfaces = Utils.getScriptInterfaces( clazz, new HashSet<Class>() ) ;
		if( interfaces.size() <= 0 )
		{
			return null ;
		}

		return createProxy( _source, clazz, interfaces ) ;
	}

	private static Object createProxy( final Object _source, final Class _clazz, final Set<Class> _interfaces )
	{
		final Class[] classArray = _interfaces.toArray( new Class[0] ) ;

		final ClassLoader loader = _clazz.getClassLoader() ;
		return Proxy.newProxyInstance( loader, classArray, ( final Object _proxy, final Method _method, final Object[] _args ) ->
		{
			final Class<?> type = _method.getReturnType() ;
			if( type.isPrimitive() || type.isEnum() || PRIMITIVE_TYPES.contains( type ) )
			{
				return _method.invoke( _source, _args ) ;
			}

			if( type.isArray() )
			{
				final Object array = _method.invoke( _source, _args ) ;

				final Class<?> componentType = type.getComponentType() ;
				if( componentType.isPrimitive() || PRIMITIVE_TYPES.contains( componentType ) )
				{
					return array ;
				}

				return createArrayProxy( array, componentType ) ;
			}

			if( Iterable.class.isAssignableFrom( type ) )
			{
				return _method.invoke( _source, _args ) ;
			}

			// We want to wrap our return object in a proxy.
			return createProxy( _method.invoke( _source, _args ) ) ;
		} ) ;
	}

	private static Object createArrayProxy( final Object _array, final Class<?> _componentType )
	{
		final int length = Array.getLength( _array ) ;
		final Object proxies = Array.newInstance( _componentType, length ) ;

		if( length <= 0 )
		{
			return proxies ;
		}

		final Object source = Array.get( _array, 0 ) ;
		final Class<?> clazz = source.getClass() ;

		final Set<Class> interfaces = Utils.getScriptInterfaces( clazz, new HashSet<Class>() ) ;

		for( int i = 0; i < length; ++i )
		{
			Array.set( proxies, i, createProxy( Array.get( _array, i ), clazz, interfaces ) ) ;
		}

		return proxies ;
	}

	private static Set<Class<?>> createPrimitiveTypes()
	{
		final Set<Class<?>> types = new HashSet<Class<?>>() ;
		types.add( Boolean.class ) ;
		types.add( Character.class ) ;
		types.add( Byte.class ) ;
		types.add( Short.class ) ;
		types.add( Integer.class ) ;
		types.add( Long.class ) ;
		types.add( Float.class ) ;
		types.add( Double.class ) ;
		types.add( Void.class ) ;
		types.add( String.class ) ;
		return types ;
	}

	private static Set<Class<?>> createIterableTypes()
	{
		final Set<Class<?>> types = new HashSet<Class<?>>() ;
		types.add( Collection.class ) ;
		return types ;
	}

	public String getName()
	{
		return name ;
	}

	public Object getProxy()
	{
		return proxy ;
	}
}
