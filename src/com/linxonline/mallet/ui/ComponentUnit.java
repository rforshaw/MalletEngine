package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

public class ComponentUnit
{
	private final List<UIElement.Component> components = MalletList.<UIElement.Component>newList() ;

	public ComponentUnit() {}

	public boolean add( final int _index, final UIElement.Component _component )
	{
		if( _component != null )
		{
			if( components.contains( _component ) == false )
			{
				components.add( _index, _component ) ;
				return true ;
			}
		}
		return false ;
	}

	public boolean remove( final UIElement.Component _component )
	{
		return components.remove( _component ) == true ;
	}

	public List<UIElement.Component> getComponents()
	{
		return components ;
	}

	public void refresh()
	{
		final int size = components.size() ;
		for( int i = 0; i < size; i++ )
		{
			components.get( i ).refresh() ;
		}
	}

	public void shutdown()
	{
		final int size = components.size() ;
		for( int i = 0; i < size; i++ )
		{
			components.get( i ).shutdown() ;
		}
	}

	public void clear()
	{
		components.clear() ;
	}
}
