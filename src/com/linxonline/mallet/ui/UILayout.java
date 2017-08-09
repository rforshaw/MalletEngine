package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.audio.AudioDelegate ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

public class UILayout extends UIElement
{
	private final List<UIElement> ordered = MalletList.<UIElement>newList() ;		// Layouts children
	private final List<UIElement> toRemove = MalletList.<UIElement>newList() ;		// UIElements to be removed from the layout.

	private Type type ;
	private final UIElementUpdater updater ;										// Used to position the layouts children

	private EngageListener engageMode = null ;										// Selection/Focus mode

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

		type = _type ;
		switch( type )
		{
			case HORIZONTAL : updater = getHorizontalUpdater() ; break ;
			case VERTICAL   : updater = getVerticalUpdater() ;   break ;
			case GRID       : updater = getGridUpdater() ;       break ;
			case FORM       : updater = getFormUpdater() ;       break ;
			default         : updater = getHorizontalUpdater() ; break ;
		}

		setEngageMode( new SingleEngageListener() ) ;
	}

	@Override
	public void passDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world, final Camera _camera )
	{
		super.passDrawDelegate( _delegate, _world, _camera ) ;
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			ordered.get( i ).passDrawDelegate( _delegate, _world, _camera ) ;
		}
	}

	/**
		Engaging a UILayout is somewhat redundant.
		You will most likely want to engage a child element 
		owned by the layout.
	*/
	@Override
	public void engage()
	{
		super.engage() ;
		current = State.CHILD_ENGAGED ;
	}

	/**
		Set the Engagement protocol that should be used 
		on the child elements of the layout.
		The layout's children will only receive inputs 
		if they are flagged as engaged. This listener 
		will allow the layout to change how it decides 
		elements are flagged as engaged.
	*/
	public void setEngageMode( final EngageListener _listener )
	{
		removeListener( engageMode ) ;
		engageMode = addListener( _listener ) ;
	}

	/**
		Add the passed in UIElement to the end of the UILayout.
		Returns the passed in element, allows for further modifications. 
	*/
	public <T extends UIElement> T addElement( final T _element )
	{
		if( ordered.contains( _element ) == false )
		{
			applyLayer( _element, getLayer() ) ;
			ordered.add( _element ) ;
		}
		return _element ; 
	}

	/**
		Populate the list with the UILayout's child elements.
		The elements returned are not copies.
	*/
	public void getElements( final List<UIElement> _elements )
	{
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			_elements.add( ordered.get( i ) ) ;
		}
	}

	/**
		Return the child elements attached to this 
		UILayout - should only be used if you are 
		extending this class.
	*/
	protected List<UIElement> getElements()
	{
		return ordered ;
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
	public void update( final float _dt, final List<Event<?>> _events )
	{
		if( isDirty() == true )
		{
			updater.update( _dt, ordered ) ;
		}

		super.update( _dt, _events ) ;

		{
			final int size = ordered.size() ;
			for( int i = 0; i < size; i++ )
			{
				final UIElement element = ordered.get( i ) ;
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
	}

	@Override
	public void setLayer( final int _layer )
	{
		super.setLayer( _layer ) ;
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			applyLayer( ordered.get( i ), _layer ) ;
		}
	}

	@Override
	public void setVisible( final boolean _visibility )
	{
		super.setVisible( _visibility ) ;
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			ordered.get( i ).setVisible( _visibility ) ;
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
		super.refresh() ;
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			final UIElement element = ordered.get( i ) ;
			if( element.isEngaged() == true )
			{
				// A UILayout will only pass input events to a child 
				// element if the element is flagged as engaged.
				// Use an engagement listener to determine this.
				if( element.passInputEvent( _event ) == InputEvent.Action.CONSUME )
				{
					// A child element may want to deny the other 
					// children the ability to process the InputEvent.
					// This means that the children's order is important.
					return InputEvent.Action.CONSUME ;
				}
			}
		}

		if( super.passInputEvent( _event ) == InputEvent.Action.CONSUME )
		{
			return InputEvent.Action.CONSUME ;
		}

		// If the UILayout or the children don't want t
		// consume the event then let it propagate.
		return InputEvent.Action.PROPAGATE ;
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

	public Type getType()
	{
		return type ;
	}

	/**
		Order the UIElements from top to bottom, filling up the space 
		provided. Multiple elements will share vertical space.
		Elements with minimum height set will be provided with it.
	*/
	protected UIElementUpdater getVerticalUpdater()
	{
		return new UIElementUpdater()
		{
			private final Vector3 layoutPosition = new Vector3() ;
			private final Vector3 childPosition = new Vector3() ;

			@Override
			public void update( final float _dt, final List<UIElement> _ordered )
			{
				final Vector3 availableLength = new Vector3() ;
				int minNumX = 0 ;
				int minNumY = 0 ;

				final int size = _ordered.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _ordered.get( i ) ;
					if( element.isVisible() == false )
					{
						// Don't take into account elements that 
						// are invisible.
						continue ;
					}

					final Vector3 minimum = element.getMinimumLength() ;
					minNumX += ( minimum.x <= 0.01f ) ? 1 : 0 ;
					minNumY += ( minimum.y <= 0.01f ) ? 1 : 0 ;

					// UI elements can specify a minimum length,
					// we need to accumulate this before we can 
					// calculate how much actual available length 
					// we have.

					availableLength.add( minimum ) ;
					availableLength.add( element.getMargin() ) ;
				}

				{
					// Available length currently holds the minimum 
					// amount required by the UILayout to display itself
					// with a resemblance of accuracy.
					final UIRatio ratio = getRatio() ;
					setMinimumLength( ratio.toUnitX( availableLength.x ),
									  ratio.toUnitY( availableLength.y ),
									  ratio.toUnitZ( availableLength.z ) ) ;
				}

				availableLength.x = UILayout.this.getLength().x ;
				availableLength.y = UILayout.this.getLength().y - availableLength.y ;

				calcAbsolutePosition( layoutPosition, UILayout.this ) ;
				childPosition.setXYZ( layoutPosition ) ;

				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _ordered.get( i ) ;
					if( element.isVisible() == false )
					{
						// Don't take into account elements that 
						// are invisible.
						continue ;
					}

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
					if( element.isVisible() == false )
					{
						// Don't take into account elements that 
						// are invisible.
						element.setLength( 0.0f, 0.0f, 0.0f ) ;
						continue ;
					}

					final Vector3 minimum = element.getMinimumLength() ;
					final UIRatio ratio = element.getRatio() ;

					float lenX = 0.0f ;
					float lenY = 0.0f ;

					if( minimum.x <= 0.01f )
					{
						// If minimum.x has not been set
						lenX = availableLength.x ;
					}

					if( minimum.y <= 0.01f )
					{
						// If minimum.y has not been set
						lenY = availableLength.y / minNumY ;
					}

					element.setLength( ratio.toUnitX( lenX ),
									   ratio.toUnitY( lenY ),
									   ratio.toUnitZ( 0.0f ) ) ;
					final Vector3 length = element.getLength() ;
					final Vector3 margin = element.getMargin() ;

					element.setPosition( ratio.toUnitX( childPosition.x ),
										 ratio.toUnitY( childPosition.y ),
										 ratio.toUnitZ( childPosition.z ) ) ;
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
	protected UIElementUpdater getHorizontalUpdater()
	{
		return new UIElementUpdater()
		{
			private final Vector3 layoutPosition = new Vector3() ;
			private final Vector3 childPosition = new Vector3() ;

			@Override
			public void update( final float _dt, final List<UIElement> _ordered )
			{
				final Vector3 availableLength = new Vector3() ;
				int minNumX = 0 ;
				int minNumY = 0 ;

				final int size = _ordered.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _ordered.get( i ) ;
					if( element.isVisible() == false )
					{
						// Don't take into account elements that 
						// are invisible.
						continue ;
					}

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
					if( element.isVisible() == false )
					{
						// Don't take into account elements that 
						// are invisible.
						continue ;
					}

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
					if( element.isVisible() == false )
					{
						// Don't take into account elements that 
						// are invisible.
						continue ;
					}

					final Vector3 minimum = element.getMinimumLength() ;
					final Vector3 maximum = element.getMaximumLength() ;
					final UIRatio ratio = element.getRatio() ;

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

					element.setLength( ratio.toUnitX( lenX ),
									   ratio.toUnitY( lenY ),
									   ratio.toUnitZ( 0.0f ) ) ;
					final Vector3 length = element.getLength() ;
					final Vector3 margin = element.getMargin() ;

					element.setPosition( ratio.toUnitX( childPosition.x ),
										 ratio.toUnitY( childPosition.y ),
										 ratio.toUnitZ( childPosition.z ) ) ;
					childPosition.setXYZ( childPosition.x + length.x + margin.x, childPosition.y, layoutPosition.z ) ;
				}
			}
		} ;
	}

	protected UIElementUpdater getGridUpdater()
	{
		return new UIElementUpdater()
		{
			@Override
			public void update( final float _dt, final List<UIElement> _ordered )
			{

			}
		} ;
	}

	protected UIElementUpdater getFormUpdater()
	{
		return new UIElementUpdater()
		{
			@Override
			public void update( final float _dt, final List<UIElement> _ordered )
			{
			
			}
		} ;
	}

	protected static void calcAbsolutePosition( final Vector3 _pos, final UIElement _element )
	{
		final Vector3 pos = _element.getPosition() ;
		final Vector3 offset = _element.getOffset() ;
		_pos.setXYZ( pos.x + offset.x, pos.y + offset.y, pos.z + offset.z ) ;
	}

	protected static void applyLayer( final UIElement _element, final int _layer )
	{
		if( _element.getLayer() < _layer )
		{
			// Child elements should always be a
			// layer above the parent.
			_element.setLayer( _layer + 1 ) ;
		}
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

	public static abstract class EngageListener extends InputListener<UILayout> {}

	/**
		Only allows one UIElement within the UILayout to 
		be enagaged at any one time.
		This implementation will eventually be modified to 
		enforce this rule when using keyboard inputs and 
		gamepad inputs, for now it will deal with mouse/touch inputs.
	*/
	public static class SingleEngageListener extends EngageListener
	{
		private UIElement currentEngaged = null ;
		private int currentIndex = 0 ;

		public SingleEngageListener() {}

		@Override
		public InputEvent.Action mouseMove( final InputEvent _input )
		{
			final UILayout layout = getParent() ;

			final int size = layout.ordered.size() ;
			for( int i = 0; i < size; i++ )
			{
				final UIElement element = layout.ordered.get( i ) ;
				if( element.isVisible() == true &&
					element.isEngaged() == false )
				{
					if( element.isIntersectInput( _input ) == true )
					{
						setCurrentEngaged( element, i ) ;
						break ;
					}
				}
			}

			disengageOthers( currentEngaged, layout.ordered ) ;
			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public InputEvent.Action mousePressed( final InputEvent _input )
		{
			return mouseMove( _input ) ;
		}

		@Override
		public InputEvent.Action mouseReleased( final InputEvent _input )
		{
			return mouseMove( _input ) ;
		}

		@Override
		public InputEvent.Action touchMove( final InputEvent _input )
		{
			return mouseMove( _input ) ;
		}

		@Override
		public InputEvent.Action touchPressed( final InputEvent _input )
		{
			return mouseMove( _input ) ;
		}

		@Override
		public InputEvent.Action touchReleased( final InputEvent _input )
		{
			return mouseMove( _input ) ;
		}

		@Override
		public InputEvent.Action keyReleased( final InputEvent _input )
		{
			final UILayout layout = getParent() ;
			final List<UIElement> elements = layout.ordered ;

			if( currentEngaged == null && currentIndex < elements.size() )
			{
				setCurrentEngaged( elements.get( currentIndex ), currentIndex ) ;
			}

			switch( _input.getKeyCode() )
			{
				case UP    :
				case DOWN  :
				case LEFT  :
				case RIGHT :
			}

			disengageOthers( currentEngaged, layout.ordered ) ;
			return InputEvent.Action.PROPAGATE ;
		}

		private void setCurrentEngaged( final UIElement _toEngage, final int _index )
		{
			if( _toEngage == null )
			{
				disengage( currentEngaged ) ;
				currentEngaged = null ;
				currentIndex = _index ;
				return ;
			}

			if( currentEngaged != _toEngage )
			{
				disengage( currentEngaged ) ;

				currentEngaged = _toEngage ;
				currentEngaged.engage() ;
				currentIndex = _index ;
			}
		}

		private static void disengageOthers( final UIElement _current, final List<UIElement> _others )
		{
			final int size = _others.size() ;
			for( int i = 0; i < size; i++ )
			{
				final UIElement element = _others.get( i ) ;
				if( element != _current &&
					element.isEngaged() == true )
				{
					// Only disengage the elements that were previously
					// engaged and are not the currently engaged element.
					element.disengage() ;
				}
			}
		}

		private static void disengage( final UIElement _element )
		{
			if( _element != null )
			{
				_element.disengage() ;
			}
		}
	}

	protected interface UIElementUpdater
	{
		public void update( final float _dt, final List<UIElement> _ordered ) ;
	}
}
