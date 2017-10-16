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
		public void update( final float _dt, final List<Event<?>> _events )
		{
			super.update( _dt, _events ) ;
			dropdown.update( _dt, _events ) ;
		}

		@Override
		public void passDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world, final Camera _camera )
		{
			super.passDrawDelegate( _delegate, _world, _camera ) ;
			dropdown.passDrawDelegate( _delegate, _world, _camera ) ;
		}

		@Override
		public InputEvent.Action passInputEvent( final InputEvent _event )
		{
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
	}

	public static class DropDownListener extends InputListener<Item>
	{
		private final UIElement dropdown ;
		private final Vector3 position = new Vector3() ;
		private final Vector3 length = new Vector3() ;

		private final Connect.Slot<Item> engagedSlot = new Connect.Slot<Item>()
		{
			@Override
			public void slot( final Item _item )
			{
				_item.getPosition( position ) ;
				_item.getLength( length ) ;

				//dropdown.setLayer( parent.getLayer() + 1 ) ;
				dropdown.setPosition( position.x, position.y + length.y, 0.0f ) ;
				dropdown.engage() ;
			}
		} ;

		private final Connect.Slot<Item> disengagedSlot = new Connect.Slot<Item>()
		{
			@Override
			public void slot( final Item _item )
			{
				dropdown.disengage() ;
				dropdown.setVisible( false ) ;
			}
		} ;

		public DropDownListener( final UIElement _toDrop )
		{
			dropdown = _toDrop ;
			dropdown.setVisible( false ) ;
		}

		@Override
		public void setParent( Item _parent )
		{
			UIElement.connect( _parent, _parent.elementEngaged(), engagedSlot ) ;
			UIElement.connect( _parent, _parent.elementDisengaged(), disengagedSlot ) ;

			super.setParent( _parent ) ;
		}

		@Override
		public InputEvent.Action mouseMove( final InputEvent _input )
		{
			return dropdown.passInputEvent( _input ) ;
		}

		@Override
		public InputEvent.Action mousePressed( final InputEvent _input )
		{
			return mouseMove( _input ) ;
		}

		@Override
		public InputEvent.Action mouseReleased( final InputEvent _input )
		{
			final InputEvent.Action action = mouseMove( _input ) ;
			dropdown.setVisible( !dropdown.isVisible() ) ;
			dropdown.setEngage( !dropdown.isEngaged() ) ;
			return action ;
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
			return mouseMove( _input ) ;
		}

		@Override
		public InputEvent.Action keyReleased( final InputEvent _input )
		{
			return mouseMove( _input ) ;
		}

		@Override
		public InputEvent.Action analogueMove( final InputEvent _input )
		{
			return mouseMove( _input ) ;
		}

		@Override
		public void shutdown()
		{
			super.shutdown() ;
			dropdown.shutdown() ;

			final Item parent = getParent() ;
			UIElement.disconnect( parent, parent.elementEngaged(),    engagedSlot ) ;
			UIElement.disconnect( parent, parent.elementDisengaged(), disengagedSlot ) ;
		}
	}
}
