package com.linxonline.mallet.ui ;

import java.util.ArrayList ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

public class UILayout extends UIElement
{
	private final ArrayList<UIElement> elements = new ArrayList<UIElement>() ;
	private final ArrayList<UISpacer> spacers = new ArrayList<UISpacer>() ;
	private final UIElementUpdater updater ;

	public UILayout( final Type _type )
	{
		this( _type, new Vector3(), new Vector3(), new Vector3() ) ;
	}

	public UILayout( final Type _type, final Vector3 _position, final Vector3 _offset, final Vector3 _length )
	{
		super( _position, _offset, _length ) ;
		switch( _type )
		{
			case HORIZONTAL : updater = getHorizontalUpdater() ; break ;
			case VERTICAL   : updater = getVerticalUpdater() ;   break ;
			case GRID       : updater = getGridUpdater() ;       break ;
			case FORM       : updater = getFormUpdater() ;       break ;
			default         : updater = getFormUpdater() ;       break ;
		}
	}

	@Override
	public void setInputAdapterInterface( final InputAdapterInterface _adapter )
	{
		super.setInputAdapterInterface( _adapter ) ;
		for( final UIElement element : elements )
		{
			element.setInputAdapterInterface( getInputAdapter() ) ;
		}
	}

	@Override
	public void setEventController( final EventController _controller )
	{
		super.setEventController( _controller ) ;
		for( final UIElement element : elements )
		{
			element.setEventController( getEventController() ) ;
		}
	}

	public void addElement( final UIElement _element )
	{
		if( elements.contains( _element ) == false )
		{
			_element.setInputAdapterInterface( getInputAdapter() ) ;
			_element.setEventController( getEventController() ) ;
			elements.add( _element ) ;

			if( _element instanceof UISpacer )
			{
				spacers.add( ( UISpacer )_element ) ;
			}
		}
	}
	
	public void removeElement( final UIElement _element )
	{
		if( elements.remove( _element ) == true )
		{
			if( _element instanceof UISpacer )
			{
				spacers.remove( ( UISpacer )_element ) ;
			}
			_element.clear() ;
		}
	}

	@Override
	public void update( final float _dt )
	{
		updater.update( _dt, elements ) ;
		for( final UIElement element : elements )
		{
			element.update( _dt ) ;
		}
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		for( final UIElement element : elements )
		{
			if( element.passInputEvent( _event ) == InputEvent.Action.CONSUME )
			{
				return InputEvent.Action.CONSUME ;
			}
		}

		return InputEvent.Action.PROPAGATE ;
	}

	@Override
	public void clear()
	{
		super.clear() ;
		for( final UIElement element : elements )
		{
			element.clear() ;
		}
		elements.clear() ;
		spacers.clear() ;
	}

	@Override
	public void reset()
	{
		super.reset() ;
		for( final UIElement element : elements )
		{
			element.reset() ;
		}
		elements.clear() ;
		spacers.clear() ;
	}

	private UIElementUpdater getHorizontalUpdater()
	{
		return new UIElementUpdater()
		{
			private final Vector3 position = new Vector3() ;

			public void update( final float _dt, final ArrayList<UIElement> _elements )
			{
				final int size = _elements.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _elements.get( i ) ;
					calcAbsolutePosition( position, element ) ;
				}
			}
		} ;
	}

	private UIElementUpdater getVerticalUpdater()
	{
		return new UIElementUpdater()
		{
			private final Vector3 position = new Vector3() ;

			public void update( final float _dt, final ArrayList<UIElement> _elements )
			{
				final int size = _elements.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _elements.get( i ) ;
					calcAbsolutePosition( position, element ) ;
				}
			}
		} ;
	}

	private UIElementUpdater getGridUpdater()
	{
		return new UIElementUpdater()
		{
			private final Vector3 position = new Vector3() ;

			public void update( final float _dt, final ArrayList<UIElement> _elements )
			{
				final int size = _elements.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _elements.get( i ) ;
					calcAbsolutePosition( position, element ) ;
				}
			}
		} ;
	}

	private UIElementUpdater getFormUpdater()
	{
		return new UIElementUpdater()
		{
			private final Vector3 position = new Vector3() ;

			public void update( final float _dt, final ArrayList<UIElement> _elements )
			{
				final int size = _elements.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _elements.get( i ) ;
					calcAbsolutePosition( position, element ) ;
				}
			}
		} ;
	}

	private static void calcAbsolutePosition( final Vector3 _pos, final UIElement _element )
	{
		final Vector3 pos = _element.getPosition() ;
		final Vector3 offset = _element.getOffset() ;
		_pos.setXYZ( pos.x + offset.x, pos.y + offset.y, pos.z + offset.z ) ;
	}

	public enum Type
	{
		HORIZONTAL,
		VERTICAL,
		GRID,
		FORM
	}

	private interface UIElementUpdater
	{
		public void update( final float _dt, final ArrayList<UIElement> _elements ) ;
	}
}
