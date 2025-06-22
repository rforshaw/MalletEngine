package com.linxonline.mallet.ui ;

import java.util.List ;
import java.util.Iterator ;

import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

/**
	Extend this class if the implementation is 
	expected to have children.

	This should only be used to allow other external 
	classes to access, add or remove elements.
*/
public class UIChildren implements IChildren
{
	private final List<UIElement> ordered = MalletList.<UIElement>newList() ;		// Layouts children

	@Override
	public <T extends UIElement> T addElement( final T _element )
	{
		if( ordered.contains( _element ) == false )
		{
			ordered.add( _element ) ;
			return _element ;
		}
		return null ; 
	}

	@Override
	public <T extends UIElement> T addElement( final int _index, final T _element )
	{
		if( ordered.contains( _element ) == false )
		{
			ordered.add( _index, _element ) ;
			return _element ;
		}
		return null ; 
	}

	@Override
	public void getElements( final List<UIElement> _elements )
	{
		_elements.addAll( ordered ) ;
	}

	@Override
	public void removeElement( final UIElement _element )
	{
		if( ordered.remove( _element ) == true )
		{
			_element.shutdown() ;
			_element.clear() ;
		}
	}

	public int size()
	{
		return ordered.size() ;
	}

	public List<UIElement> getElements()
	{
		return ordered ;
	}

	public void setWorldAndCamera( final World _world, final Camera _camera )
	{
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			ordered.get( i ).setWorldAndCamera( _world, _camera ) ;
		}
	}

	public void disengage()
	{
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			final UIElement element = ordered.get( i ) ;
			if( element.isEngaged() == true )
			{
				element.disengage() ;
			}
		}
	}

	public void setLayer( final int _parentlayer )
	{
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			final UIElement element = ordered.get( i ) ;
			if( element.getLayer() <= _parentlayer )
			{
				// Child elements should always be a
				// layer above the parent.
				element.setLayer( _parentlayer + 1 ) ;
			}
		}
	}

	public void setVisible( final boolean _visible )
	{
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			ordered.get( i ).setVisible( _visible ) ;
		}
	}

	public void refresh()
	{
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			ordered.get( i ).makeDirty() ;
		}
	}

	public void destroy()
	{
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			ordered.get( i ).destroy() ;
		}
	}
	
	public void clear()
	{
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			ordered.get( i ).clear() ;
		}
		ordered.clear() ;
	}

	public void shutdown()
	{
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			ordered.get( i ).shutdown() ;
		}
	}

	public void reset()
	{
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			ordered.get( i ).reset() ;
		}
	}
	
	public UIElement isIntersectInput( final InputEvent _event )
	{
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			final UIElement element = ordered.get( i ) ;
			if( element.isIntersectInput( _event ) )
			{
				return element ;
			}
		}

		return null ;
	}

	public boolean update( final float _dt )
	{
		boolean dirtyChildren = false ;

		final Iterator<UIElement> iter = ordered.iterator() ;
		while( iter.hasNext() )
		{
			final UIElement element = iter.next() ;
			if( element.isDirty() )
			{
				// If a Child element is updating we'll 
				// most likely also want to update the parent.
				dirtyChildren = true ;
			}

			element.update( _dt ) ;

			if( element.destroy )
			{
				iter.remove() ;
				element.shutdown() ;
				element.clear() ;
				dirtyChildren = true ;		// We'll also want to refresh the UILayout.
			}
		}

		return dirtyChildren ;
	}
}
