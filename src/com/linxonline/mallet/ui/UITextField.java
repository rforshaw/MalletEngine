 package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

public class UITextField extends UIElement
{
	private boolean checked = false ;
	private final StringBuilder text = new StringBuilder() ;

	/**
		If the UICheckbox is being added to a UILayout
		then you don't have to define the position, 
		offset, or length.
	*/
	public UITextField()
	{
		this( new Vector3(), new Vector3(), new Vector3(), null ) ;
	}

	public UITextField( final Vector3 _length )
	{
		this( new Vector3(), new Vector3(), _length, null ) ;
	}

	public UITextField( final Vector3 _offset,
					   final Vector3 _length )
	{
		this( new Vector3(), _offset, _length, null ) ;
	}

	public UITextField( final Vector3 _position,
					   final Vector3 _offset,
					   final Vector3 _length )
	{
		this( _position, _offset, _length, null ) ;
	}

	public UITextField( final Vector3 _position,
					   final Vector3 _offset,
					   final Vector3 _length,
					   final BaseListener<UIButton> _listener )
	{
		super( _position, _offset, _length ) ;
		addListener( _listener ) ;
	}

	public StringBuilder getText()
	{
		return text ;
	}

	public static UIListener createUIListener( final String _text,
											   final MalletFont _font,
											   final MalletTexture _sheet,
											   final UIElement.UV _uv )
	{
		return new UIListener( _text, _font, _sheet, _uv ) ;
	}

	public static class UIListener extends UIFactory.UIBasicListener<UITextField>
	{
		private final String placeholder ;
	
		private Draw drawEdit = null ;
		

		public UIListener( final String _text,
						   final MalletFont _font,
						   final MalletTexture _sheet,
						   final UIElement.UV _uv )
		{
			super( _text, _font, _sheet, _uv ) ;
			placeholder = _text ;
		}

		@Override
		public void constructDraws()
		{
			super.constructDraws() ;
			final MalletFont font = getFont() ;
			final MalletColour colour = getColour() ;

			if( font != null )
			{
				final UITextField parent = getParent() ;
				final Vector3 length = parent.getLength() ;
				final StringBuilder edit = parent.getText() ;

				final Vector3 textOffset = new Vector3( parent.getOffset() ) ;
				textOffset.add( length.x / 2, length.y / 2, 0.0f ) ;

				final int layer = parent.getLayer() ;

				drawEdit = DrawAssist.createTextDraw( edit,
													  font,
													  parent.getPosition(),
													  textOffset,
													  new Vector3(),
													  new Vector3( 1, 1, 1 ),
													  layer + 1 ) ;
				DrawAssist.amendTextLength( drawEdit, font.stringIndexWidth( edit, length.x ) ) ;
				DrawAssist.amendColour( drawEdit, colour ) ;
				DrawAssist.amendUI( drawEdit, true ) ;
			}
		}

		/**
			Called when listener receives a valid DrawDelegate
			and when the parent UIElement is flagged as visible.
		*/
		@Override
		public void addDraws( final DrawDelegate<World, Draw> _delegate, final World _world )
		{
			super.addDraws( _delegate, _world ) ;
			if( drawEdit != null )
			{
				_delegate.addTextDraw( drawEdit, _world ) ;
			}
		}

		/**
			Only called if there is a valid DrawDelegate and 
			when the parent UIElement is flagged as invisible.
		*/
		@Override
		public void removeDraws( final DrawDelegate<World, Draw> _delegate )
		{
			super.removeDraws( _delegate ) ;
			_delegate.removeDraw( drawEdit ) ;
		}

		@Override
		public void refresh()
		{
			super.refresh() ;
			if( drawEdit != null )
			{
				final UITextField parent = getParent() ;
				final Vector3 length = parent.getLength() ;
				final MalletFont font = getFont() ;
				final StringBuilder edit = parent.getText() ;

				final Vector3 textOffset = DrawAssist.getOffset( drawEdit ) ;
				textOffset.setXYZ( getOffset() ) ;

				final MalletFont.Metrics metrics = font.getMetrics() ;
				final float x = UI.align( drawTextAlignmentX, font.stringWidth( edit ), length.x ) ;
				final float y = UI.align( drawTextAlignmentY, metrics.getHeight(), length.y ) ;

				textOffset.add( x, y, 0.0f ) ;

				DrawAssist.amendTextLength( drawEdit, font.stringIndexWidth( edit, length.x ) ) ;
				DrawAssist.amendOrder( drawEdit, parent.getLayer() + 1 ) ;
				DrawAssist.forceUpdate( drawEdit ) ;
			}
		}

		@Override
		public InputEvent.Action keyPressed( final InputEvent _input )
		{
			switch( _input.getKeyCode() )
			{
				case UP           :
				case DOWN         :
				case LEFT         :
				case RIGHT        :
				case F1           :
				case F2           :
				case F3           :
				case F4           :
				case F5           :
				case F6           :
				case F7           :
				case F8           :
				case F9           :
				case F10          :
				case F11          :
				case F12          :
				case DELETE       :
				case HOME         :
				case END          :
				case PAGE_UP      :
				case PAGE_DOWN    :
				case PRINT_SCREEN :
				case SCROLL_LOCK  :
				case INSERT       :
				case ESCAPE       :
				case SHIFT        :
				case CTRL         :
				case ALT          :
				case ALTGROUP     :
				case META         :
				case ENTER        :
				case TAB          :
				case CAPS_LOCK    :
				case WINDOWS      : break ;
				case BACKSPACE    :
				{
					final StringBuilder edit = getParent().getText() ;
					final int length = edit.length() ;
					if( length > 0 )
					{
						edit.setLength( length - 1 ) ;
						getParent().makeDirty() ;
					}
					break ;
				}
				default :
				{
					final StringBuilder edit = getParent().getText() ;
					edit.append( _input.getKeyCharacter() ) ;
					getParent().makeDirty() ;
					break ;
				}
			}
			return InputEvent.Action.CONSUME ;
		}

		@Override
		public InputEvent.Action touchReleased( final InputEvent _input )
		{
			return super.mouseReleased( _input ) ;
		}

		@Override
		public InputEvent.Action mouseReleased( final InputEvent _input )
		{
			final StringBuilder txt = getText() ;
			if( txt.length() > 0 )
			{
				txt.setLength( 0 ) ;
				getParent().makeDirty() ;
			}
			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public void disengage()
		{
			final StringBuilder edit = getParent().getText() ;
			if( edit.length() <= 0 && getText().length() <= 0 )
			{
				getText().append( placeholder ) ;
				getParent().makeDirty() ;
			}
		}
	}
}
