package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

/**
	UIMenu is designed to provide Header/Footer toolbars that 
	can be filled with Menu.Items - these items are extended 
	UIButtons that allow for an external UIElement to be 
	shown/hidden once clicked.
*/
public class UIMenu extends UILayout
{
	public UIMenu( final ILayout.Type _type )
	{
		this( _type, 1.0f ) ;
	}

	public UIMenu( final ILayout.Type _type, final float _thickness )
	{
		super( _type ) ;
		switch( _type )
		{
			case HORIZONTAL :
			{
				setMaximumLength( 0.0f, _thickness, 0.0f ) ;
				break ;
			}
			default         :
			case VERTICAL   :
			{
				setMaximumLength( _thickness, 0.0f, 0.0f ) ;
				break ;
			}
		}
	}

	public static UIMenu applyMeta( final UIMenu.Meta _meta, final UIMenu _menu )
	{
		UILayout.applyMeta( _meta, _menu ) ;
		switch( _meta.getType() )
		{
			case HORIZONTAL :
			{
				_menu.setMaximumLength( 0.0f, _meta.getThickness(), 0.0f ) ;
				break ;
			}
			default         :
			case VERTICAL   :
			{
				_menu.setMaximumLength( _meta.getThickness(), 0.0f, 0.0f ) ;
				break ;
			}
		}
		return _menu ;
	}

	public static class Meta extends UILayout.Meta
	{
		private final UIVariant thickness = new UIVariant( "THICKNESS", 0.0f, new Connect.Signal() ) ;

		public Meta()
		{
			super() ;

			int row = rowCount( root() ) ;
			createData( null, row + 1, 1 ) ;

			setData( new UIModelIndex( root(), row++, 0 ), thickness,  UIAbstractModel.Role.User ) ;
		}

		@Override
		public String getElementType()
		{
			return "UIMENU" ;
		}

		public final void setThickness( final float _thickness )
		{
			if( Math.abs( thickness.toFloat() - _thickness ) > 0.001f )
			{
				thickness.setFloat( _thickness ) ;
				UIElement.signal( this, thickness.getSignal() ) ;
			}
		}

		public final float getThickness()
		{
			return thickness.toFloat() ;
		}

		public final Connect.Signal thicknessChanged()
		{
			return thickness.getSignal() ;
		}
	}

	/**
		Menu.Item is a UIButton that supports a dropdown.
		The dropdown can be any UIElement though most likely 
		to be a UILayout or UIList.
		By default the dropdown is invisible and disengaged, if 
		the Menu.Item is pressed then the dropdown is made visible 
		and flagged as engaged.The dropdown will loose visibility 
		and engagement if the Menu.Item is disengaged. The dropdown 
		will be disengaged and made invisible if a mouse release
		event is received and the event is consumed.
	*/
	public static class Item extends UIButton
	{
		private final UIElement dropdown ;
		private final int originalLayer ;
	
		public Item( final UIElement _dropdown )
		{
			super() ;
			dropdown = _dropdown ;
			dropdown.setVisible( false ) ;
			originalLayer = dropdown.getLayer() ;

			UIElement.connect( this, layerChanged(), ( final Item _item ) ->
			{
				dropdown.setLayer( _item.getLayer() + originalLayer ) ;
			} ) ;

			UIElement.connect( this, elementEngaged(), new Connect.Slot<Item>()
			{
				private final Vector3 position = new Vector3() ;
				private final Vector3 length = new Vector3() ;
			
				@Override
				public void slot( final Item _item )
				{
					_item.getPosition( position ) ;
					_item.getLength( length ) ;

					dropdown.setPosition( position.x, position.y + length.y, 0.0f ) ;
					dropdown.engage() ;
				}
			} ) ;

			UIElement.connect( this, elementDisengaged(), ( final Item _item ) ->
			{
				dropdown.disengage() ;
				dropdown.setVisible( false ) ;
			} ) ;

			UIElement.connect( this, released(), ( final Item _item ) ->
			{
				dropdown.setVisible( !dropdown.isVisible() ) ;
				dropdown.setEngage( !dropdown.isEngaged() ) ;
			} ) ;
		}

		@Override
		public void update( final float _dt )
		{
			super.update( _dt ) ;
			dropdown.update( _dt ) ;
		}

		@Override
		public void setWorldAndCamera( final World _world, final Camera _camera )
		{
			super.setWorldAndCamera( _world, _camera ) ;
			dropdown.setWorldAndCamera( _world, _camera ) ;
		}

		@Override
		public InputEvent.Action passInputEvent( final InputEvent _event )
		{
			if( dropdown.isVisible() == true )
			{
				if( dropdown.passInputEvent( _event ) == InputEvent.Action.CONSUME )
				{
					switch( _event.getInputType() )
					{
						case MOUSE1_RELEASED   :
						case MOUSE2_RELEASED   :
						case MOUSE3_RELEASED   :
						{
							dropdown.setVisible( !dropdown.isVisible() ) ;
							dropdown.setEngage( !dropdown.isEngaged() ) ;
							break ;
						}
					}
					return InputEvent.Action.CONSUME ;
				}
			}

			if( super.passInputEvent( _event ) == InputEvent.Action.CONSUME )
			{
				return InputEvent.Action.CONSUME ;
			}

			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public boolean isIntersectInput( final InputEvent _event )
		{
			if( super.isIntersectInput( _event ) == true )
			{
				return true ;
			}

			return ( dropdown.isIntersectInput( _event ) && dropdown.isVisible() ) ;
		}

		@Override
		public void shutdown()
		{
			super.shutdown() ;
			dropdown.shutdown() ;
		}
	}
}
