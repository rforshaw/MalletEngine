package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.audio.AudioDelegate ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

/**
	Core class of the UI Framework.

	Supports child elements in vertical or horizontal 
	layouts - children will be populated to fill out as much 
	space as possible.

	Children with minimum requirements will be given that space.
	Children with maximum requirements will not be given more 
	than what they have requested. 

	UILayout will update its minimum length to 
	represent the total minimum length of all of its children.

	Supports child elements in Grid layouts - children will 
	be set to the dimensions of the first element with a valid 
	minimum or maximum length. Elements will be displayed 
	left to right, once there is no more width it will drop 
	down to the next row.
*/
public class UILayout extends UIElement implements IChildren
{
	private final UIChildren children = new UIChildren() ;
	private ILayout.Type type ;								// How children should be ordered
	private ILayout updater ;								// Used to position the children

	// It's very likely that a child of the layout will be flagged 
	// as dirty - this may result in the other children of the 
	// layout being impacted - for example if a minimum length has 
	// changed, however, that change will not affect the dimensions 
	// of the layout - so we want the layout to readjust itself but the 
	// layout does not need to inform its parent - which would cause a 
	// chain reaction of almost everything updating.
	private boolean dirtyChildren = false ;											// Used to determine if the children are dirty

	private EngageComponent engageMode = null ;										// Selection/Focus mode

	private final Connect.Signal typeChanged = new Connect.Signal() ;

	public UILayout( final ILayout.Type _type )
	{
		super() ;
		initLayoutConnections() ;

		setType( _type ) ;
		setEngageMode( new SingleEngageComponent( this ) ) ;
	}

	private void initLayoutConnections()
	{
		/**
			Engaging a UILayout is somewhat redundant.
			You will most likely want to engage a child element 
			owned by the layout.
		*/
		UIElement.connect( this, elementEngaged(), new Connect.Slot<UILayout>()
		{
			@Override
			public void slot( final UILayout _this )
			{
				current = State.CHILD_ENGAGED ;
			}
		} ) ;

		UIElement.connect( this, elementDisengaged(), new Connect.Slot<UILayout>()
		{
			@Override
			public void slot( final UILayout _this )
			{
				_this.children.disengage() ;
			}
		} ) ;

		/**
			Set the layer that the visual elements are 
			expected to be placed on.
			
			This also applies to the layouts children.
			Causes the elements to be flagged as dirty.
		*/
		UIElement.connect( this, layerChanged(), new Connect.Slot<UILayout>()
		{
			@Override
			public void slot( final UILayout _this )
			{
				_this.children.setLayer( getLayer() ) ;
			}
		} ) ;

		UIElement.connect( this, elementShown(), new Connect.Slot<UILayout>()
		{
			@Override
			public void slot( final UILayout _this )
			{
				_this.children.setVisible( true ) ;
			}
		} ) ;

		UIElement.connect( this, elementHidden(), new Connect.Slot<UILayout>()
		{
			@Override
			public void slot( final UILayout _this )
			{
				_this.children.setVisible( false ) ;
			}
		} ) ;

		UIElement.connect( this, elementDestroyed(), new Connect.Slot<UILayout>()
		{
			@Override
			public void slot( final UILayout _this )
			{
				children.destroy() ;
			}
		} ) ;

		/**
			Cleanup any resources, handlers that the listeners 
			may have acquired.

			Will also call shutdown on all children. Call clear 
			if you wish to also remove all children from layout.
		*/
		UIElement.connect( this, elementShutdown(), new Connect.Slot<UILayout>()
		{
			@Override
			public void slot( final UILayout _this )
			{
				children.shutdown() ;
			}
		} ) ;

		/**
			Clear out each of the systems.
			Remove all slots connected to signals.
			Remove all listeners - note call shutdown if they have 
			any resources attached.
			Remove any events that may be in the event stream.
		*/
		UIElement.connect( this, elementClear(), new Connect.Slot<UILayout>()
		{
			@Override
			public void slot( final UILayout _this )
			{
				children.clear() ;
			}
		} ) ;

		/**
			Reset the UILayout as if it has just been constructed.
			This does not remove listeners, connections or children.

			Call reset on all children.
		*/
		UIElement.connect( this, elementReset(), new Connect.Slot<UILayout>()
		{
			@Override
			public void slot( final UILayout _this )
			{
				children.reset() ;
			}
		} ) ;
	}

	@Override
	public void setWorldAndCamera( final World _world, final Camera _camera )
	{
		super.setWorldAndCamera( _world, _camera ) ;
		children.setWorldAndCamera( _world, _camera ) ;
	}

	/**
		Set the Engagement protocol that should be used 
		on the child elements of the layout.
		The layout's children will only receive inputs 
		if they are flagged as engaged. This listener 
		will allow the layout to change how it decides 
		elements are flagged as engaged.
	*/
	public void setEngageMode( final EngageComponent _listener )
	{
		if( engageMode != null )
		{
			engageMode.destroy() ;
		}
		engageMode = _listener ;
	}

	public EngageComponent getEngageMode()
	{
		return engageMode ;
	}

	/**
		Add the passed in UIElement to the end of the UILayout.
		Returns the passed in element, allows for further modifications. 
	*/
	@Override
	public <T extends UIElement> T addElement( final T _element )
	{
		return addElement( children.size(), _element ) ;
	}

	@Override
	public <T extends UIElement> T addElement( final int _index, final T _element )
	{
		final UIElement element = children.addElement( _index, _element ) ;
		if( element != null )
		{
			applyLayer( element, getLayer() ) ;

			final World world = getWorld() ;
			if( world != null )
			{
				element.setWorldAndCamera( world, getCamera() ) ;
			}
		}
		return _element ; 
	}

	public List<UIElement> getElements()
	{
		return children.getElements() ;
	}

	/**
		Populate the list with the UILayout's child elements.
		The elements returned are not copies.
	*/
	public void getElements( final List<UIElement> _elements )
	{
		children.getElements( _elements ) ;
	}

	/**
		Flag an element to be removed from the UILayout.
		Element will be removed on the next update cycle.
	*/
	public void removeElement( final UIElement _element )
	{
		children.removeElement( _element ) ;
	}

	@Override
	public void update( final float _dt, final List<Event<?>> _events )
	{
		final boolean dirt = isDirty() ;
		super.update( _dt, _events ) ;

		if( dirt == true || dirtyChildren == true )
		{
			dirtyChildren = false ;
			updater.update( _dt, this ) ;
		}

		dirtyChildren = children.update( _dt, _events ) ;
	}

	public void setType( final ILayout.Type _type )
	{
		if( type != _type  )
		{
			type = _type ;
			switch( type )
			{
				case HORIZONTAL : updater = getHorizontalUpdater() ; break ;
				case VERTICAL   : updater = getVerticalUpdater() ;   break ;
				case GRID       : updater = getGridUpdater() ;       break ;
				case FORM       : updater = getFormUpdater() ;       break ;
				default         : updater = getHorizontalUpdater() ; break ;
			}
			UILayout.signal( this, typeChanged() ) ;
		}
	}

	/**
		Refreshing a UILayout will most likely 
		result in all child elements requiring to be 
		refreshed.

		Flag the child elements as dirty and during 
		the next update cycle they will be refreshed.
	*/
	@Override
	protected void refresh()
	{
		children.refresh() ;
		super.refresh() ;
	}

	/**
		Check to see if the InputEvent intersects with 
		either the UILayout or one of its children.

		We check the children as there is a chance that 
		the child is beyond the UILayout boundaries, for 
		example a dropdown menu.
	*/
	@Override
	public boolean isIntersectInput( final InputEvent _event )
	{
		if( super.isIntersectInput( _event ) == true )
		{
			return true ;
		}

		// There is a chance that a layout has children 
		// that go beyond the layouts boundaries.
		// For example UIMenu dropdown.
		return children.isIntersectInput( _event ) != null ;
	}

	protected ILayout getCurrentUpdater()
	{
		return updater ;
	}

	public ILayout.Type getType()
	{
		return type ;
	}

	/**
		Called when the type of the layout 
		has been changed using setType().
		This defines how child elements are laid out.
	*/
	public Connect.Signal typeChanged()
	{
		return typeChanged ;
	}

	/**
		Order the UIElements from top to bottom, filling up the space 
		provided. Multiple elements will share vertical space.
		Elements with minimum height set will be provided with it.
	*/
	protected ILayout getVerticalUpdater()
	{
		return new ILayout()
		{
			private final List<UIElement> ordered = MalletList.<UIElement>newList() ;
			private final Vector3 layoutPosition = new Vector3() ;
			private final Vector3 childPosition = new Vector3() ;

			@Override
			public void update( final float _dt, final IChildren _children )
			{
				ordered.clear() ;
				_children.getElements( ordered ) ;

				final Vector3 availableLength = new Vector3() ;
				int minNumX = 0 ;
				int minNumY = 0 ;

				final int size = ordered.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = ordered.get( i ) ;
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

				availableLength.x = UILayout.this.getLength().x ;
				availableLength.y = UILayout.this.getLength().y - availableLength.y ;

				calcAbsolutePosition( layoutPosition, UILayout.this ) ;
				childPosition.setXYZ( layoutPosition ) ;

				for( int i = 0; i < size; i++ )
				{
					final UIElement element = ordered.get( i ) ;
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
					final UIElement element = ordered.get( i ) ;
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
	protected ILayout getHorizontalUpdater()
	{
		return new ILayout()
		{
			private final List<UIElement> ordered = MalletList.<UIElement>newList() ;
			private final Vector3 layoutPosition = new Vector3() ;
			private final Vector3 childPosition = new Vector3() ;

			@Override
			public void update( final float _dt, final IChildren _children )
			{
				ordered.clear() ;
				_children.getElements( ordered ) ;

				final Vector3 availableLength = new Vector3() ;
				int minNumX = 0 ;
				int minNumY = 0 ;

				final int size = ordered.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = ordered.get( i ) ;
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
					final UIElement element = ordered.get( i ) ;
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
					final UIElement element = ordered.get( i ) ;
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

	/**
		Order the UIElements from left to right, setting their size to 
		the minimum or maximim length of the element with a defined 
		minimum or maximum value.
	*/
	protected ILayout getGridUpdater()
	{
		return new ILayout()
		{
			private final List<UIElement> ordered = MalletList.<UIElement>newList() ;
			private final Vector3 layoutPosition = new Vector3() ;
			private final Vector3 childPosition = new Vector3() ;

			@Override
			public void update( final float _dt, final IChildren _children )
			{
				ordered.clear() ;
				_children.getElements( ordered ) ;
				final UIElement reference = getReferenceElement( ordered ) ;
				if( reference == null )
				{
					Logger.println( "No valid minimum or maximum length for grid.", Logger.Verbosity.NORMAL ) ;
					return ;
				}

				final Vector3 length = reference.getLength() ;
				final Vector3 margin = reference.getMargin() ;

				final Vector3 layoutLength = UILayout.this.getLength() ;

				calcAbsolutePosition( layoutPosition, UILayout.this ) ;
				childPosition.setXYZ( layoutPosition ) ;
				childPosition.add( margin ) ;

				final float layoutWidth = layoutPosition.x + layoutLength.x ;
				
				final int size = ordered.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = ordered.get( i ) ;
					if( element.isVisible() == false )
					{
						// Don't take into account elements that 
						// are invisible.
						continue ;
					}

					final UIRatio ratio = element.getRatio() ;
					element.setLength( ratio.toUnitX( length.x ),
									   ratio.toUnitY( length.y ),
									   ratio.toUnitZ( length.z ) ) ;

					if( ( childPosition.x + length.x + margin.x ) > layoutWidth )
					{
						final float y = childPosition.y + length.y + margin.y ;
						childPosition.setXYZ( layoutPosition.x + margin.x, y, childPosition.z ) ;
					}

					element.setPosition( ratio.toUnitX( childPosition.x ),
										 ratio.toUnitY( childPosition.y ),
										 ratio.toUnitZ( childPosition.z ) ) ;

					final float x = childPosition.x + length.x + margin.x ;
					childPosition.setXYZ( x, childPosition.y, childPosition.z ) ;
				}
			}

			private final UIElement getReferenceElement( final List<UIElement> _ordered )
			{
				final int size = _ordered.size() ;
				for( int i = 0; i < size; i++ )
				{
					final UIElement element = _ordered.get( i ) ;
					final Vector3 min = element.getMinimumLength() ;
					if( min.x > 0.0f && min.y > 0.0f )
					{
						return element ;
					}

					final Vector3 max = element.getMinimumLength() ;
					if( max.x > 0.0f && max.y > 0.0f )
					{
						return element ;
					}
				}

				return null ;
			}
		} ;
	}

	protected ILayout getFormUpdater()
	{
		return new ILayout()
		{
			@Override
			public void update( final float _dt, final IChildren _children )
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
		if( _element.getLayer() <= _layer )
		{
			// Child elements should always be a
			// layer above the parent.
			_element.setLayer( _layer + 1 ) ;
		}
	}

	public static <T extends UILayout> T applyMeta( final UILayout.Meta _meta, final T _layout )
	{
		return UIElement.applyMeta( _meta, _layout ) ;
	}

	public static abstract class EngageComponent extends InputComponent
	{
		public EngageComponent( final UILayout _parent )
		{
			super( _parent ) ;
		}
	
		public abstract boolean isEngaged() ;

		public abstract UIElement getEngaged() ;
	}

	/**
		Only allows one UIElement within the UILayout to 
		be enagaged at any one time.
		This implementation will eventually be modified to 
		enforce this rule when using keyboard inputs and 
		gamepad inputs, for now it will deal with mouse/touch inputs.
	*/
	public static class SingleEngageComponent extends EngageComponent
	{
		private UIElement currentEngaged = null ;

		public SingleEngageComponent( final UILayout _parent )
		{
			super( _parent ) ;
			UIElement.connect( _parent, _parent.elementDisengaged(), ( final UILayout _layout ) ->
			{
				// If the layout has been disengaged then it is 
				// safe to say that all children of the layout 
				// should also be disengaged.
				setEngaged( null ) ;
				final List<UIElement> ordered = _layout.getElements() ;
				disengageOthers( null, ordered ) ;
			} ) ;
		}

		@Override
		public InputEvent.Action mouseMove( final InputEvent _input )
		{
			final UILayout layout = getParentLayout() ;
			final UIElement current = getEngaged() ;

			if( current != null )
			{
				if( current.isVisible() == true &&
					current.isDisabled() == false &&
					current.destroy == false )
				{
					if( current.isIntersectInput( _input ) == true )
					{
						return passInput( current, _input ) ;
					}
				}
			}

			setEngaged( null ) ;
			final List<UIElement> ordered = layout.getElements() ;
			disengageOthers( null, ordered ) ;

			final int size = ordered.size() ;
			for( int i = 0; i < size; i++ )
			{
				final UIElement element = ordered.get( i ) ;
				if( element.isVisible() == true )
				{
					if( element.isIntersectInput( _input ) == true )
					{
						element.engage() ;
						setEngaged( element.isDisabled() ? null : element ) ;
						disengageOthers( getEngaged(), ordered ) ;

						return passInput( element, _input ) ;
					}
				}
			}

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
		public InputEvent.Action keyPressed( final InputEvent _input )
		{
			return passInput( getEngaged(), _input ) ;
		}

		@Override
		public InputEvent.Action scroll( final InputEvent _input )
		{
			return passInput( getEngaged(), _input ) ;
		}

		@Override
		public InputEvent.Action keyReleased( final InputEvent _input )
		{
			return passInput( getEngaged(), _input ) ;
		}

		private InputEvent.Action passInput( final UIElement _current, final InputEvent _input )
		{
			return ( _current != null ) ? _current.passInputEvent( _input ) : InputEvent.Action.PROPAGATE ;
		}

		public void setEngaged( final UIElement _toEngage )
		{
			currentEngaged = _toEngage ;
		}

		public UIElement getEngaged()
		{
			return currentEngaged ;
		}
		
		public boolean isEngaged()
		{
			return getEngaged() != null ;
		}

		public UILayout getParentLayout()
		{
			return ( UILayout )getParent() ;
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
	}

	public static class Meta extends UIElement.Meta
	{
		private final UIVariant type = new UIVariant( "LAYOUT", ILayout.Type.VERTICAL, new Connect.Signal() ) ;

		public Meta()
		{
			super() ;
			int row = rowCount( root() ) ;
			createData( null, row + 1, 1 ) ;
			setData( new UIModelIndex( root(), row++, 0 ), type, UIAbstractModel.Role.User ) ;
		}

		@Override
		public String getElementType()
		{
			return "UILAYOUT" ;
		}

		@Override
		public boolean supportsChildren()
		{
			return true ;
		}

		public void setType( final ILayout.Type _type )
		{
			if( _type.equals( type.toObject() ) == false )
			{
				type.setObject( _type ) ;
				UIElement.signal( this, type.getSignal() ) ;
			}
		}

		public ILayout.Type getType()
		{
			return type.toObject( ILayout.Type.class ) ;
		}

		public Connect.Signal typeChanged()
		{
			return type.getSignal() ;
		}
	}
}
