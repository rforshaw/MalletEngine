package com.linxonline.mallet.ui ;

import java.util.ArrayList ;

import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.audio.AudioDelegate ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

public class UILayout extends UIElement
{
	private final ArrayList<UIElement> ordered = new ArrayList<UIElement>() ;
	private final ArrayList<UIElement> toRemove = new ArrayList<UIElement>() ;
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
			default         : updater = getHorizontalUpdater() ; break ;
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

	public <T extends UIElement> T addElement( final T _element )
	{
		if( ordered.contains( _element ) == false )
		{
			ordered.add( _element ) ;
			_element.setInputAdapterInterface( getInputAdapter() ) ;
		}
		return _element ; 
	}

	/**
		Flag an element to be removed from the UILayout.
		Element will be removed on the next update cycle.
	*/
	public void removeElement( final UIElement _element )
	{
		if( toRemove.contains( _element ) == false )
		{
			toRemove.add( _element ) ;
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
			if( element.destroy == true )
			{
				// If the child element is flagged for 
				// destruction add it to the remove list.
				removeElement( element ) ;

				// We'll also want to refresh the UILayout.
				makeDirty() ;
			}
			else if( element.isDirty() == true )
			{
				// If a Child element is updating we'll 
				// most likely also want to update the parent.
				makeDirty() ;
			}
		}

		for( final UIElement element : toRemove )
		{
			if( ordered.remove( element ) == true )
			{
				element.shutdown() ;
				element.clear() ;
			}
		}
		toRemove.clear() ;
	}

	@Override
	public void refresh()
	{
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			ordered.get( i ).makeDirty() ;
		}
		super.refresh() ;
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

		return super.passInputEvent( _event ) ;
	}

	/**
		Cleanup any resources, handlers that the listeners 
		may have acquired.
	*/
	@Override
	public void shutdown()
	{
		super.shutdown() ;
		for( final UIElement element : ordered )
		{
			element.shutdown() ;
		}
		ordered.clear() ;
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
		Order the UIElements from top to bottom, filling up the space 
		provided. Multiple elements will share vertical space.
		Elements with minimum height set will be provided with it.
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

				final int size = _ordered.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _ordered.get( i ) ;
					final Vector3 minimum = element.getMinimumLength() ;
					minNumX += ( minimum.x <= 0.01f ) ? 1 : 0 ;
					minNumY += ( minimum.y <= 0.01f ) ? 1 : 0 ;

					availableLength.add( minimum ) ;
					availableLength.add( element.getMargin() ) ;
				}

				availableLength.x = UILayout.this.getLength().x ;
				availableLength.y = UILayout.this.getLength().y - availableLength.y ;

				calcAbsolutePosition( layoutPosition, UILayout.this ) ;
				childPosition.setXYZ( layoutPosition ) ;

				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _ordered.get( i ) ;
					final Vector3 maximum = element.getMaximumLength() ;

					// If the length allocated to this element is greater 
					// than the maximum length, then we must remove the 
					// element from the average minNumY and remove maximum.y 
					// from available length.

					if( maximum.y > 0.0f )
					{
						final float lenY = availableLength.y / minNumY ;
						if( lenY > maximum.y )
						{
							minNumY -= 1 ;
							availableLength.y -= maximum.y ;
						}
					}
				}

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

					if( minimum.y <= 0.01f )
					{
						lenY = availableLength.y / minNumY ;
					}
					
					element.setLength( lenX, lenY, 0.0f ) ;
					final Vector3 length = element.getLength() ;
					final Vector3 margin = element.getMargin() ;

					element.setPosition( childPosition.x, childPosition.y, childPosition.z ) ;
					childPosition.setXYZ( childPosition.x, childPosition.y + length.y + margin.y, layoutPosition.z ) ;
				}
			}
		} ;
	}

	/**
		Order the UIElements from left to right, filling up the space 
		provided. Multiple elements will share horizontal space.
		Elements with minimum width set will be provided with it.
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

				final int size = _ordered.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _ordered.get( i ) ;
					final Vector3 minimum = element.getMinimumLength() ;
					minNumX += ( minimum.x <= 0.01f ) ? 1 : 0 ;
					minNumY += ( minimum.y <= 0.01f ) ? 1 : 0 ;

					availableLength.add( minimum ) ;
					availableLength.add( element.getMargin() ) ;
				}

				availableLength.x = UILayout.this.getLength().x - availableLength.x ;
				availableLength.y = UILayout.this.getLength().y ;

				calcAbsolutePosition( layoutPosition, UILayout.this ) ;
				childPosition.setXYZ( layoutPosition ) ;

				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _ordered.get( i ) ;
					final Vector3 maximum = element.getMaximumLength() ;
					
					// If the length allocated to this element is greater 
					// than the maximum length, then we must remove the 
					// element from the average minNumY and remove maximum.y 
					// from available length.

					if( maximum.x > 0.0f )
					{
						final float lenX = availableLength.x / minNumX ;
						if( lenX > maximum.x )
						{
							minNumX -= 1 ;
							availableLength.x -= maximum.x ;
						}
					}
				}

				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _ordered.get( i ) ;
					final Vector3 minimum = element.getMinimumLength() ;
					final Vector3 maximum = element.getMaximumLength() ;

					float lenX = 0.0f ;
					float lenY = 0.0f ;

					if( minimum.x <= 0.01f )
					{
						lenX = availableLength.x / minNumX ;
					}

					if( minimum.y <= 0.01f )
					{
						lenY = availableLength.y ;
					}

					element.setLength( lenX, lenY, 0.0f ) ;
					final Vector3 length = element.getLength() ;
					final Vector3 margin = element.getMargin() ;

					element.setPosition( childPosition.x, childPosition.y, childPosition.z ) ;
					childPosition.setXYZ( childPosition.x + length.x + margin.x, childPosition.y, layoutPosition.z ) ;
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

	public enum Type
	{
		HORIZONTAL,
		VERTICAL,
		GRID,
		FORM ;

		public static Type derive( final String _type )
		{
			if( _type == null )
			{
				return HORIZONTAL ;
			}
			
			if( _type.isEmpty() == true )
			{
				return HORIZONTAL ;
			}

			return Type.valueOf( _type ) ;
		}
	}

	private interface UIElementUpdater
	{
		public void update( final float _dt, final ArrayList<UIElement> _ordered ) ;
	}
}
