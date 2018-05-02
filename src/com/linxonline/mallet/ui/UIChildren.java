package com.linxonline.mallet.ui ;

import java.util.List ;

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
	private final List<UIElement> toRemove = MalletList.<UIElement>newList() ;		// UIElements to be removed from the layout.

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
	public void getElements( final List<UIElement> _elements )
	{
		_elements.addAll( ordered ) ;
	}

	@Override
	public void removeElement( final UIElement _element )
	{
		if( toRemove.contains( _element ) == false )
		{
			toRemove.add( _element ) ;
		}
	}

	public List<UIElement> getElements()
	{
		return ordered ;
	}

	public void passDrawDelegate( final DrawDelegate _delegate, final World _world, final Camera _camera )
	{
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			ordered.get( i ).passDrawDelegate( _delegate, _world, _camera ) ;
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
			if( element.isIntersectInput( _event ) == true )
			{
				return element ;
			}
		}

		return null ;
	}

	public boolean update( final float _dt, final List<Event<?>> _events )
	{
		boolean dirtyChildren = false ;
	
		{
			final int size = ordered.size() ;
			for( int i = 0; i < size; i++ )
			{
				final UIElement element = ordered.get( i ) ;
				if( element.isDirty() == true )
				{
					// If a Child element is updating we'll 
					// most likely also want to update the parent.
					dirtyChildren = true ;
				}

				element.update( _dt, _events ) ;

				if( element.destroy == true )
				{
					// If the child element is flagged for 
					// destruction add it to the remove list.
					removeElement( element ) ;

					// We'll also want to refresh the UILayout.
					dirtyChildren = true ;
				}
			}
		}

		if( toRemove.isEmpty() == false )
		{
			final int size = toRemove.size() ;
			for( int i = 0; i < size; i++ )
			{
				final UIElement element = toRemove.get( i ) ;
				if( ordered.remove( element ) == true )
				{
					element.shutdown() ;
					element.clear() ;
				}
			}
			toRemove.clear() ;
		}

		return dirtyChildren ;
	}
}
