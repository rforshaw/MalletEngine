package com.linxonline.mallet.ui ;

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
		public Item()
		{
			super() ;
		}
	}

	public static class DropDownListener extends InputListener<UIMenu>
	{
		private UIElement dropdown ;

		public DropDownListener( final UIElement _toDrop )
		{
			dropdown = _toDrop ;
		}

		@Override
		public InputEvent.Action mouseReleased( final InputEvent _input )
		{
			final UIElement parent = getParent() ;
			final Vector3 position = parent.getPosition() ;
			final Vector3 length   = parent.getLength() ;
			final int layer        = parent.getLayer() + 1 ;

			dropdown.setLayer( layer ) ;
			dropdown.setPosition( position.x, position.y + length.y, 0.0f ) ;

			parent.makeDirty() ;
			return InputEvent.Action.CONSUME ;
		}

		@Override
		public void refresh()
		{
			dropdown.makeDirty() ;
		}
	}
}
