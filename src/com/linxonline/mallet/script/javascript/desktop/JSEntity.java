package com.linxonline.mallet.script.javascript ;

import java.util.List ;
import java.util.Set ;
import java.util.HashSet ;

import java.lang.reflect.Method ;

import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.entity.Entity ;

public final class JSEntity
{
	private final Entity entity ;
	private final List<JSComponent> components = MalletList.<JSComponent>newList() ;

	public JSEntity( final Entity _entity )
	{
		entity = _entity ;
	}

	public boolean hasComponentByAbsoluteName( final String _name )
	{
		final JSComponent component = getByAbsoluteName( _name ) ;
		return ( component != null ) ? true : false ;
	}

	public boolean hasComponentBySimpleName( final String _name )
	{
		final JSComponent component = getBySimpleName( _name ) ;
		return ( component != null ) ? true : false ;
	}

	/**
		Retrieve a specific component using the components
		absolute name, for example:
			com.linxonline.mallet.entity.components.UIComponent
	*/
	public Object getComponentByAbsoluteName( final String _name )
	{
		final JSComponent component = getByAbsoluteName( _name ) ;
		return ( component != null ) ? component.getProxy() : null ;
	}

	/**
		Retrieve a specific component using the components
		simple name, for example:
			UIComponent
		NOTE: This function is for convience it's unlikely you'll
		have multiple components attached to an entity with the
		same class name but within different packages.
		If you do... use getComponentByAbsoluteName() instead.
	*/
	public Object getComponentBySimpleName( final String _name )
	{
		final JSComponent component = getBySimpleName( _name ) ;
		return ( component != null ) ? component.getProxy() : null ;
	}

	/**
		Return all the components that implement ScriptInterface interfaces.
		Components that extend these interfaces are deliberately designed
		for scripting use.
	*/
	public List<Object> getComponents()
	{
		retrieveComponents() ;

		final List<Object> fill = MalletList.<Object>newList( components.size() ) ;
		for( final JSComponent component : components )
		{
			fill.add( component.getProxy() ) ;
		}
		return fill ;
	}

	public void destroy()
	{
		entity.destroy() ;
	}

	public boolean isDead()
	{
		return entity.isDead() ;
	}

	private JSComponent getBySimpleName( final String _name )
	{
		retrieveComponents() ;
		for( final JSComponent component : components )
		{
			if( component.getSimpleName().equals( _name ) )
			{
				return component ;
			}
		}

		return null ;
	}
	
	private JSComponent getByAbsoluteName( final String _name )
	{
		retrieveComponents() ;
		for( final JSComponent component : components )
		{
			if( component.getAbsoluteName().equals( _name ) )
			{
				return component ;
			}
		}

		return null ;
	}

	private void retrieveComponents()
	{
		if( components.isEmpty() )
		{
			final Set<Class> interfaces = new HashSet<Class>() ;

			final List<Entity.Component> comps = entity.getAllComponents( MalletList.<Entity.Component>newList() ) ;
			for( final Entity.Component component : comps )
			{
				final Class clazz = component.getClass() ;
				Utils.getScriptInterfaces( clazz, interfaces ) ;
				if( interfaces.size() > 0 )
				{
					// Only added the component if it implements
					// ScriptInterface interfaces.

					final Class[] classArray = interfaces.toArray( new Class[0] ) ;
					components.add( new JSComponent( component, clazz, classArray ) ) ;
				}
				interfaces.clear() ;
			}
		}
	}
}
