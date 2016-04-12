package com.linxonline.mallet.ui ;

import java.util.ArrayList ;

import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.audio.AudioDelegate ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

public class UILayout extends UIElement
{
	private final ArrayList<UIElement> ordered = new ArrayList<UIElement>() ;	// Contains all UIElements - Buttons and Spacers
	private final UIElementUpdater updater ;

	public UILayout( final Type _type )
	{
		this( _type, new Vector3(), new Vector3(), new Vector3() ) ;
	}

	public UILayout( final Type _type, final Vector3 _length )
	{
		this( _type, new Vector3(), new Vector3(), _length ) ;
	}

	public UILayout( final Type _type, final Vector3 _offset, final Vector3 _length )
	{
		this( _type, new Vector3(), _offset, _length ) ;
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
		for( final UIElement element : ordered )
		{
			element.setInputAdapterInterface( getInputAdapter() ) ;
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
		_element.setInputAdapterInterface( getInputAdapter() ) ;
	}
	
	public void removeElement( final UIElement _element )
	{
		if( ordered.remove( _element ) == true )
		{
			_element.clear() ;
		}
	}

	@Override
	public void update( final float _dt, final ArrayList<Event<?>> _events )
	{
		if( isDirty() == true )
		{
			updater.update( _dt, ordered ) ;
		}

		super.update( _dt, _events ) ;

		for( final UIElement element : ordered )
		{
			element.update( _dt, _events ) ;
		}
	}

	@Override
	public void refresh()
	{
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			ordered.get( i ).makeDirty() ;
		}
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		for( final UIElement element : ordered )
		{
			if( element.passInputEvent( _event ) == InputEvent.Action.CONSUME )
			{
				return InputEvent.Action.CONSUME ;
			}
		}

		return InputEvent.Action.PROPAGATE ;
	}

	@Override
	public void shutdown() {}

	@Override
	public void clear()
	{
		super.clear() ;
		for( final UIElement element : ordered )
		{
			element.clear() ;
		}
		ordered.clear() ;
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
	}

	/**
		Order the UIElements from left to right along the x-axis.
		When the layouts length.x has been filled, drop down to the 
		next row, the next row starts at the maximum height of the 
		last row.
	*/
	private UIElementUpdater getVerticalUpdater()
	{
		return new UIElementUpdater()
		{
			private final Vector3 layoutPosition = new Vector3() ;
			private final Vector3 childPosition = new Vector3() ;

			public void update( final float _dt, final ArrayList<UIElement> _ordered )
			{
				final Vector3 availableLength = new Vector3() ;
				int minNumX = 0 ;
				int minNumY = 0 ;

				for( final UIElement element : _ordered )
				{
					final Vector3 minimum = element.getMinimumLength() ;
					minNumX += ( minimum.x <= 0.01f ) ? 1 : 0 ;
					minNumY += ( minimum.y <= 0.01f ) ? 1 : 0 ;

					availableLength.add( minimum ) ;
					availableLength.add( element.getMargin() ) ;
				}

				availableLength.x = UILayout.this.getLength().x ;
				availableLength.y = UILayout.this.getLength().y - availableLength.y ;

				final int size = _ordered.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _ordered.get( i ) ;
					final Vector3 minimum = element.getMinimumLength() ;

					float lenX = 0.0f ;
					float lenY = 0.0f ;

					if( minimum.x <= 0.01f )
					{
						lenX = availableLength.x ;
					}
					else
					{
					
					}

					if( minimum.y <= 0.01f )
					{
						lenY = availableLength.y / minNumY ;
					}
					else
					{
					
					}

					//System.out.println( "Vertical X: " + lenX + " Y: " + lenY ) ;
					element.setLength( lenX, lenY, 0.0f ) ;
				}

				calcAbsolutePosition( layoutPosition, UILayout.this ) ;
				childPosition.setXYZ( layoutPosition ) ;

				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _ordered.get( i ) ;
					final Vector3 length = element.getLength() ;
					final Vector3 margin = element.getMargin() ;

					if( UILayout.this.intersectPoint( childPosition.x + length.x, childPosition.y + length.y ) == true )
					{
						element.setPosition( childPosition.x, childPosition.y, childPosition.z ) ;
						childPosition.setXYZ( childPosition.x, childPosition.y + length.y + margin.y, layoutPosition.z ) ;
						continue ;
					}

					childPosition.setXYZ( childPosition.x + length.x + margin.x, layoutPosition.y, layoutPosition.z ) ;
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
	private UIElementUpdater getHorizontalUpdater()
	{
		return new UIElementUpdater()
		{
			private final Vector3 layoutPosition = new Vector3() ;
			private final Vector3 childPosition = new Vector3() ;

			public void update( final float _dt, final ArrayList<UIElement> _ordered )
			{
				final Vector3 availableLength = new Vector3() ;
				int minNumX = 0 ;
				int minNumY = 0 ;

				for( final UIElement element : _ordered )
				{
					final Vector3 minimum = element.getMinimumLength() ;
					minNumX += ( minimum.x <= 0.01f ) ? 1 : 0 ;
					minNumY += ( minimum.y <= 0.01f ) ? 1 : 0 ;

					availableLength.add( minimum ) ;
					availableLength.add( element.getMargin() ) ;
				}

				availableLength.x = UILayout.this.getLength().x - availableLength.x ;
				availableLength.y = UILayout.this.getLength().y ;

				final int size = _ordered.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _ordered.get( i ) ;
					final Vector3 minimum = element.getMinimumLength() ;

					float lenX = 0.0f ;
					float lenY = 0.0f ;

					if( minimum.x <= 0.01f )
					{
						lenX = availableLength.x / minNumX ;
					}
					else
					{
					
					}

					if( minimum.y <= 0.01f )
					{
						lenY = availableLength.y ;
					}
					else
					{
					
					}

					//System.out.println( "Horizontal X: " + lenX + " Y: " + lenY ) ;
					element.setLength( lenX, lenY, 0.0f ) ;
				}

				calcAbsolutePosition( layoutPosition, UILayout.this ) ;
				childPosition.setXYZ( layoutPosition ) ;

				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _ordered.get( i ) ;
					final Vector3 length = element.getLength() ;
					final Vector3 margin = element.getMargin() ;

					if( UILayout.this.intersectPoint( childPosition.x + length.x, childPosition.y + length.y ) == true )
					{
						element.setPosition( childPosition.x, childPosition.y, childPosition.z ) ;
						childPosition.setXYZ( childPosition.x + length.x + margin.x, childPosition.y, layoutPosition.z ) ;
						continue ;
					}

					childPosition.setXYZ( layoutPosition.x, childPosition.y + length.y + margin.y, layoutPosition.z ) ;
				}
			}
		} ;
	}

	private UIElementUpdater getGridUpdater()
	{
		return new UIElementUpdater()
		{
			public void update( final float _dt, final ArrayList<UIElement> _ordered )
			{

			}
		} ;
	}

	private UIElementUpdater getFormUpdater()
	{
		return new UIElementUpdater()
		{
			public void update( final float _dt, final ArrayList<UIElement> _ordered )
			{
			
			}
		} ;
	}

	private static void calcAbsolutePosition( final Vector3 _pos, final UIElement _element )
	{
		final Vector3 pos = _element.getPosition() ;
		final Vector3 offset = _element.getOffset() ;
		_pos.setXYZ( pos.x + offset.x, pos.y + offset.y, pos.z + offset.z ) ;
	}

	private static void calcAvailableLengthMissX( final ArrayList<UIElement> _elements, final Vector3 _total, final Vector3 _available )
	{
		_available.setXYZ( 0.0f, 0.0f, 0.0f ) ;
		for( final UIElement element : _elements )
		{
			final Vector3 margin = element.getMargin() ;
			final Vector3 minLen = element.getMinimumLength() ;
			//_available.x += margin.x + ( minLen != null ? minLen.x : 0.0f ) ;
			_available.y += margin.y + ( minLen != null ? minLen.y : 0.0f ) ;
			_available.z += margin.z + ( minLen != null ? minLen.z : 0.0f ) ;
		}

		_available.x = _total.x - _available.x ;
		_available.y = _total.y - _available.y ;
		_available.z = _total.z - _available.z ;
	}

	private static void calcAvailableLengthMissY( final ArrayList<UIElement> _elements, final Vector3 _total, final Vector3 _available )
	{
		_available.setXYZ( 0.0f, 0.0f, 0.0f ) ;
		for( final UIElement element : _elements )
		{
			final Vector3 margin = element.getMargin() ;
			final Vector3 minLen = element.getMinimumLength() ;
			_available.x += margin.x + ( minLen != null ? minLen.x : 0.0f ) ;
			//_available.y += margin.y + ( minLen != null ? minLen.y : 0.0f ) ;
			_available.z += margin.z + ( minLen != null ? minLen.z : 0.0f ) ;
		}

		_available.x = _total.x - _available.x ;
		_available.y = _total.y - _available.y ;
		_available.z = _total.z - _available.z ;
	}

	private static void updateLengthsMaxX( final ArrayList<UIElement> _elements, final Vector3 _totalLength )
	{
		final int num = _elements.size() ;
		final float lengthX = _totalLength.x /*/ num*/ ;
		final float lengthY = _totalLength.y / num ;
		final float lengthZ = _totalLength.z / num ;

		for( final UIElement element : _elements )
		{
			element.setLength( lengthX, lengthY, lengthZ ) ;
		}
	}

	private static void updateLengthsMaxY( final ArrayList<UIElement> _elements, final Vector3 _totalLength )
	{
		final int num = _elements.size() ;
		final float lengthX = _totalLength.x / num ;
		final float lengthY = _totalLength.y /*/ num*/ ;
		final float lengthZ = _totalLength.z / num ;

		for( final UIElement element : _elements )
		{
			element.setLength( lengthX, lengthY, lengthZ ) ;
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
		public void update( final float _dt, final ArrayList<UIElement> _ordered ) ;
	}
}
