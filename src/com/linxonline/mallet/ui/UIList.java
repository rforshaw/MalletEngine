package com.linxonline.mallet.ui ;

import java.util.UUID ;
import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

public class UIList extends UILayout
{
	private final FrameBuffer frame ;
	private World externalWorld ;
	private Camera externalCamera = CameraAssist.getDefault() ;
	private DrawUpdater updater = null ;

	private final Vector3 defaultItemSize = new Vector3() ;		// In pixels
	private final Vector3 scrollbarLength = new Vector3() ;
	private final Vector3 scrollWidth = new Vector3() ;

	// Used to define the boundaries of the scroll
	private final Vector3 absoluteLength = new Vector3() ;		// In pixels
	private int dragDelay = 100 ;

	private final Connect.Signal defaultElementSizeChanged = new Connect.Signal() ;
	private final Connect.Signal scrollWidthChanged        = new Connect.Signal() ;
	private final Connect.Signal dragDelayChanged          = new Connect.Signal() ;

	public UIList( final ILayout.Type _type )
	{
		super( _type ) ;
		setDefaultElementSize( 1.0f, 1.0f, 1.0f ) ;
		setScrollWidth( 0.5f ) ;

		final Vector3 position = getPosition() ;
		final Vector3 offset = getOffset() ;
		final Vector3 length = getLength() ;

		frame = new FrameBuffer( position.x, position.y, position.z,
								 offset.x,   offset.y,   offset.z,
								 length.x,   length.y ) ;
		UIList.this.setListWorldAndCamera( frame.getWorld(), frame.getCamera() ) ;

		UIElement.connect( this, elementShutdown(), new Connect.Slot<UIList>()
		{
			@Override
			public void slot( final UIList _this )
			{
				if( updater != null )
				{
					updater.removeDynamics( frame.getFrame() ) ;
				}
				frame.shutdown() ;
			}
		} ) ;

		UIElement.connect( this, layerChanged(), new Connect.Slot<UIElement>()
		{
			@Override
			public void slot( final UIElement _this )
			{
				if( updater != null )
				{
					updater.removeDynamics( frame.getFrame() ) ;
				}

				if( externalWorld != null )
				{
					final int layer = _this.getLayer() + 1 ;
					final Program program = frame.getProgram() ;
					final Draw draw = frame.getFrame() ;

					final DrawUpdater updater = DrawUpdater.getOrCreate( externalWorld, program, draw.getShape(), true, layer ) ;
					updater.addDynamics( draw ) ;
				}
			}
		} ) ;

		UIElement.connect( this, positionChanged(), new Connect.Slot<UIElement>()
		{
			@Override
			public void slot( final UIElement _this )
			{
				final Vector3 position = _this.getPosition() ;
				frame.setPosition( position.x, position.y, position.z ) ;
				updater.forceUpdate() ;
			}
		} ) ;

		UIElement.connect( this, offsetChanged(), new Connect.Slot<UIElement>()
		{
			@Override
			public void slot( final UIElement _this )
			{
				final Vector3 offset = _this.getOffset() ;
				frame.setOffset( offset.x, offset.y, offset.z ) ;
				updater.forceUpdate() ;
			}
		} ) ;

		UIElement.connect( this, lengthChanged(), new Connect.Slot<UIElement>()
		{
			@Override
			public void slot( final UIElement _this )
			{
				final Vector3 length = _this.getLength() ;
				frame.setLength( length.x, length.y, length.z ) ;
				updater.forceUpdate() ;
			}
		} ) ;

		new ScrollInputComponent( this ) ;
		setEngageMode( new ScrollSingleEngageComponent( this ) ) ;
	}

	/**
		Add the passed in UIElement to the end of the UILayout.
		Returns the passed in element, allows for further modifications. 
	*/
	@Override
	public <T extends UIElement> T addElement( final T _element )
	{
		final List<UIElement> ordered = getElements() ;
		if( ordered.contains( _element ) == false )
		{
			applyLayer( _element, getLayer() ) ;
			ordered.add( _element ) ;

			_element.setWorldAndCamera( getInternalWorld(), getInternalCamera() ) ;
		}
		return _element ; 
	}

	/**
		If any elements added to this list do not have a minimum or 
		maximum length defined then this value will be used instead.
		Values are expected to be the unit type defined by UIRatio.
		If the UIElement is not overidden the default value, Global UI Ratio 
		is used.
	*/
	public void setDefaultElementSize( final float _x, final float _y, final float _z )
	{
		final UIRatio ratio = getRatio() ;
		defaultItemSize.x = ( _x <= 0.0f ) ? ratio.toPixelX( 1.0f ) : ratio.toPixelX( _x ) ;
		defaultItemSize.y = ( _y <= 0.0f ) ? ratio.toPixelX( 1.0f ) : ratio.toPixelY( _y ) ;
		defaultItemSize.z = ( _z <= 0.0f ) ? ratio.toPixelX( 1.0f ) : ratio.toPixelZ( _z ) ;
		UIElement.signal( this, defaultElementSizeChanged() ) ;
	}

	/**
		Defines the basic dimensions of the scrollbar.
	*/
	public void setScrollWidth( final float _width )
	{
		final UIRatio ratio = getRatio() ;
		scrollWidth.x = ( _width <= 0.0f ) ? ratio.toPixelX( 1.0f ) : ratio.toPixelX( _width ) ;
		scrollWidth.y = ( _width <= 0.0f ) ? ratio.toPixelX( 1.0f ) : ratio.toPixelY( _width ) ;
		scrollWidth.z = ( _width <= 0.0f ) ? ratio.toPixelX( 1.0f ) : ratio.toPixelZ( _width ) ;
		UIElement.signal( this, scrollWidthChanged() ) ;
	}

	/**
		Define how long the delay can be between the user 
		pressing down and moving that could trigger scrolling.
	*/
	public void setDragDelay( final int _delay )
	{
		if( dragDelay != _delay )
		{
			dragDelay = _delay ;
			UIElement.signal( this, dragDelayChanged() ) ;
		}
	}

	/**
		Return how long the delay can be between the user 
		pressing down and moving that could trigger scrolling.
	*/
	public int getDragDelay()
	{
		return dragDelay ;
	}

	@Override
	public void setWorldAndCamera( final World _world, final Camera _camera )
	{
		externalWorld = _world ;
		externalCamera = ( _camera != null ) ? _camera : externalCamera ;

		final List<UIElement.Component> base = getComponentUnit().getComponents() ;
		final int size = base.size() ;
		for( int i = 0; i < size; i++ )
		{
			base.get( i ).setWorld( externalWorld ) ;
		}

		final Program program = frame.getProgram() ;
		final Draw draw = frame.getFrame() ;

		// Though the UIList will give its children its 
		// own DrawDelegate the UIList will give the 
		// DrawDelegate passed in here the Draw pane.
		updater = DrawUpdater.getOrCreate( externalWorld, program, draw.getShape(), true, getLayer() + 1 ) ;
		updater.addDynamics( draw ) ;
	}

	private void setListWorldAndCamera( final World _world, final Camera _camera )
	{
		final List<UIElement> ordered = getElements() ;

		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			ordered.get( i ).setWorldAndCamera( _world, _camera ) ;
		}
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		switch( _event.getInputType() )
		{
			case SCROLL_WHEEL      :
			case KEYBOARD_PRESSED  :
			case KEYBOARD_RELEASED : return processInputEvent( _event ) ;
			default                :
			{
				if( isIntersectInput( _event ) == false )
				{
					// A UIElement should only pass the InputEvent 
					// to its listeners if the input is intersecting 
					// else we run the risk of doing pointless processing.
					return InputEvent.Action.PROPAGATE ;
				}

				final Vector3 position = getPosition() ;
				final Vector3 offset = getOffset() ;

				final InputEvent input = new InputEvent( _event ) ;
				final float x = getCamera().convertInputToUIX( input.getMouseX() ) - ( position.x + offset.x ) ;
				final float y = getCamera().convertInputToUIY( input.getMouseY() ) - ( position.y + offset.y ) ;
				input.setInput( input.getInputType(), ( int )x, ( int )y ) ;

				return processInputEvent( input ) ;
			}
		}
	}

	/**
		Override the parent implementation coming from UILayout.
		UILayout tests all children to determine whether or not
		the input intersects, this is to cover the case on a 
		child extending beyond the UILayout.
		
		Children of a UIList are in a different co-ordinate space
		and so passing them an unconverted input will cause 
		problems. For now UIList only checks itself.
	*/
	@Override
	public boolean isIntersectInput( final InputEvent _event )
	{
		if( super.isIntersectInput( _event, getCamera() ) == true )
		{
			return true ;
		}

		return false ;
	}

	/**
		Order the UIElements from top to bottom.
		If the element's length is greater than 0 then 
		it will be given that space.

		If the element does not have a valid length then 
		minimum length will be used instead.

		If the element does not have a valid minimum length, 
		then maximum will be used.

		If non of the above provide a valid length then the 
		element's length will be set to default item size. 
	*/
	@Override
	protected ILayout getVerticalUpdater()
	{
		return new ILayout()
		{
			private final List<UIElement> ordered = MalletList.<UIElement>newList() ;
			private final Vector3 childPosition = new Vector3() ;

			@Override
			public void update( final float _dt, final IChildren _children )
			{
				ordered.clear() ;
				_children.getElements( ordered ) ;

				final Vector3 listLength = UIList.this.getLength() ;
				final Vector3 listMargin = UIList.this.getMargin() ;

				final float width = listLength.x - ( scrollWidth.x + ( listMargin.x * 2.0f ) ) ;
				absoluteLength.setXYZ( width, 0.0f, listLength.z ) ;
				childPosition.setXYZ( scrollWidth.x, 0.0f, 0.0f ) ;

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
					final Vector3 length = element.getLength() ;
					final Vector3 margin = element.getMargin() ;

					final Vector3 maximum = element.getMaximumLength() ;
					if( maximum.y > 0.0f )
					{
						element.setLength( ratio.toUnitX( absoluteLength.x ),
										   ratio.toUnitY( maximum.y ),
										   ratio.toUnitZ( absoluteLength.z ) ) ;
					}
					else
					{
						element.setLength( ratio.toUnitX( absoluteLength.x ),
											ratio.toUnitY( defaultItemSize.y ),
											ratio.toUnitZ( absoluteLength.z ) ) ;
					}

					element.setPosition( ratio.toUnitX( childPosition.x ),
										 ratio.toUnitY( childPosition.y ),
										 ratio.toUnitZ( childPosition.z ) ) ;
					childPosition.setXYZ( childPosition.x, childPosition.y + length.y + margin.y, childPosition.z ) ;
					absoluteLength.add( 0.0f, length.y + margin.y, 0.0f ) ;
				}
			}
		} ;
	}

	/**
		Order the UIElements from top to bottom.
		If the element's length is greater than 0 then 
		it will be given that space.

		If the element does not have a valid length, 
		then maximum will be used. Minimum does not need to 
		be checked as length cannot be smaller than minimum.

		If non of the above provide a valid length then the 
		element's length will be set to default item size. 
	*/
	@Override
	protected ILayout getHorizontalUpdater()
	{
		return new ILayout()
		{
			private final List<UIElement> ordered = MalletList.<UIElement>newList() ;
			private final Vector3 childPosition = new Vector3() ;

			@Override
			public void update( final float _dt, final IChildren _children )
			{
				ordered.clear() ;
				_children.getElements( ordered ) ;

				final Vector3 listLength = UIList.this.getLength() ;
				final Vector3 listMargin = UIList.this.getMargin() ;

				final float height = listLength.y - ( scrollWidth.y + ( listMargin.y * 2.0f ) ) ;
				absoluteLength.setXYZ( 0.0f, height, listLength.z ) ;
				childPosition.setXYZ( 0.0f, scrollWidth.y, 0.0f ) ;

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
					final Vector3 length = element.getLength() ;
					final Vector3 margin = element.getMargin() ;

					final Vector3 maximum = element.getMaximumLength() ;
					if( maximum.x > 0.0f )
					{
						element.setLength( ratio.toUnitX( maximum.x ),
											ratio.toUnitY( absoluteLength.y ),
											ratio.toUnitZ( absoluteLength.z ) ) ;
					}
					else
					{
						element.setLength( ratio.toUnitX( defaultItemSize.x ),
											ratio.toUnitY( absoluteLength.y ),
											ratio.toUnitZ( absoluteLength.z ) ) ;
					}

					element.setPosition( ratio.toUnitX( childPosition.x ),
										 ratio.toUnitY( childPosition.y ),
										 ratio.toUnitZ( childPosition.z ) ) ;
					childPosition.setXYZ( childPosition.x + length.x + margin.x, childPosition.y, childPosition.z ) ;
					absoluteLength.add( length.x + margin.x, 0.0f, 0.0f ) ;
				}
			}
		} ;
	}

	@Override
	protected ILayout getGridUpdater()
	{
		return new ILayout()
		{
			private final List<UIElement> ordered = MalletList.<UIElement>newList() ;
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

				final Vector3 listLength = UIList.this.getLength() ;
				final Vector3 listMargin = UIList.this.getMargin() ;

				final float width  = listLength.x - scrollWidth.x ;

				absoluteLength.setXYZ( width, margin.y, listLength.z ) ;
				childPosition.setXYZ( margin ) ;
				childPosition.x += scrollWidth.x ;

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

					if( ( childPosition.x + length.x + margin.x ) > width )
					{
						final float y = childPosition.y + length.y + margin.y ;
						childPosition.setXYZ( scrollWidth.x + margin.x, y, childPosition.z ) ;
						absoluteLength.y = y + length.y + margin.y ;
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

	@Override
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

	/**
		Returns the elements absolute length in pixels.
		Pass in a Vector3 to retrieve the length in units.
	*/
	public Vector3 getAbsoluteLength( final Vector3 _unit )
	{
		if( _unit != null )
		{
			getRatio().toUnit( getAbsoluteLength(), _unit ) ;
		}

		return getAbsoluteLength() ;
	}

	/**
		Return the element's actual absolute length in pixels.
	*/
	public Vector3 getAbsoluteLength()
	{
		getCurrentUpdater().update( 0.0f, this ) ;
		return absoluteLength ;
	}

	/**
		Returns the elements absolute length in pixels.
		Pass in a Vector3 to retrieve the length in units.
	*/
	public Vector3 getScrollWidth( final Vector3 _unit )
	{
		if( _unit != null )
		{
			getRatio().toUnit( getScrollWidth(), _unit ) ;
		}

		return getScrollWidth() ;
	}

	/**
		Return the element's actual absolute length in pixels.
	*/
	public Vector3 getScrollWidth()
	{
		return scrollWidth ;
	}

	public Vector3 getScrollbarLength( final Vector3 _unit )
	{
		if( _unit != null )
		{
			getRatio().toUnit( getScrollbarLength(), _unit ) ;
		}

		return getScrollbarLength() ;
	}

	public Vector3 getScrollbarLength()
	{
		final Vector3 len = getLength() ;
		final Vector3 absLen = getAbsoluteLength() ;

		scrollbarLength.x = ( absLen.x > len.x ) ? ( len.x * len.x ) / absLen.x : 0.0f ;
		scrollbarLength.y = ( absLen.y > len.y ) ? ( len.y * len.y ) / absLen.y : 0.0f ;
		scrollbarLength.z = ( absLen.z > len.z ) ? ( len.y * len.z ) / absLen.z : 0.0f ;
		return scrollbarLength ;
	}

	/**
		Return the camera that this UI is expected to be 
		displayed on - used to convert inputs to the 
		correct co-ordinate system.
	*/
	@Override
	public Camera getCamera()
	{
		return externalCamera ;
	}

	public World getInternalWorld()
	{
		return frame.getWorld() ;
	}

	public Camera getInternalCamera()
	{
		return frame.getCamera() ;
	}

	public Connect.Signal defaultElementSizeChanged()
	{
		return defaultElementSizeChanged ;
	}

	public Connect.Signal scrollWidthChanged()
	{
		return scrollWidthChanged ;
	}

	public Connect.Signal dragDelayChanged()
	{
		return dragDelayChanged ;
	}

	public class ScrollSingleEngageComponent extends SingleEngageComponent
	{
		public ScrollSingleEngageComponent( final UILayout _parent )
		{
			super( _parent ) ;
		}

		@Override
		public InputEvent.Action mouseMove( final InputEvent _input )
		{
			final InputEvent.Action action = super.mouseMove( _input ) ;
			//System.out.println( action ) ;
			return action ;
		}
	}

	public static class Meta extends UILayout.Meta
	{
		public Meta()
		{
			super() ;
		}

		@Override
		public String getElementType()
		{
			return "UILIST" ;
		}
	}

	private static class ScrollInputComponent extends InputComponent
	{
		private final Vector3 position = new Vector3() ;
		private final Vector3 length = new Vector3() ;

		private final Vector2 last = new Vector2() ;
		private final Vector2 diff = new Vector2() ;

		private boolean pressed = false ;

		private final Connect.Slot<UIList> disengagedSlot = new Connect.Slot<UIList>()
		{
			@Override
			public void slot( final UIList _layout )
			{
				pressed = false ;
			}
		} ;

		public ScrollInputComponent( final UIList _parent )
		{
			super( _parent ) ;
			UIElement.connect( _parent, _parent.elementDisengaged(), disengagedSlot ) ;
		}

		@Override
		public void shutdown()
		{
			super.shutdown() ;
			final UIList parent = getParentList() ;
			UIElement.disconnect( parent, parent.elementDisengaged(), disengagedSlot ) ;
		}

		@Override
		public InputEvent.Action scroll( final InputEvent _input )
		{
			final EngageComponent mode = getParentList().getEngageMode() ;
			if( mode.isEngaged() == true )
			{
				applyScroll( -_input.getMouseX() * 50, -_input.getMouseY() * 50 ) ;
				return InputEvent.Action.CONSUME ;
			}
			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public InputEvent.Action touchReleased( final InputEvent _input )
		{
			return mouseReleased( _input ) ;
		}

		@Override
		public InputEvent.Action touchPressed( final InputEvent _input )
		{
			return mousePressed( _input ) ;
		}

		@Override
		public InputEvent.Action touchMove( final InputEvent _input )
		{
			return mouseMove( _input ) ;
		}

		@Override
		public InputEvent.Action mouseReleased( final InputEvent _input )
		{
			pressed = false ;
			last.setXY( _input.getMouseX(), _input.getMouseY() ) ;
			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public InputEvent.Action mousePressed( final InputEvent _input )
		{
			pressed = true ;
			last.setXY( _input.getMouseX(), _input.getMouseY() ) ;
			applyScroll( 0.0f, 0.0f ) ;
			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public InputEvent.Action mouseMove( final InputEvent _input )
		{
			InputEvent.Action action = InputEvent.Action.CONSUME ;
			final EngageComponent mode = getParentList().getEngageMode() ;
			if( pressed == true )
			{
				final UIList parent = getParentList() ;
				diff.x = ( parent.getType() == ILayout.Type.HORIZONTAL ) ? ( last.x - _input.getMouseX() ) : 0.0f ;
				diff.y = ( parent.getType() == ILayout.Type.VERTICAL || parent.getType() == ILayout.Type.GRID ) ? ( last.y - _input.getMouseY() ) : 0.0f ;
				action = applyScroll( diff.x, diff.y ) ;
			}

			last.setXY( _input.getMouseX(), _input.getMouseY() ) ;
			return action ;
		}

		private InputEvent.Action applyScroll( final float _x, final float _y )
		{
			final UIList parent = getParentList() ;
			parent.frame.getCamera().getUIPosition( position ) ;
			parent.getLength( length ) ;

			position.add( _x, _y, 0.0f ) ;

			final Vector3 length = parent.getLength() ;
			final float width = parent.absoluteLength.x - length.x ;
			final float height = parent.absoluteLength.y - length.y ;

			final ILayout.Type type = parent.getType() ;
			InputEvent.Action action = InputEvent.Action.CONSUME ;
			if( ( type == ILayout.Type.HORIZONTAL && ( position.x <= 0.0f || position.x >= width ) ) || 
				( type == ILayout.Type.VERTICAL   && ( position.y <= 0.0f || position.y >= height ) ) )
			{
				action = InputEvent.Action.PROPAGATE ;
			}

			position.x = ( position.x > width ) ? width : position.x ;
			position.y = ( position.y > height ) ? height : position.y ;

			position.x = ( position.x > 0.0f ) ? position.x : 0.0f ;
			position.y = ( position.y > 0.0f ) ? position.y : 0.0f ;

			parent.frame.getCamera().setUIPosition( position.x, position.y, 0.0f ) ;
			parent.makeDirty() ;

			return action ;
		}

		public UIList getParentList()
		{
			return ( UIList )getParent() ;
		}
	}
}
