package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.audio.AudioDelegate ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

/**
	UIMenu is designed to provide Header/Footer toolbars that 
	can be filled with Menu.Items - these items are extended 
	UIButtons that allow for an external UIElement to be 
	shown/hidden once clicked.

	FIXME: There is a bug with the UILayout engagement system
	that disables dropdown UIElements from recieving 
	input events under certain circumstances.
	
	I'm not sure if this bug is caused by the UIMenu, Item, 
	or dropdown not correctly being engaged.
*/
public class UIMenu extends UILayout
{
	public UIMenu( final Type _type, final float _length )
	{
		super( _type ) ;
		switch( _type )
		{
			case HORIZONTAL :
			{
				setMaximumLength( 0.0f, _length, 0.0f ) ;
				break ;
			}
			default         :
			case VERTICAL   :
			{
				setMaximumLength( _length, 0.0f, 0.0f ) ;
				break ;
			}
		}
	}

	public static class Item extends UIButton
	{
		private final UIElement dropdown ;
	
		public Item( final UIElement _dropdown )
		{
			super() ;
			dropdown = _dropdown ;
			addListener( new UIMenu.DropDownListener( dropdown ) ) ;
		}

		@Override
		public void setInputAdapterInterface( final InputAdapterInterface _adapter )
		{
			super.setInputAdapterInterface( _adapter ) ;
			dropdown.setInputAdapterInterface( _adapter ) ;
		}

		@Override
		public void update( final float _dt, final List<Event<?>> _events )
		{
			super.update( _dt, _events ) ;
			dropdown.update( _dt, _events ) ;
		}

		@Override
		public InputEvent.Action passInputEvent( final InputEvent _event )
		{
			if( super.passInputEvent( _event ) == InputEvent.Action.CONSUME )
			{
				return InputEvent.Action.CONSUME ;
			}

			if( dropdown.passInputEvent( _event ) == InputEvent.Action.CONSUME )
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
	}

	public static class DropDownListener extends InputListener<Item>
	{
		private final UIElement dropdown ;
		private final Vector3 position = new Vector3() ;
		private final Vector3 length = new Vector3() ;

		public DropDownListener( final UIElement _toDrop )
		{
			dropdown = _toDrop ;
			dropdown.setVisible( false ) ;
		}

		@Override
		public void engage()
		{
			final UIElement parent = getParent() ;
			parent.getPosition( position ) ;
			parent.getLength( length ) ;

			dropdown.setLayer( parent.getLayer() + 1 ) ;
			dropdown.setPosition( position.x, position.y + length.y, 0.0f ) ;
			dropdown.engage() ;
		}

		@Override
		public void disengage()
		{
			dropdown.disengage() ;
			dropdown.setVisible( false ) ;
		}

		@Override
		public InputEvent.Action mouseReleased( final InputEvent _input )
		{
			dropdown.setVisible( !dropdown.isVisible() ) ;
			dropdown.setEngage( !dropdown.isEngaged() ) ;
			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public void shutdown()
		{
			super.shutdown() ;
			dropdown.shutdown() ;
		}
	}
}
