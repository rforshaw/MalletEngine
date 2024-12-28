package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;
import com.linxonline.mallet.input.* ;

public class GUIEditText extends GUIText
{
	private boolean onlyNumbers = false ;
	private boolean editing = false ;
	private boolean blinkCursor = false ;

	private Draw cursorDraw = new Draw() ;
	private DrawUpdater cursorUpdater ;
	private GeometryBuffer cursorGeometry ;

	private final TextDraw drawPlaceholder = new TextDraw() ;

	private boolean shift = false ;
	private int start = 0 ;
	private int end = 0 ;

	private final Connect.Slot<UITextField> elementDisengagedSlot =  new Connect.Slot<UITextField>()
	{
		@Override
		public void slot( final UITextField _textfield )
		{
			editing = false ;
			_textfield.makeDirty() ;
		}
	} ;

	public GUIEditText( final Meta _meta, final UITextField _parent )
	{
		super( _meta, _parent.getText(), _parent ) ;
		UIElement.connect( _parent, _parent.elementDisengaged(), elementDisengagedSlot ) ;

		drawPlaceholder.getText().append( _meta.getPlaceholder() ) ;
		onlyNumbers = _meta.isOnlyNumbers() ;

		constructDraws() ;
	}

	private void constructDraws()
	{
		final UITextField parent = getParent() ;

		final Colour colour = getColour() ;
		final Vector3 position = getPosition() ;
		final Vector3 offset = getOffset() ;

		{
			final UIRatio ratio = parent.getRatio() ;
			final float width = ratio.toPixelX( 0.05f ) ;
			final float height = ratio.toPixelY( 0.42f ) ;
		
			final Shape plane = new Shape( Shape.Style.FILL, 6, 4 ) ;
			plane.copyVertex( Shape.construct( 0,     0,      0,   colour) ) ;
			plane.copyVertex( Shape.construct( 0,     height, 0,   colour ) ) ;
			plane.copyVertex( Shape.construct( width, height, 0,   colour ) ) ;
			plane.copyVertex( Shape.construct( width, 0,      0,   colour ) ) ;

			plane.addIndex( 0 ) ;
			plane.addIndex( 1 ) ;
			plane.addIndex( 2 ) ;

			plane.addIndex( 0 ) ;
			plane.addIndex( 2 ) ;
			plane.addIndex( 3 ) ;

			cursorDraw = new Draw( position.x, position.y, position.z,
								   offset.x, offset.y, offset.z ) ;
			cursorDraw.setShape( plane ) ;
		}

		final Font font = getFont() ;
		if( font != null )
		{
			final Vector3 length = getLength() ;
			final Vector3 margin = getMargin() ;
			final StringBuilder placeholder = drawPlaceholder.getText() ;

			final Font.Metrics metrics = font.getMetrics() ;
			final float offsetX = UI.align( drawAlignmentX, font.stringWidth( placeholder ), length.x - margin.x ) ;
			final float offsetY = UI.align( drawAlignmentY, metrics.getHeight(), length.y - margin.y ) ;

			drawPlaceholder.setPositionInstant( position.x + margin.x, position.y + margin.y, position.z + margin.z ) ;
			drawPlaceholder.setOffsetInstant( offsetX, offsetY, 0.0f ) ;
			drawPlaceholder.setBoundary( length.x - margin.x, length.y - margin.y ) ;

			drawPlaceholder.setRange( 0, font.stringIndexWidth( placeholder, length.x - margin.x ) ) ;
			drawPlaceholder.setColour( colour ) ;
		}
	}

	/**
		Called when listener receives a valid DrawDelegate
		and when the parent UIElement is flagged as visible.
	*/
	@Override
	public void addDraws( final World _world )
	{
		TextUpdater updater = getUpdater() ;
		if( updater != null )
		{
			// Remove the draw object from the previous 
			// updater the draw may have changed significantly.
			final TextBuffer geometry = updater.getBuffer( 0 ) ;
			geometry.removeDraws( drawPlaceholder ) ;
			updater.forceUpdate() ;
		}

		super.addDraws( _world ) ;

		cursorUpdater = GUI.getDrawUpdater( _world, ( Shape )cursorDraw.getShape(), getLayer() ) ;
		cursorGeometry = cursorUpdater.getBuffer( 0 ) ;
		cursorGeometry.addDraws( cursorDraw ) ;

		updater = getUpdater() ;

		final TextBuffer geometry = updater.getBuffer( 0 ) ;
		geometry.addDraws( drawPlaceholder ) ;

		final UITextField parent = getParent() ;
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
		final TextUpdater updater = getUpdater() ;
		if( updater != null )
		{
			final TextBuffer geometry = updater.getBuffer( 0 ) ;
			geometry.removeDraws( drawPlaceholder ) ;
			updater.forceUpdate() ;
		}

		cursorGeometry.removeDraws( cursorDraw ) ;
		cursorUpdater.forceUpdate() ;
	}

	@Override
	public void layerUpdated( final int _layer )
	{
		final TextUpdater updater = getUpdater() ;
		if( updater != null )
		{
			final TextBuffer geometry = updater.getBuffer( 0 ) ;
			geometry.removeDraws( drawPlaceholder ) ;
			updater.forceUpdate() ;
		}

		super.layerUpdated( _layer ) ;

		cursorGeometry.removeDraws( cursorDraw ) ;
		cursorUpdater.forceUpdate() ;

		cursorUpdater = GUI.getDrawUpdater( getWorld(), ( Shape )cursorDraw.getShape(), _layer ) ;
		cursorGeometry = cursorUpdater.getBuffer( 0 ) ;
		cursorGeometry.addDraws( cursorDraw ) ;
	}

	@Override
	public void refresh()
	{
		super.refresh() ;

		final UITextField parent = getParent() ;
		final int layer = parent.getLayer() + 1 ;

		final Font font = getFont() ;
		final Vector3 position = getPosition() ;
		final Vector3 offset = getOffset() ;
		final Vector3 length = getLength() ;
		final Vector3 margin = getMargin() ;

		final StringBuilder edit = getText() ;

		updateTextRange() ;

		if( isEditing() == true )
		{
			cursorDraw.setPositionInstant( position.x + margin.x, position.y + margin.y, position.z + margin.z ) ;
			final int index = parent.getCursorIndex() ;

			final float xOffset = offset.x + font.stringWidth( edit, start, index ) ;
			cursorDraw.setOffsetInstant( xOffset, offset.y, offset.z ) ;
			cursorDraw.setHidden( false ) ;
			cursorUpdater.forceUpdate() ;

			drawPlaceholder.setHidden( true ) ;
			getUpdater().forceUpdate() ;
		}
		else
		{
			cursorDraw.setHidden( true ) ;
			cursorUpdater.forceUpdate() ;

			final StringBuilder text = getText() ;
			drawPlaceholder.setHidden( text.length() > 0 ) ;

			final StringBuilder placeholder = drawPlaceholder.getText() ;
			final Font.Metrics metrics = font.getMetrics() ;
			final float offsetX = UI.align( drawAlignmentX, font.stringWidth( placeholder ), length.x - margin.x ) ;
			final float offsetY = UI.align( drawAlignmentY, metrics.getHeight(), length.y - margin.y ) ;

			drawPlaceholder.setPosition( position.x + margin.x, position.y + margin.y, position.z + margin.z ) ;
			drawPlaceholder.setOffset( offsetX, offsetY, 0.0f ) ;
			drawPlaceholder.setBoundary( length.x - margin.x, length.y - margin.y ) ;

			drawPlaceholder.setRange( 0, font.stringIndexWidth( placeholder, length.x - margin.x ) ) ;

			getUpdater().forceUpdate() ;
		}
	}

	@Override
	public void shutdown()
	{
		final UITextField parent = getParent() ;
		UITextField.disconnect( parent, parent.elementDisengaged(), elementDisengagedSlot ) ;

		super.shutdown() ;
		cursorUpdater = null ;
		cursorGeometry = null ;
	}

	private boolean isEditing()
	{
		return editing ;
	}

	private void updateTextRange()
	{
		final Font font = getFont() ;
		final Vector3 length = getLength() ;
		final Vector3 margin = getMargin() ;
		final StringBuilder edit = getText() ;

		final int index = getParent().getCursorIndex() ;
		end = font.stringIndexWidth( edit, start, length.x - margin.x ) ;

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
	public InputEvent.Action keyReleased( final InputEvent _input )
	{
		if( isEditing() == false )
		{
			return InputEvent.Action.PROPAGATE ;
		}

		switch( _input.getKeyCode() )
		{
			case SHIFT        :
			{
				shift = false ;
				break ;
			}
		}

		return InputEvent.Action.CONSUME ;
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
			case CTRL         :
			case ALT          :
			case ALTGROUP     :
			case META         :
			case ENTER        :
			case TAB          :
			case CAPS_LOCK    :
			case WINDOWS      : break ;
			case SHIFT        :
			{
				shift = true ;
				break ;
			}
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
			default : incrementChar( _input.getKeyCharacter() ) ; break ;
		}

		return InputEvent.Action.CONSUME ;
	}

	private void incrementChar( final char _char )
	{
		final UITextField parent = getParent() ;
		final int index = parent.getCursorIndex() ;
		final StringBuilder edit = getText() ;
		edit.insert( index, _char ) ;

		if( onlyNumbers )
		{
			if( isNumber( edit ) == false )
			{
				edit.deleteCharAt( index ) ;
				return ;
			}
		}

		parent.setCursorIndex( index + 1 ) ;
		parent.makeDirty() ;

		UIElement.signal( parent, parent.textChanged() ) ;
	}

	private static boolean isNumber( final StringBuilder _builder )
	{
		final int length = _builder.length() ;
		if( length <= 0 )
		{
			return false ;
		}

		final char first = _builder.charAt( 0 ) ;
		if( first != '-' && Character.isDigit( first ) == false )
		{
			return false ;
		}

		int decimal = 0 ;
		for( int i = 1; i < length; ++i )
		{
			final char c = _builder.charAt( i ) ;
			decimal += ( c == '.' ) ? 1 : 0 ;

			if( decimal > 1 )
			{
				return false ;
			}
			
			if( c != '.' && Character.isDigit( c ) == false )
			{
				return false ;
			}
		}

		return true ;
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
		final Font font = getFont() ;

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
		private final UIVariant placeholder = new UIVariant( "PLACEHOLDER", "", new Connect.Signal() ) ;
		private final UIVariant onlyNumbers = new UIVariant( "ONLY_NUMBERS", false, new Connect.Signal() ) ;

		public Meta()
		{
			super() ;

			int row = rowCount( root() ) ;
			createData( null, row + 2, 1 ) ;

			setData( new UIModelIndex( root(), row++, 0 ), placeholder, UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), onlyNumbers, UIAbstractModel.Role.User ) ;
		}

		@Override
		public String getType()
		{
			return "UITEXTFIELD_GUIEDITTEXT" ;
		}

		public void setOnlyNumbers( final boolean _enable )
		{
			if( _enable != onlyNumbers.toBool() )
			{
				onlyNumbers.setBool( _enable ) ;
				UIElement.signal( this, onlyNumbers.getSignal() ) ;
			}
		}

		public boolean isOnlyNumbers()
		{
			return onlyNumbers.toBool() ;
		}

		public Connect.Signal onlyNumbersChanged()
		{
			return placeholder.getSignal() ;
		}

		public void setPlaceholder( final String _text )
		{
			if( _text != null && placeholder.toString().equals( _text ) == false )
			{
				placeholder.setString( _text ) ;
				UIElement.signal( this, placeholder.getSignal() ) ;
			}
		}

		public String getPlaceholder()
		{
			return placeholder.toString() ;
		}

		public Connect.Signal placeholderChanged()
		{
			return placeholder.getSignal() ;
		}
	}
}
