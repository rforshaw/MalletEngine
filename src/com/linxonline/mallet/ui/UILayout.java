package com.linxonline.mallet.ui ;

import java.util.ArrayList ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

public class UILayout extends UIElement
{
	private final ArrayList<UIElement> elements = new ArrayList<UIElement>() ;	// Contains Buttons, and other UI elements
	private final ArrayList<UIElement> spacers = new ArrayList<UIElement>() ;	// Contains Spacers, and elements that extend UISpacer
	private final ArrayList<UIElement> ordered = new ArrayList<UIElement>() ;	// Contains all UIElements - Buttons and Spacers
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
		if( ordered.contains( _element ) == true )
		{
			// Return if the element already resides
			// within this UILayout.
			return ;
		}

		ordered.add( _element ) ;
		if( _element instanceof UISpacer )
		{
			spacers.add( _element ) ;
		}
		else
		{
			_element.setInputAdapterInterface( getInputAdapter() ) ;
			_element.setEventController( getEventController() ) ;
			elements.add( _element ) ;
		}
	}
	
	public void removeElement( final UIElement _element )
	{
		if( ordered.remove( _element ) == false )
		{
			// Return if the element does not reside
			// within this UILayout.
			return ;
		}

		_element.clear() ;
		if( _element instanceof UISpacer )
		{
			spacers.remove( _element ) ;
		}
		else
		{
			elements.remove( _element ) ;
		}
	}

	@Override
	public void update( final float _dt )
	{
		updater.update( _dt, ordered, spacers ) ;
		for( final UIElement element : ordered )
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
		for( final UIElement element : ordered )
		{
			element.clear() ;
		}
		ordered.clear() ;
		elements.clear() ;
		spacers.clear() ;
	}

	@Override
	public void reset()
	{
		super.reset() ;
		for( final UIElement element : ordered )
		{
			element.reset() ;
		}

		ordered.clear() ;
		elements.clear() ;
		spacers.clear() ;
	}

	/**
		Order the UIElements from left to right along the x-axis.
		When the layouts length.x has been filled, drop down to the 
		next row, the next row starts at the maximum height of the 
		last row.
	*/
	private UIElementUpdater getHorizontalUpdater()
	{
		return new UIElementUpdater()
		{
			private final Vector3 layoutPosition = new Vector3() ;
			private final Vector3 position = new Vector3() ;
			private final Vector3 totalLength = new Vector3() ;

			public void update( final float _dt, final ArrayList<UIElement> _elements, final ArrayList<UIElement> _spacers )
			{
				calcTotalLength( _elements, totalLength ) ;
				updateSpaceLengths( _spacers, totalLength ) ;

				calcAbsolutePosition( position, UILayout.this ) ;
				position.setXYZ( layoutPosition ) ;

				final int size = _elements.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _elements.get( i ) ;
					final Vector3 length = element.getLength() ;
					final Vector3 margin = element.getMargin() ;

					if( UILayout.this.intersectPoint( position.x, position.y ) == false || 
						UILayout.this.intersectPoint( position.x + length.x, position.y + length.y ) == false )
					{
						position.setXYZ( layoutPosition.x, position.y + length.y + margin.y, layoutPosition.z ) ;
					}

					element.setPosition( position.x, position.y, position.z ) ;
					position.setXYZ( position.x + length.x + margin.x, position.y, layoutPosition.z ) ;
				}
			}
		} ;
	}

	/**
		Order the UIElements from top to bottom along the y-axis.
		When the layouts length.y has been filled, jump to the 
		next column, the next column starts at the maximum width 
		of the last column.
	*/
	private UIElementUpdater getVerticalUpdater()
	{
		return new UIElementUpdater()
		{
			private final Vector3 layoutPosition = new Vector3() ;
			private final Vector3 position = new Vector3() ;
			private final Vector3 totalLength = new Vector3() ;

			public void update( final float _dt, final ArrayList<UIElement> _elements, final ArrayList<UIElement> _spacers )
			{
				calcTotalLength( _elements, totalLength ) ;
				updateSpaceLengths( _spacers, totalLength ) ;

				calcAbsolutePosition( layoutPosition, UILayout.this ) ;
				position.setXYZ( layoutPosition ) ;

				final int size = _elements.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _elements.get( i ) ;
					final Vector3 length = element.getLength() ;
					final Vector3 margin = element.getMargin() ;

					if( UILayout.this.intersectPoint( position.x, position.y ) == false || 
						UILayout.this.intersectPoint( position.x + length.x, position.y + length.y ) == false )
					{
						position.setXYZ( position.x + length.x + margin.x, layoutPosition.y, layoutPosition.z ) ;
					}

					element.setPosition( position.x, position.y, position.z ) ;
					position.setXYZ( position.x, position.y + length.y + margin.y, layoutPosition.z ) ;
				}
			}
		} ;
	}

	private UIElementUpdater getGridUpdater()
	{
		return new UIElementUpdater()
		{
			private final Vector3 position = new Vector3() ;
			private final Vector3 totalLength = new Vector3() ;

			public void update( final float _dt, final ArrayList<UIElement> _elements, final ArrayList<UIElement> _spacers )
			{
				calcTotalLength( _elements, totalLength ) ;
				updateSpaceLengths( _spacers, totalLength ) ;

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
			private final Vector3 totalLength = new Vector3() ;

			public void update( final float _dt, final ArrayList<UIElement> _elements, final ArrayList<UIElement> _spacers )
			{
				calcTotalLength( _elements, totalLength ) ;
				updateSpaceLengths( _spacers, totalLength ) ;

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

	private static void calcTotalLength( final ArrayList<UIElement> _elements, final Vector3 _total )
	{
		_total.setXYZ( 0.0f, 0.0f, 0.0f ) ;
		for( final UIElement element : _elements )
		{
			final Vector3 length = element.getLength() ;
			_total.x += length.x ;
			_total.y += length.y ;
			_total.z += length.z ;
		}
	}

	private static void updateSpaceLengths( final ArrayList<UIElement> _spacers, final Vector3 _totalLength )
	{
		final int num = _spacers.size() ;
		final float lengthX = _totalLength.x / num ;
		final float lengthY = _totalLength.y / num ;
		final float lengthZ = _totalLength.z / num ;

		for( final UIElement spacer : _spacers )
		{
			spacer.setLength( lengthX, lengthY, lengthZ ) ;
		}
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
		public void update( final float _dt, final ArrayList<UIElement> _elements, final ArrayList<UIElement> _spacers ) ;
	}
}
