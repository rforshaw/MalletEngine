package com.linxonline.mallet.script.javascript ;

import java.util.List ;

import java.lang.reflect.InvocationHandler ;
import java.lang.reflect.Method ;
import java.lang.reflect.Proxy ;

import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.entity.Entity ;

public final class JSComponent
{
	private final Entity.Component component ;
	private final Class root ;
	private final Class[] interfaces ;

	private Object proxy ;
	private String absoluteName ;
	private String simpleName ;

	public JSComponent( final Entity.Component _component, final Class _root, final Class[] _interfaces )
	{
		component = _component ;
		root = _root ;
		interfaces = _interfaces ;
	}

	/**
		We do not want the script writer to get access to everything
		that the Component can do, so we generate a proxy object based
		on the ScriptInterface interfaces.
		Non-ScriptInterface interfaces are ignored.
	*/
	public Object getProxy()
	{
		if( proxy != null )
		{
			return proxy ;
		}

		final ClassLoader loader = JSComponent.class.getClassLoader() ;
		proxy = Proxy.newProxyInstance( loader, interfaces, ( final Object _proxy, final Method _method, final Object[] _args ) ->
		{
			return _method.invoke( component, _args ) ;
		} ) ;
		return proxy ;
	}

	/**
		Return the simple name of the component.
		If the component is an anonymous class then return the
		parents simple name instead.
	*/
	public String getSimpleName()
	{
		if( simpleName == null )
		{
			simpleName = Utils.getSimpleName( root ) ;
		}
		return simpleName ;
	}

	public String getAbsoluteName()
	{
		if( absoluteName == null )
		{
			absoluteName = Utils.getCanonicalName( root ) ;
		}
		return absoluteName ;
	}
}
