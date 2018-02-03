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
	private final World internalWorld ;
	private final Camera internalCamera ;
	private Camera externalCamera = CameraAssist.getDefaultCamera() ;
	private final Draw pane ;

	private final Vector3 defaultItemSize = new Vector3() ;		// In pixels
	private final Vector3 scrollbarLength = new Vector3() ;
	private final Vector3 scrollWidth = new Vector3() ;

	// Used to define the boundaries of the scroll
	private final Vector3 absoluteLength = new Vector3() ;		// In pixels
	private int dragDelay = 100 ;

	// Anything added to the UIList will make use of this 
	// DrawDelegate rather than the delegate coming from 
	// the UIComponent.
	private DrawDelegate<World, Draw> internalDelegate = null ;

	private final Connect.Signal defaultElementSizeChanged = new Connect.Signal() ;
	private final Connect.Signal scrollWidthChanged        = new Connect.Signal() ;
	private final Connect.Signal dragDelayChanged          = new Connect.Signal() ;

	public UIList( final Type _type )
	{
		this( _type, new Vector3(), new Vector3(), new Vector3() ) ;
	}

	public UIList( final Type _type, final Vector3 _length )
	{
		this( _type, new Vector3(), new Vector3(), _length ) ;
	}

	public UIList( final Type _type, final Vector3 _offset, final Vector3 _length )
	{
		this( _type, new Vector3(), _offset, _length ) ;
	}

	public UIList( final Type _type, final Vector3 _position, final Vector3 _offset, final Vector3 _length )
	{
		super( _type, _position, _offset, _length ) ;
		setDefaultElementSize( 1.0f, 1.0f, 1.0f ) ;
		setScrollWidth( 0.5f ) ;

		final UUID uid = UUID.randomUUID() ;
		internalWorld = WorldAssist.constructWorld( uid.toString(), 0 ) ;
		internalCamera = CameraAssist.createCamera( "SCROLL_CAMERA", new Vector3(),
																	 new Vector3(),
																	 new Vector3( 1, 1, 1 ) ) ;

		CameraAssist.addCamera( internalCamera, internalWorld ) ;
		pane = UIList.createPane( internalWorld, this ) ;

		addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
		{
			public void callback( final DrawDelegate<World, Draw> _delegate )
			{
				if( internalDelegate != null )
				{
					// Don't call shutdown(), we don't want to 
					// clean anything except an existing DrawDelegate.
					internalDelegate.shutdown() ;
				}

				internalDelegate = _delegate ;
				UIList.this.passListDrawDelegate( internalDelegate, internalWorld, internalCamera ) ;
			}
		} ) ) ;

		setEngageMode( new ScrollSingleEngageListener() ) ;
		initScrollInput() ;
	}

	private void initScrollInput()
	{
		addListener( new InputListener<UIList>()
		{
			private final Vector3 position = new Vector3() ;
			private final Vector3 length = new Vector3() ;

			private final Vector2 current = new Vector2() ;
			private final Vector2 last = new Vector2() ;
			private final Vector2 diff = new Vector2() ;

			private long timestamp = 0L ;
			private int timeDiff = 0 ;
			private boolean pressed = false ;

			private final Connect.Slot<UIList> disengagedSlot = new Connect.Slot<UIList>()
			{
				@Override
				public void slot( final UIList _layout )
				{
					pressed = false ;
				}
			} ;

			@Override
			public void setParent( UIList _parent )
			{
				UIElement.connect( _parent, _parent.elementDisengaged(), disengagedSlot ) ;
				super.setParent( _parent ) ;
			}
			
			@Override
			public void shutdown()
			{
				super.shutdown() ;
				final UIList parent = getParent() ;
				UIElement.disconnect( parent, parent.elementDisengaged(), disengagedSlot ) ;
			}

			@Override
			public InputEvent.Action scroll( final InputEvent _input )
			{
				final EngageListener mode = getParent().getEngageMode() ;
				if( mode.isEngaged() == true )
				{
					applyScroll( -_input.getMouseX() * 10, -_input.getMouseY() * 10 ) ;
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
				final EngageListener mode = getParent().getEngageMode() ;
				if( pressed == true )
				{
					diff.x = ( getType() == UIList.Type.HORIZONTAL ) ? ( last.x - _input.getMouseX() ) : 0.0f ;
					diff.y = ( getType() == UIList.Type.VERTICAL || getType() == UIList.Type.GRID ) ? ( last.y - _input.getMouseY() ) : 0.0f ;
					action = applyScroll( diff.x, diff.y ) ;
				}

				last.setXY( _input.getMouseX(), _input.getMouseY() ) ;
				return action ;
			}

			private InputEvent.Action applyScroll( final float _x, final float _y )
			{
				CameraAssist.getUIPosition( internalCamera, position ) ;
				getLength( length ) ;

				position.add( _x, _y, 0.0f ) ;

				final Vector3 length = getParent().getLength() ;
				final float width = absoluteLength.x - length.x ;
				final float height = absoluteLength.y - length.y ;

				final Type type = getParent().getType() ;
				InputEvent.Action action = InputEvent.Action.CONSUME ;
				if( ( type == Type.HORIZONTAL && ( position.x <= 0.0f || position.x >= width ) ) || 
					( type == Type.VERTICAL   && ( position.y <= 0.0f || position.y >= height ) ) )
				{
					action = InputEvent.Action.PROPAGATE ;
				}

				position.x = ( position.x > width ) ? width : position.x ;
				position.y = ( position.y > height ) ? height : position.y ;

				position.x = ( position.x > 0.0f ) ? position.x : 0.0f ;
				position.y = ( position.y > 0.0f ) ? position.y : 0.0f ;

				CameraAssist.amendUIPosition( internalCamera, position.x, position.y, 0.0f ) ;
				getParent().makeDirty() ;

				return action ;
			}
		} ) ;
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

			if( getInternalDrawDelegate() != null )
			{
				_element.passDrawDelegate( getInternalDrawDelegate(), getInternalWorld(), getInternalCamera() ) ;
			}
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
	public void setLayer( final int _layer )
	{
		super.setLayer( _layer ) ;
		if( isDirty() == true )
		{
			DrawAssist.amendOrder( pane, getLayer() + 1 ) ;
		}
	}

	@Override
	public void setLength( final float _x, final float _y, final float _z )
	{
		super.setLength( _x, _y, _z ) ;

		if( isDirty() == true )
		{
			// We only want to update the pane if 
			// calling setLength was considered significant 
			// enough to flag the UIList as dirty.
			final Vector3 length = getLength() ;
			final int width = ( int )length.x ;
			final int height = ( int )length.y ;

			CameraAssist.amendScreenResolution( internalCamera, width, height ) ;
			CameraAssist.amendDisplayResolution( internalCamera, width, height ) ;
			CameraAssist.amendOrthographic( internalCamera, 0.0f, height, 0.0f, width, -1000.0f, 1000.0f ) ;

			WorldAssist.setRenderDimensions( internalWorld, 0, 0, width, height ) ;
			WorldAssist.setDisplayDimensions( internalWorld, 0, 0, width, height ) ;

			Shape.updatePlaneGeometry( DrawAssist.getDrawShape( pane ), getLength() ) ;
			DrawAssist.forceUpdate( pane ) ;
		}
	}

	@Override
	public void passDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world, final Camera _camera )
	{
		externalCamera = ( _camera != null ) ? _camera : externalCamera ;

		final List<IBase<? extends UIElement>> base = getListenerUnit().getListeners() ;
		final int size = base.size() ;
		for( int i = 0; i < size; i++ )
		{
			// Listeners to the UIList will be given the 
			// DrawDelegate passed in - only children 
			// will be given the lists DrawDelegate.
			base.get( i ).passDrawDelegate( _delegate, _world ) ;
		}

		// Though the UIList will give its children its 
		// own DrawDelegate the UIList will give the 
		// DrawDelegate passed in here the Draw pane.
		_delegate.addBasicDraw( pane, _world ) ;
	}

	private void passListDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world, final Camera _camera )
	{
		final List<UIElement> ordered = getElements() ;

		final int size = ordered.size() ;
		for( int i = 0; i < size; i++ )
		{
			// Pass the DrawDelegate of UIList to the 
			// lists children instead of the DrawDelegate
			// that is provided by passDrawDelegate.
			ordered.get( i ).passDrawDelegate( _delegate, _world, _camera ) ;
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
				final float x = CameraAssist.convertInputToUICameraX( getCamera(), input.getMouseX() ) - ( position.x + offset.x ) ;
				final float y = CameraAssist.convertInputToUICameraY( getCamera(), input.getMouseY() ) - ( position.y + offset.y ) ;
				input.setInput( input.getInputType(), ( int )x, ( int )y ) ;

				return processInputEvent( input ) ;
			}
		}
	}

	/**
		Override the parent implementation comsing from UILayout.
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
	
	@Override
	public void shutdown()
	{
		super.shutdown() ;
		if( internalWorld != WorldAssist.getDefaultWorld() )
		{
			WorldAssist.destroyWorld( internalWorld ) ;
		}
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
	protected UIElementUpdater getVerticalUpdater()
	{
		return new UIElementUpdater()
		{
			private final Vector3 childPosition = new Vector3() ;

			@Override
			public void update( final float _dt, final List<UIElement> _ordered )
			{
				final Vector3 listLength = UIList.this.getLength() ;
				final Vector3 listMargin = UIList.this.getMargin() ;

				final float width = listLength.x - ( scrollWidth.x + ( listMargin.x * 2.0f ) ) ;
				absoluteLength.setXYZ( width, 0.0f, listLength.z ) ;
				childPosition.setXYZ( scrollWidth.x, 0.0f, 0.0f ) ;

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
	protected UIElementUpdater getHorizontalUpdater()
	{
		return new UIElementUpdater()
		{
			private final Vector3 layoutPosition = new Vector3() ;
			private final Vector3 childPosition = new Vector3() ;

			@Override
			public void update( final float _dt, final List<UIElement> _ordered )
			{
				final Vector3 listLength = UIList.this.getLength() ;
				final Vector3 listMargin = UIList.this.getMargin() ;

				final float height = listLength.y - ( scrollWidth.y + ( listMargin.y * 2.0f ) ) ;
				absoluteLength.setXYZ( 0.0f, height, listLength.z ) ;
				childPosition.setXYZ( 0.0f, scrollWidth.y, 0.0f ) ;

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

	protected UIElementUpdater getGridUpdater()
	{
		return new UIElementUpdater()
		{
			private final Vector3 childPosition = new Vector3() ;

			@Override
			public void update( final float _dt, final List<UIElement> _ordered )
			{
				final UIElement reference = getReferenceElement( _ordered ) ;
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
		getCurrentUpdater().update( 0.0f, getElements() ) ;
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
		Return the DrawDelegate that this UI is expected to be 
		displayed on.
	*/
	public DrawDelegate<World, Draw> getInternalDrawDelegate()
	{
		return internalDelegate ;
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
		return internalWorld ;
	}

	public Camera getInternalCamera()
	{
		return internalCamera ;
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

	private static Draw createPane( final World _world, final UIElement _parent )
	{
		final Vector3 length = _parent.getLength() ;
		final Draw pane = DrawAssist.createDraw( _parent.getPosition(),
												 _parent.getOffset(),
												 new Vector3(),
												 new Vector3( 1, 1, 1 ), _parent.getLayer() + 1 ) ;

		DrawAssist.amendUI( pane, true ) ;
		DrawAssist.amendShape( pane, Shape.constructPlane( new Vector3( length.x, length.y, 0 ),
															new Vector2( 0, 1 ),
															new Vector2( 1, 0 ) ) ) ;

		final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
		ProgramAssist.map( program, "inTex0", new MalletTexture( _world ) ) ;
		DrawAssist.attachProgram( pane, program ) ;

		return pane ;
	}

	public class ScrollSingleEngageListener extends SingleEngageListener
	{
		private InputEvent lastInput = null ;
	
		public ScrollSingleEngageListener() {}

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
}
