package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.audio.AudioDelegate ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

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
			if( dropdown != null )
			{
				dropdown.setLayer( getLayer() + 1 ) ;
				addListener( new UIMenu.DropDownListener( dropdown ) ) ;
			}
		}

		@Override
		public void setInputAdapterInterface( final InputAdapterInterface _adapter )
		{
			super.setInputAdapterInterface( _adapter ) ;
			if( dropdown != null )
			{
				dropdown.setInputAdapterInterface( _adapter ) ;
			}
		}

		@Override
		public InputEvent.Action passInputEvent( final InputEvent _event )
		{
			System.out.println( "Item: " + _event ) ;
			if( super.passInputEvent( _event ) == InputEvent.Action.CONSUME )
			{
				// Don't pass the InputEvent on to the child elements.
				// The UILayout may wish to consume the event if it was 
				// used to get focus onto a child element.  
				return InputEvent.Action.CONSUME ;
			}

			if( dropdown != null )
			{
				return dropdown.passInputEvent( _event ) ;
			}

			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public void update( final float _dt, final List<Event<?>> _events )
		{
			super.update( _dt, _events ) ;
			if( dropdown != null )
			{
				dropdown.update( _dt, _events ) ;
			}
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
		public void disengage()
		{
			dropdown.disengage() ;
			dropdown.setVisible( false ) ;
		}

		@Override
		public InputEvent.Action mouseReleased( final InputEvent _input )
		{
			final UIElement parent = getParent() ;
			parent.getPosition( position ) ;
			parent.getLength( length ) ;

			dropdown.setVisible( true ) ;
			dropdown.setPosition( position.x, position.y + length.y, 0.0f ) ;
			dropdown.engage() ;


			parent.makeDirty() ;
			return InputEvent.Action.CONSUME ;
		}

		@Override
		public void shutdown()
		{
			super.shutdown() ;
			dropdown.shutdown() ;
		}
	}
}
