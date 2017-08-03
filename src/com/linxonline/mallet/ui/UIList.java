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
	private final Draw pane ;

	private final Vector3 defaultItemSize = new Vector3() ;		// In pixels
	
	// Anything added to the UIList will make use of this 
	// DrawDelegate rather than the delegate coming from 
	// the UIComponent.
	private DrawDelegate<World, Draw> delegate = null ;

	public UIList( final Type _type, final float _length )
	{
		super( _type ) ;
		final UUID uid = UUID.randomUUID() ;
		world = WorldAssist.constructWorld( uid.toString(), 0 ) ;
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
				UIList.this.passListDrawDelegate( delegate, world ) ;
			}
		} ) ) ;

		switch( _type )
		{
			case HORIZONTAL :
			{
				setMaximumLength( 0.0f, _length, 0.0f ) ;
				break ;
			}
			case VERTICAL :
			default       :
			{
				setMaximumLength( _length, 0.0f, 0.0f ) ;
				break ;
			}
		}
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
	public void passDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world )
	{
		// Though the UIList will give its children its 
		// own DrawDelegate the UIList will give the 
		// DrawDelegate passed in here the Draw pane.
		_delegate.addBasicDraw( pane, _world ) ;
	}

	private void passListDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world )
	{
		// Pass the DrawDelegate of UIList to the 
		// lists children instead of the DrawDelegate
		// that is provided by passDrawDelegate.
		super.passDrawDelegate( delegate, world ) ;
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
			@Override
			public void update( final float _dt, final List<UIElement> _ordered )
			{

			}
		} ;
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
	protected UIElementUpdater getHorizontalUpdater()
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
