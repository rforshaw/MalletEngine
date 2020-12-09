package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;
import com.linxonline.mallet.input.* ;

public class GUIEditText extends GUIText
{
	private boolean editing = false ;
	private boolean blinkCursor = false ;

	private final Program cursorProgram = ProgramAssist.add( new Program( "SIMPLE_GEOMETRY" ) );
	private final Draw cursorDraw = new Draw() ;
	private IUpdater<Draw, ?> cursorUpdater ;

	private int start = 0 ;
	private int end = 0 ;

	public GUIEditText( final Meta _meta, final UITextField _parent )
	{
		super( _meta, _parent.getText(), _parent ) ;
		UIElement.connect( _parent, _parent.elementDisengaged(), new Connect.Slot<UITextField>()
		{
			@Override
			public void slot( final UITextField _textfield )
			{
				editing = false ;
				_textfield.makeDirty() ;
			}
		} ) ;

		constructDraws() ;
	}

	private void constructDraws()
	{
		final UITextField parent = getParent() ;

		final MalletColour colour = getColour() ;
		final Vector3 position = getPosition() ;
		final Vector3 offset = getOffset() ;

		{
			final UIRatio ratio = parent.getRatio() ;
			final float width = ratio.toPixelX( 0.05f ) ;
			final float height = ratio.toPixelY( 0.42f ) ;
		
			final Shape triangle = new Shape( Shape.Style.FILL, 4, 4 ) ;
			triangle.addVertex( Shape.construct( 0,     0,      0,   colour) ) ;
			triangle.addVertex( Shape.construct( 0,     height, 0,   colour ) ) ;
			triangle.addVertex( Shape.construct( width, height, 0,   colour ) ) ;
			triangle.addVertex( Shape.construct( width, 0,      0,   colour ) ) ;

			triangle.addIndex( 0 ) ;
			triangle.addIndex( 1 ) ;
			triangle.addIndex( 2 ) ;
			triangle.addIndex( 3 ) ;

			cursorDraw.setPosition( position.x, position.y, position.z ) ;
			cursorDraw.setOffset( offset.x, offset.y, offset.z ) ;
			cursorDraw.setShape( Shape.triangulate( triangle ) ) ;
		}
	}

	/**
		Called when listener receives a valid DrawDelegate
		and when the parent UIElement is flagged as visible.
	*/
	@Override
	public void addDraws( final World _world )
	{
		super.addDraws( _world ) ;
		final UITextField parent = getParent() ;

		if( isEditing() == true )
		{
			cursorUpdater = DrawUpdater.getOrCreate( _world, cursorProgram, cursorDraw.getShape(), true, getLayer() ) ;
			cursorUpdater.addDynamics( cursorDraw ) ;
		}

		parent.makeDirty() ;
	}

	/**
		Only called if there is a valid DrawDelegate and 
		when the parent UIElement is flagged as invisible.
	*/
	@Override
	public void removeDraws()
	{
		super.removeDraws() ;
		if( cursorUpdater != null )
		{
			cursorUpdater.removeDynamics( cursorDraw ) ;
		}
	}

	@Override
	public void layerUpdated( final int _layer )
	{
		super.layerUpdated( _layer ) ;

		{
			if( cursorUpdater != null )
			{
				cursorUpdater.removeDynamics( cursorDraw ) ;
			}

			cursorUpdater = DrawUpdater.getOrCreate( getWorld(), cursorProgram, cursorDraw.getShape(), true, _layer ) ;
		}
	}

	@Override
	public void refresh()
	{
		super.refresh() ;

		final UITextField parent = getParent() ;
		final int layer = parent.getLayer() + 1 ;

		final MalletFont font = getFont() ;
		final Vector3 position = getPosition() ;
		final Vector3 offset = getOffset() ;
		final Vector3 length = getLength() ;
		final StringBuilder edit = getText() ;

		{
			//final MalletFont.Metrics metrics = font.getMetrics() ;
			//offset.x = UI.align( getAlignmentX(), font.stringWidth( edit ), length.x ) ;
			//offset.y = UI.align( getAlignmentY(), metrics.getHeight(), length.y ) ;

			updateTextRange() ;
		}

		if( isEditing() == true )
		{
			cursorDraw.setPosition( position.x, position.y, position.z ) ;
			final int index = parent.getCursorIndex() ;

			final float xOffset = offset.x + font.stringWidth( edit, start, index ) ;
			cursorDraw.setOffset( xOffset, offset.y, offset.z ) ;

			if( cursorUpdater != null )
			{
				cursorUpdater.addDynamics( cursorDraw ) ;
			}
		}
		else
		{
			if( cursorUpdater != null )
			{
				cursorUpdater.removeDynamics( cursorDraw ) ;
			}
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
		final StringBuilder edit = getText() ;

		final int index = getParent().getCursorIndex() ;
		end = font.stringIndexWidth( edit, start, length.x ) ;

		final int temp = end ;
		end = ( index > end ) ? index : end ;
		end = ( end < edit.length() ) ? end : edit.length() ;

		start += end - temp ;
		start = ( index < start ) ? index : start ;
		start = ( start >= 0 ) ? start : 0 ;

		final TextDraw draw = getTextDraw() ;
		draw.setRange( start, end ) ;
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
				final UITextField parent = getParent() ;
				final int index = parent.getCursorIndex() - 1 ;
				parent.setCursorIndex( ( index < 0 ) ? 0 : index ) ;
				parent.makeDirty() ;
				break ;
			}
			case RIGHT        :
			{
				final UITextField parent = getParent() ;
				final StringBuilder edit = getText() ;

				final int index = parent.getCursorIndex() + 1 ;
				parent.setCursorIndex( ( index >= edit.length() ) ? edit.length() : index ) ;
				parent.makeDirty() ;
				break ;
			}
			case DELETE       :
			{
				final UITextField parent = getParent() ;
				final int index = parent.getCursorIndex() ;
				final StringBuilder edit = getText() ;
				final int length = edit.length() ;
				if( index >= 0 && index < length )
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
				final UITextField parent = getParent() ;
				int index = parent.getCursorIndex() ;
				final StringBuilder edit = getText() ;
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
				final UITextField parent = getParent() ;
				final int index = parent.getCursorIndex() ;
				final StringBuilder edit = getText() ;
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

		final UITextField parent = getParent() ;
		final Vector3 position = parent.getPosition() ;
		final StringBuilder text = getText() ;
		final MalletFont font = getFont() ;

		final float width = _input.getMouseX() - position.x ;
		parent.setCursorIndex( font.stringIndexWidth( text, width ) ) ;

		parent.makeDirty() ;
		return InputEvent.Action.PROPAGATE ;
	}

	public UITextField getParent()
	{
		return ( UITextField )super.getParent() ;
	}
	
	public static class Meta extends GUIText.Meta
	{
		public Meta()
		{
			super() ;
		}

		@Override
		public String getType()
		{
			return "UITEXTFIELD_GUIEDITTEXT" ;
		}
	}
}
