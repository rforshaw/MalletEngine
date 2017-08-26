 package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

public class UITextField extends UIElement
{
	private final StringBuilder text = new StringBuilder() ;

	private int cursorIndex = 0 ;

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

	public void setCursorIndex( final int _index )
	{
		cursorIndex = _index ;
	}

	public int getCursorIndex()
	{
		return cursorIndex ;
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
		private boolean editing = false ;
		private boolean blinkCursor = false ;

		private DrawDelegate<World, Draw> delegate = null ;
		private World world ;

		private Draw drawPlaceholder = null ;
		private Draw drawEdit = null ;
		private Draw drawCursor = null ;

		public UIListener( final String _text,
						   final MalletFont _font,
						   final MalletTexture _sheet,
						   final UIElement.UV _uv )
		{
			super( null, _font, _sheet, _uv ) ;
			placeholder = _text ;
		}

		@Override
		public void constructDraws()
		{
			super.constructDraws() ;
			final MalletFont font = getFont() ;
			final MalletColour colour = getColour() ;

			final UITextField parent = getParent() ;
			final UIRatio ratio = parent.getRatio() ;
			final Vector3 length = parent.getLength() ;
			final int layer = parent.getLayer() + 1 ;

			final Vector3 textOffset = new Vector3( parent.getOffset() ) ;
			textOffset.add( length.x / 2, length.y / 2, 0.0f ) ;

			final StringBuilder edit = parent.getText() ;

			{
				drawEdit = DrawAssist.createTextDraw( edit,
													  font,
													  parent.getPosition(),
													  new Vector3( textOffset ),
													  new Vector3(),
													  new Vector3( 1, 1, 1 ),
													  layer ) ;

				DrawAssist.amendColour( drawEdit, colour ) ;
				DrawAssist.amendUI( drawEdit, true ) ;
			}

			{
				drawPlaceholder = DrawAssist.createTextDraw( placeholder,
															 font,
															 parent.getPosition(),
															 new Vector3( textOffset ),
															 new Vector3(),
															 new Vector3( 1, 1, 1 ),
															 layer ) ;

				DrawAssist.amendColour( drawPlaceholder, colour ) ;
				DrawAssist.amendUI( drawPlaceholder, true ) ;
			}

			{
				final float width = ratio.toPixelX( 0.05f ) ;
				final float height = ratio.toPixelY( 0.42f ) ;
			
				final Shape triangle = Shape.create( Shape.Style.FILL, 4, 4 ) ;
				triangle.addVertex( Shape.construct( 0,     0,      0,   colour) ) ;
				triangle.addVertex( Shape.construct( 0,     height, 0,   colour ) ) ;
				triangle.addVertex( Shape.construct( width, height, 0,   colour ) ) ;
				triangle.addVertex( Shape.construct( width, 0,      0,   colour ) ) ;

				triangle.addIndex( 0 ) ;
				triangle.addIndex( 1 ) ;
				triangle.addIndex( 2 ) ;
				triangle.addIndex( 3 ) ;

				drawCursor = DrawAssist.createDraw( parent.getPosition(),
													new Vector3( textOffset ),
													new Vector3(),
													new Vector3( 1, 1, 1 ),
													layer ) ;

				DrawAssist.amendShape( drawCursor, Shape.triangulate( triangle ) ) ;
				DrawAssist.attachProgram( drawCursor, ProgramAssist.create( "SIMPLE_GEOMETRY" ) ) ;
				DrawAssist.amendUI( drawCursor, true ) ;
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
			delegate = _delegate ;
			world = _world ;

			delegate.addTextDraw( drawEdit, world ) ;

			final StringBuilder edit = getParent().getText() ;
			if( isEditing() == false && edit.length() <= 0 )
			{
				delegate.addTextDraw( drawPlaceholder, world ) ;
			}

			if( isEditing() == true )
			{
				delegate.addBasicDraw( drawCursor, world ) ;
			}
			getParent().makeDirty() ;
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
			_delegate.removeDraw( drawPlaceholder ) ;
			_delegate.removeDraw( drawCursor ) ;
		}

		@Override
		public void refresh()
		{
			super.refresh() ;
			if( delegate == null )
			{
				return ;
			}

			final MalletFont font = getFont() ;
			final StringBuilder edit = getParent().getText() ;

			final UITextField parent = getParent() ;
			final Vector3 length = parent.getLength() ;
			final int layer = parent.getLayer() + 1 ;

			applyTextOffset( drawEdit ) ;
			DrawAssist.amendTextLength( drawEdit, font.stringIndexWidth( edit, length.x ) ) ;
			DrawAssist.amendOrder( drawEdit, layer ) ;
			DrawAssist.forceUpdate( drawEdit ) ;

			if( isEditing() == false && edit.length() <= 0 )
			{
				delegate.addTextDraw( drawPlaceholder, world ) ;

				applyTextOffset( drawPlaceholder ) ;
				DrawAssist.amendOrder( drawPlaceholder, layer ) ;
				DrawAssist.forceUpdate( drawPlaceholder ) ;
			}
			else
			{
				delegate.removeDraw( drawPlaceholder ) ;
			}

			if( isEditing() == true )
			{
				delegate.addBasicDraw( drawCursor, world ) ;

				final int index = getParent().getCursorIndex() ;

				applyTextOffset( drawCursor ) ;
				final Vector3 textOffset = DrawAssist.getOffset( drawCursor ) ;
				textOffset.x += getFont().stringWidth( edit, 0, index ) ;

				DrawAssist.amendOrder( drawCursor, layer ) ;
				DrawAssist.forceUpdate( drawCursor ) ;
			}
			else
			{
				delegate.removeDraw( drawCursor ) ;
			}
		}

		private void applyTextOffset( final Draw _draw )
		{
			final UITextField parent = getParent() ;
			final StringBuilder edit = parent.getText() ;
			final Vector3 length     = parent.getLength() ;
			final MalletFont font    = getFont() ;

			final Vector3 textOffset = DrawAssist.getOffset( _draw ) ;
			textOffset.setXYZ( getOffset() ) ;

			final MalletFont.Metrics metrics = font.getMetrics() ;
			final float x = UI.align( drawTextAlignmentX, font.stringWidth( edit ), length.x ) ;
			final float y = UI.align( drawTextAlignmentY, metrics.getHeight(), length.y ) ;

			textOffset.add( x, y, 0.0f ) ;
		}

		private boolean isEditing()
		{
			return editing ;
		}

		@Override
		public InputEvent.Action keyPressed( final InputEvent _input )
		{
			if( isEditing() == false )
			{
				return InputEvent.Action.PROPAGATE ;
			}

			switch( _input.getKeyCode() )
			{
				case UP           :
				case DOWN         :
				case LEFT         :
				{
					final UITextField parent = getParent() ;
					final int index = parent.getCursorIndex() - 1 ;
					parent.setCursorIndex( ( index < 0 ) ? 0 : index ) ;
					parent.makeDirty() ;
					break ;
				}
				case RIGHT        :
				{
					final UITextField parent = getParent() ;
					final StringBuilder edit = parent.getText() ;

					final int index = parent.getCursorIndex() + 1 ;
					parent.setCursorIndex( ( index > edit.length() ) ? edit.length() : index ) ;
					parent.makeDirty() ;
					break ;
				}
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
				{
					final UITextField parent = getParent() ;
					final int index = parent.getCursorIndex() ;
					final StringBuilder edit = parent.getText() ;
					final int length = edit.length() ;
					if( index > 0 && index < length )
					{
						edit.deleteCharAt( index ) ;
						parent.setCursorIndex( index ) ;
						parent.makeDirty() ;
					}
					break ;
				}
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
					final UITextField parent = getParent() ;
					int index = parent.getCursorIndex() ;
					final StringBuilder edit = parent.getText() ;
					if( index > 0 )
					{
						index -= 1 ;
						edit.deleteCharAt( index ) ;
						parent.setCursorIndex( index ) ;
						parent.makeDirty() ;
					}
					break ;
				}
				default :
				{
					final UITextField parent = getParent() ;
					final int index = parent.getCursorIndex() ;
					final StringBuilder edit = getParent().getText() ;
					edit.insert( index, _input.getKeyCharacter() ) ;

					parent.setCursorIndex( index + 1 ) ;
					parent.makeDirty() ;
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
			editing = true ;

			final UITextField parent = getParent() ;
			final Vector3 position = parent.getPosition() ;
			final StringBuilder text = parent.getText() ;
			final MalletFont font = getFont() ;

			final float width = _input.getMouseX() - position.x ;
			parent.setCursorIndex( font.stringIndexWidth( text, width ) ) ;

			getParent().makeDirty() ;
			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public void disengage()
		{
			editing = false ;
			getParent().makeDirty() ;
		}
	}
}
