package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;
import com.linxonline.mallet.input.* ;

public class GUIEditText extends GUIText
{
	private boolean editing = false ;
	private boolean blinkCursor = false ;

	private Draw drawEdit = null ;
	private Draw drawCursor = null ;

	private int start = 0 ;
	private int end = 0 ;

	public GUIEditText( final Meta _meta, final UITextField _parent )
	{
		super( _meta, _parent ) ;
		UIElement.connect( _parent, _parent.elementDisengaged(), new Connect.Slot<UITextField>()
		{
			@Override
			public void slot( final UITextField _textfield )
			{
				editing = false ;
				_textfield.makeDirty() ;
			}
		} ) ;
	}

	@Override
	public void constructDraws()
	{
		super.constructDraws() ;
		final MalletFont font = getFont() ;
		final MalletColour colour = getColour() ;

		final UITextField parent = getParentTextField() ;
		final int layer = parent.getLayer() + 1 ;

		final Vector3 position = getPosition() ;
		final Vector3 offset = getOffset() ;
		final Vector3 length = getLength() ;

		final StringBuilder edit = parent.getText() ;

		{
			drawEdit = DrawAssist.createTextDraw( edit,
													font,
													position,
													offset,
													new Vector3(),
													new Vector3( 1, 1, 1 ),
													layer + 1 ) ;

			updateTextRange() ;
			DrawAssist.amendColour( drawEdit, colour ) ;
			DrawAssist.amendUI( drawEdit, true ) ;
		}

		{
			final UIRatio ratio = parent.getRatio() ;
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

			drawCursor = DrawAssist.createDraw( position,
												new Vector3( offset ),
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
	public void addDraws( final DrawDelegate _delegate, final World _world )
	{
		super.addDraws( _delegate, _world ) ;
		final UITextField parent = getParentTextField() ;
		final int layer = parent.getLayer() + 1 ;

		_delegate.addTextDraw( drawEdit, _world ) ;

		final StringBuilder edit = parent.getText() ;
		if( isEditing() == false && edit.length() <= 0 )
		{
			_delegate.addTextDraw( getDraw(), _world ) ;
		}

		if( isEditing() == true )
		{
			_delegate.addBasicDraw( drawCursor, _world ) ;
		}
		parent.makeDirty() ;
	}

	/**
		Only called if there is a valid DrawDelegate and 
		when the parent UIElement is flagged as invisible.
	*/
	@Override
	public void removeDraws( final DrawDelegate _delegate )
	{
		super.removeDraws( _delegate ) ;
		_delegate.removeDraw( drawEdit ) ;
		_delegate.removeDraw( drawCursor ) ;
	}

	@Override
	public void refresh()
	{
		super.refresh() ;
		final DrawDelegate delegate = getDrawDelegate() ;
		if( delegate == null )
		{
			return ;
		}

		final UITextField parent = getParentTextField() ;
		final int layer = parent.getLayer() + 1 ;

		final MalletFont font = getFont() ;
		final Vector3 offset = getOffset() ;
		final Vector3 length = getLength() ;
		final StringBuilder edit = parent.getText() ;

		{
			final MalletFont.Metrics metrics = font.getMetrics() ;
			offset.x = UI.align( getAlignmentX(), font.stringWidth( edit ), length.x ) ;
			offset.y = UI.align( getAlignmentY(), metrics.getHeight(), length.y ) ;

			updateTextRange() ;

			DrawAssist.amendOrder( drawEdit, layer + 1 ) ;
			DrawAssist.forceUpdate( drawEdit ) ;
		}

		if( isEditing() == false && edit.length() <= 0 )
		{
			delegate.addTextDraw( getDraw(), getWorld() ) ;
		}
		else
		{
			delegate.removeDraw( getDraw() ) ;
		}

		if( isEditing() == true )
		{
			delegate.addBasicDraw( drawCursor, getWorld() ) ;

			final int index = parent.getCursorIndex() ;

			final Vector3 cursorOffset = DrawAssist.getOffset( drawCursor ) ;
			cursorOffset.setXYZ( offset ) ;

			cursorOffset.add( font.stringWidth( edit, start, index ), 0.0f, 0.0f ) ;
			//System.out.println( "Update Cursor: " + index + " " + cursorOffset ) ;

			DrawAssist.amendOrder( drawCursor, layer ) ;
			DrawAssist.forceUpdate( drawCursor ) ;
		}
		else
		{
			delegate.removeDraw( drawCursor ) ;
		}
	}

	private boolean isEditing()
	{
		return editing ;
	}

	private void updateTextRange()
	{
		final MalletFont font = getFont() ;
		final Vector3 length = getLength() ;
		final StringBuilder edit = getParentTextField().getText() ;

		final int index = getParentTextField().getCursorIndex() ;
		end = font.stringIndexWidth( edit, start, length.x ) ;

		final int temp = end ;
		end = ( index > end ) ? index : end ;
		end = ( end < edit.length() ) ? end : edit.length() ;

		start += end - temp ;
		start = ( index < start ) ? index : start ;
		start = ( start >= 0 ) ? start : 0 ;

		DrawAssist.amendTextStart( drawEdit, start ) ;
		DrawAssist.amendTextEnd( drawEdit, end ) ;
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
			case LEFT         :
			{
				final UITextField parent = getParentTextField() ;
				final int index = parent.getCursorIndex() - 1 ;
				parent.setCursorIndex( ( index < 0 ) ? 0 : index ) ;
				parent.makeDirty() ;
				break ;
			}
			case RIGHT        :
			{
				final UITextField parent = getParentTextField() ;
				final StringBuilder edit = parent.getText() ;

				final int index = parent.getCursorIndex() + 1 ;
				parent.setCursorIndex( ( index >= edit.length() ) ? edit.length() : index ) ;
				parent.makeDirty() ;
				break ;
			}
			case DELETE       :
			{
				final UITextField parent = getParentTextField() ;
				final int index = parent.getCursorIndex() ;
				final StringBuilder edit = parent.getText() ;
				final int length = edit.length() ;
				if( index > 0 && index < length )
				{
					edit.deleteCharAt( index ) ;
					parent.setCursorIndex( index ) ;
					parent.makeDirty() ;

					UIElement.signal( parent, parent.textChanged() ) ;
				}
				break ;
			}
			case BACKSPACE    :
			{
				final UITextField parent = getParentTextField() ;
				int index = parent.getCursorIndex() ;
				final StringBuilder edit = parent.getText() ;
				if( index > 0 )
				{
					index -= 1 ;
					edit.deleteCharAt( index ) ;
					parent.setCursorIndex( index ) ;
					parent.makeDirty() ;

					UIElement.signal( parent, parent.textChanged() ) ;
				}
				break ;
			}
			default :
			{
				final UITextField parent = getParentTextField() ;
				final int index = parent.getCursorIndex() ;
				final StringBuilder edit = parent.getText() ;
				edit.insert( index, _input.getKeyCharacter() ) ;

				parent.setCursorIndex( index + 1 ) ;
				parent.makeDirty() ;

				UIElement.signal( parent, parent.textChanged() ) ;
				break ;
			}
		}

		return InputEvent.Action.CONSUME ;
	}

	@Override
	public InputEvent.Action touchReleased( final InputEvent _input )
	{
		return mouseReleased( _input ) ;
	}

	@Override
	public InputEvent.Action mouseReleased( final InputEvent _input )
	{
		editing = true ;

		final UITextField parent = getParentTextField() ;
		final Vector3 position = parent.getPosition() ;
		final StringBuilder text = parent.getText() ;
		final MalletFont font = getFont() ;

		final float width = _input.getMouseX() - position.x ;
		parent.setCursorIndex( font.stringIndexWidth( text, width ) ) ;

		parent.makeDirty() ;
		return InputEvent.Action.PROPAGATE ;
	}

	public UITextField getParentTextField()
	{
		return ( UITextField )getParent() ;
	}
	
	public static class Meta extends GUIText.Meta
	{
		public Meta() {}

		@Override
		public String getType()
		{
			return "UITEXTFIELD_GUIEDITTEXT" ;
		}
	}
}
