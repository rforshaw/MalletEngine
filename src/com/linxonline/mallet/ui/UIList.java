package com.linxonline.mallet.ui ;

import java.util.UUID ;
import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

public class UIList extends UILayout
{
	private final World world ;
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
	private DrawDelegate<World, Draw> delegate = null ;

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
		world = WorldAssist.constructWorld( uid.toString(), 0 ) ;
		internalCamera = CameraAssist.createCamera( "SCROLL_CAMERA", new Vector3(),
																	 new Vector3(),
																	 new Vector3( 1, 1, 1 ) ) ;

		CameraAssist.addCamera( internalCamera, world ) ;
		pane = UIList.createPane( world, this ) ;

		addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
		{
			public void callback( final DrawDelegate<World, Draw> _delegate )
			{
				if( delegate != null )
				{
					// Don't call shutdown(), we don't want to 
					// clean anything except an existing DrawDelegate.
					delegate.shutdown() ;
				}

				delegate = _delegate ;
				UIList.this.passListDrawDelegate( delegate, world, internalCamera ) ;
			}
		} ) ) ;

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

			@Override
			public InputEvent.Action scroll( final InputEvent _input )
			{
				final EngageListener mode = getParent().getEngageMode() ;
				if( mode.isEngaged() == true )
				{
					applyScroll( -_input.getMouseX(), -_input.getMouseY() ) ;
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
				return InputEvent.Action.CONSUME ;
			}

			@Override
			public InputEvent.Action mousePressed( final InputEvent _input )
			{
				pressed = true ;
				last.setXY( _input.getMouseX(), _input.getMouseY() ) ;
				return InputEvent.Action.CONSUME ;
			}

			@Override
			public InputEvent.Action mouseMove( final InputEvent _input )
			{
				final EngageListener mode = getParent().getEngageMode() ;
				if( pressed == true && mode.isEngaged() == false )
				{
					diff.x = ( getType() == UIList.Type.HORIZONTAL ) ? ( last.x - _input.getMouseX() ) : 0.0f ;
					diff.y = ( getType() == UIList.Type.VERTICAL )   ? ( last.y - _input.getMouseY() ) : 0.0f ;
					applyScroll( diff.x, diff.y ) ;
				}

				last.setXY( _input.getMouseX(), _input.getMouseY() ) ;
				return InputEvent.Action.CONSUME ;
			}

			private void applyScroll( final float _x, final float _y )
			{
				CameraAssist.getUIPosition( internalCamera, position ) ;
				getLength( length ) ;

				position.add( _x, _y, 0.0f ) ;

				final Vector3 length = getParent().getLength() ;
				final float width = absoluteLength.x - length.x ;
				final float height = absoluteLength.y - length.y ;

				position.x = ( position.x > width ) ? width : position.x ;
				position.y = ( position.y > height ) ? height : position.y ;

				position.x = ( position.x > 0.0f ) ? position.x : 0.0f ;
				position.y = ( position.y > 0.0f ) ? position.y : 0.0f ;

				CameraAssist.amendUIPosition( internalCamera, position.x, position.y, 0.0f ) ;
				getParent().makeDirty() ;
			}

			@Override
			public void disengage()
			{
				pressed = false ;
			}
		} ) ;
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
	}

	/**
		Define how long the delay can be between the user 
		pressing down and moving that could trigger scrolling.
	*/
	public void setDragDelay( final int _delay )
	{
		dragDelay = _delay ;
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

			WorldAssist.setRenderDimensions( world, 0, 0, width, height ) ;
			WorldAssist.setDisplayDimensions( world, 0, 0, width, height ) ;

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
		if( world != WorldAssist.getDefaultWorld() )
		{
			WorldAssist.destroyWorld( world ) ;
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

	@Override
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
		Return the camera that this UI is expected to be 
		displayed on - used to convert inputs to the 
		correct co-ordinate system.
	*/
	@Override
	public Camera getCamera()
	{
		return externalCamera ;
	}

	public Camera getInternalCamera()
	{
		return internalCamera ;
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
	
	public static class UIScrollbarListener<T extends UIList> extends GUIBase<T>
	{
		private final Vector3 xLength = new Vector3() ;
		private final Vector3 yLength = new Vector3() ;
		private final Vector3 offset = new Vector3() ;			// Offset within the UIElement

		private final MalletTexture sheet ;
		private final UIElement.UV uv ;

		protected Draw xBar = null ;
		protected Draw yBar = null ;

		public UIScrollbarListener( final MalletTexture _sheet,
									final UIElement.UV _uv )
		{
			sheet = _sheet ;
			uv = _uv ;
		}

		/**
			Can be used to construct Draw objects before a 
			DrawDelegate is provided by the Rendering System.
		*/
		public void constructDraws()
		{
			final T parent = getParent() ;
			final int layer = parent.getLayer() ;

			CameraAssist.getUIPosition( parent.getInternalCamera(), offset ) ;
			updateLengths( parent.getScrollbarLength(),
						   parent.getScrollWidth() ) ;

			{
				xBar = DrawAssist.createDraw( parent.getPosition(),
											  offset,
											  new Vector3(),
											  new Vector3( 1, 1, 1 ),
											  layer ) ;
				DrawAssist.amendUI( xBar, true ) ;
				DrawAssist.amendShape( xBar, Shape.constructPlane( xLength, uv.min, uv.max ) ) ;

				final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
				ProgramAssist.map( program, "inTex0", sheet ) ;

				DrawAssist.attachProgram( xBar, program ) ;
			}

			{
				yBar = DrawAssist.createDraw( parent.getPosition(),
											offset,
											new Vector3(),
											new Vector3( 1, 1, 1 ),
											layer ) ;
				DrawAssist.amendUI( yBar, true ) ;
				DrawAssist.amendShape( yBar, Shape.constructPlane( yLength, uv.min, uv.max ) ) ;

				final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
				ProgramAssist.map( program, "inTex0", sheet ) ;

				DrawAssist.attachProgram( yBar, program ) ;
			}
		}

		/**
			Called when listener receives a valid DrawDelegate
			and when the parent UIElement is flagged as visible.
		*/
		@Override
		public void addDraws( final DrawDelegate<World, Draw> _delegate, final World _world )
		{
			if( xBar != null )
			{
				_delegate.addBasicDraw( xBar, _world ) ;
			}

			if( yBar != null )
			{
				_delegate.addBasicDraw( yBar, _world ) ;
			}
		}

		/**
			Only called if there is a valid DrawDelegate and 
			when the parent UIElement is flagged as invisible.
		*/
		@Override
		public void removeDraws( final DrawDelegate<World, Draw> _delegate )
		{
			_delegate.removeDraw( xBar ) ;
			_delegate.removeDraw( yBar ) ;
		}

		@Override
		public void refresh()
		{
			super.refresh() ;
			final T parent = getParent() ;

			updatePosition( offset ) ;
			updateLengths( parent.getScrollbarLength(),
						   parent.getScrollWidth() ) ;

			if( xBar != null )
			{
				DrawAssist.amendOrder( xBar, parent.getLayer() ) ;
				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( xBar ), xLength ) ;
				DrawAssist.forceUpdate( xBar ) ;
			}

			if( yBar != null )
			{
				DrawAssist.amendOrder( yBar, parent.getLayer() ) ;
				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( yBar ), yLength ) ;
				DrawAssist.forceUpdate( yBar ) ;
			}
		}

		private void updatePosition( final Vector3 _position )
		{
			final T parent = getParent() ;
			final Vector3 length = parent.getLength() ;
			final Vector3 absLength = parent.getAbsoluteLength() ;

			CameraAssist.getUIPosition( parent.getInternalCamera(), _position ) ;
			_position.setXYZ( ( absLength.x > 0.0f ) ? ( _position.x * length.x ) / absLength.x : 0.0f,
							  ( absLength.y > 0.0f ) ? ( _position.y * length.y ) / absLength.y : 0.0f,
							  ( absLength.z > 0.0f ) ? ( _position.z * length.z ) / absLength.z : 0.0f ) ;

		}

		private void updateLengths( final Vector3 _length, final Vector3 _width )
		{
			xLength.setXYZ( _width.x, _length.y, _width.z ) ;
			yLength.setXYZ( _length.x, _width.y, _width.z ) ;
		}
	}
}
