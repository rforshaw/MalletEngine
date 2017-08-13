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
	private Camera externalCamera ;
	private final Draw pane ;

	private final Vector3 defaultItemSize = new Vector3() ;		// In pixels

	// Used to define the boundaries of the scroll
	private final Vector3 absoluteLength = new Vector3() ;		// In pixels

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

			private boolean active = false ;
			private boolean moved = false ;

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
			public InputEvent.Action mouseReleased( final InputEvent _input )
			{
				active = false ;
				if( moved == true )
				{
					moved = false ;
					return InputEvent.Action.CONSUME ;
				}

				return InputEvent.Action.PROPAGATE ;
			}
			
			@Override
			public InputEvent.Action mousePressed( final InputEvent _input )
			{
				active = true ;
				last.setXY( _input.getMouseX(), _input.getMouseY() ) ;
				return InputEvent.Action.CONSUME ;
			}
			
			@Override
			public InputEvent.Action mouseMove( final InputEvent _input )
			{
				current.setXY( _input.getMouseX(), _input.getMouseY() ) ;
				if( active == true )
				{
					moved = true ;
					diff.x = ( getType() == UIList.Type.HORIZONTAL ) ? ( last.x - current.x ) : 0.0f ;
					diff.y = ( getType() == UIList.Type.VERTICAL )   ? ( last.y - current.y ) : 0.0f ;

					CameraAssist.getUIPosition( internalCamera, position ) ;
					getLength( length ) ;

					position.add( diff.x, diff.y, 0.0f ) ;

					/*position.x = ( position.x >= -absoluteLength.x ) ? position.x : -absoluteLength.x ;
					position.y = ( position.y >= -absoluteLength.y ) ? position.y : -absoluteLength.y ;

					position.x = ( position.x + length.x < absoluteLength.x ) ? position.x : 0.0f ;
					position.y = ( position.y + length.y < absoluteLength.y ) ? position.y : 0.0f ;*/

					CameraAssist.amendUIPosition( internalCamera, position.x, position.y, 0.0f ) ;
					last.setXY( current ) ;
					return InputEvent.Action.CONSUME ;
				}

				last.setXY( current ) ;
				return InputEvent.Action.PROPAGATE ;
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

	@Override
	public void setLength( final float _x, final float _y, final float _z )
	{
		super.setLength( _x, _y, _z ) ;

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

	@Override
	public void passDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world, final Camera _camera )
	{
		externalCamera = _camera ;

		// Though the UIList will give its children its 
		// own DrawDelegate the UIList will give the 
		// DrawDelegate passed in here the Draw pane.
		_delegate.addBasicDraw( pane, _world ) ;
	}

	private void passListDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world, final Camera _camera )
	{
		// Pass the DrawDelegate of UIList to the 
		// lists children instead of the DrawDelegate
		// that is provided by passDrawDelegate.
		super.passDrawDelegate( _delegate, _world, _camera ) ;
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		if( isIntersectInput( _event ) == true )
		{
			final Vector3 position = getPosition() ;
			final Vector3 offset = getOffset() ;

			final InputEvent input = new InputEvent( _event ) ;
			final int x = input.getMouseX() - ( int )( position.x + offset.x ) ;
			final int y = input.getMouseY() - ( int )( position.y + offset.y ) ;
			input.setInput( input.getInputType(), x, y ) ;

			super.passInputEvent( input ) ;
			// Even if no listener consumes the input 
			// we still don't want it to propagate.
			// The button may be part of a UIList and 
			// the event that was intended for the button 
			// will fall through to the UIList instead.  
			return InputEvent.Action.CONSUME ; 
		}

		return InputEvent.Action.PROPAGATE ;
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
				absoluteLength.setXYZ( listLength.x, 0.0f, listLength.z ) ;
				childPosition.setXYZ( 0.0f, 0.0f, 0.0f ) ;

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
						element.setLength( ratio.toUnitX( listLength.x ),
										   ratio.toUnitY( maximum.y ),
										   ratio.toUnitZ( listLength.z ) ) ;
					}
					else
					{
						element.setLength( ratio.toUnitX( listLength.x ),
											ratio.toUnitY( defaultItemSize.y ),
											ratio.toUnitZ( listLength.z ) ) ;
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
				absoluteLength.setXYZ( 0.0f, listLength.y, listLength.z ) ;
				childPosition.setXYZ( 0.0f, 0.0f, 0.0f ) ;

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
											ratio.toUnitY( listLength.y ),
											ratio.toUnitZ( listLength.z ) ) ;
					}
					else
					{
						element.setLength( ratio.toUnitX( defaultItemSize.x ),
											ratio.toUnitY( listLength.y ),
											ratio.toUnitZ( listLength.z ) ) ;
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
		Return the camera that this UI is expected to be 
		displayed on - used to convert inputs to the 
		correct co-ordinate system.
	*/
	@Override
	public Camera getCamera()
	{
		return externalCamera ;
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
}
