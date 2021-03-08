package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public class GUIText extends GUIComponent
{
	protected UI.Alignment drawAlignmentX = UI.Alignment.CENTRE ;
	protected UI.Alignment drawAlignmentY = UI.Alignment.CENTRE ;

	private MalletFont font ;
	private MalletColour colour = MalletColour.white() ;

	private TextUpdater updater ;
	private final Program program = ProgramAssist.add( new Program( "SIMPLE_FONT" ) ) ;
	private final TextDraw drawText ;

	public GUIText( final Meta _meta, final UIElement _parent )
	{
		this( _meta, new StringBuilder(), _parent ) ;
	}

	public GUIText( final Meta _meta, final StringBuilder _text, final UIElement _parent )
	{
		super( _meta, _parent ) ;
		setLayerOffset( 1 ) ;
		drawAlignmentX = _meta.getAlignmentX() ;
		drawAlignmentY = _meta.getAlignmentY() ;

		drawText = new TextDraw( _text ) ;
		drawText.getText().append( _meta.getText() ) ;

		font = _meta.getFont() ;
		colour = _meta.getColour( colour ) ;
		constructDraws() ;
	}

	public void setAlignment( final UI.Alignment _x, final UI.Alignment _y )
	{
		drawAlignmentX = ( _x == null ) ? UI.Alignment.CENTRE : _x ;
		drawAlignmentY = ( _y == null ) ? UI.Alignment.CENTRE : _y ;
	}

	public void setColour( final MalletColour _colour )
	{
		colour = ( _colour != null ) ? _colour : MalletColour.white() ;
	}

	/**
		Can be used to construct Draw objects before a 
		DrawDelegate is provided by the Rendering System.
	*/
	private void constructDraws()
	{
		if( font != null )
		{
			final Vector3 length = getLength() ;
			final Vector3 position = getPosition() ;
			final Vector3 offset = getOffset() ;

			final MalletFont.Metrics metrics = font.getMetrics() ;
			offset.x = UI.align( drawAlignmentX, font.stringWidth( getText() ), length.x ) ;
			offset.y = UI.align( drawAlignmentY, metrics.getHeight(), length.y ) ;

			drawText.setPosition( position.x, position.y, position.z ) ;
			drawText.setOffset( offset.x, offset.y, offset.z ) ;

			drawText.setRange( 0, font.stringIndexWidth( getText(), length.x ) ) ;
			drawText.setColour( colour ) ;

			program.mapUniform( "inTex0", font ) ;
		}
	}

	/**
		Called when listener receives a valid DrawDelegate
		and when the parent UIElement is flagged as visible.
	*/
	@Override
	public void addDraws( final World _world )
	{
		if( updater != null )
		{
			// Remove the draw object from the previous 
			// updater the draw may have changed significantly.
			updater.removeDynamics( drawText ) ;
		}

		updater = TextUpdater.getOrCreate( getWorld(), program, true, getLayer() ) ;
		updater.addDynamics( drawText ) ;
	}

	/**
		Only called if there is a valid DrawDelegate and 
		when the parent UIElement is flagged as invisible.
	*/
	@Override
	public void removeDraws()
	{
		if( updater != null )
		{
			updater.removeDynamics( drawText ) ;
		}
	}

	@Override
	public void layerUpdated( final int _layer )
	{
		if( updater != null )
		{
			updater.removeDynamics( drawText ) ;
		}

		updater = TextUpdater.getOrCreate( getWorld(), program, true, _layer ) ;
	}

	@Override
	public void refresh()
	{
		super.refresh() ;
		if( drawText != null && getParent().isVisible() )
		{
			final Vector3 position = getPosition() ;
			final Vector3 length = getLength() ;
			final Vector3 offset = getOffset() ;

			final MalletFont.Metrics metrics = font.getMetrics() ;
			offset.x = UI.align( drawAlignmentX, font.stringWidth( getText() ), length.x ) ;
			offset.y = UI.align( drawAlignmentY, metrics.getHeight(), length.y ) ;

			drawText.setPosition( position.x, position.y, position.z ) ;
			drawText.setOffset( offset.x, offset.y, offset.z ) ;

			drawText.setRange( 0, font.stringIndexWidth( getText(), length.x ) ) ;

			drawText.makeDirty() ;
			updater.forceUpdate() ;
		}
	}

	public void setRange( final int _start, final int _end )
	{
		drawText.setRange( _start, _end ) ;
	}

	public StringBuilder getText()
	{
		return drawText.getText() ;
	}

	public UI.Alignment getAlignmentX()
	{
		return drawAlignmentX ;
	}

	public UI.Alignment getAlignmentY()
	{
		return drawAlignmentY ;
	}

	public IUpdater<TextDraw, TextBuffer> getUpdater()
	{
		return updater ;
	}

	public Program getProgram()
	{
		return program ;
	}

	public TextDraw getTextDraw()
	{
		return drawText ;
	}

	public MalletFont getFont()
	{
		return font ;
	}

	public MalletColour getColour()
	{
		return colour ;
	}

	public static class Meta extends GUIComponent.Meta
	{
		private final UIVariant xAlign = new UIVariant( "ALIGNMENT_X",  UI.Alignment.CENTRE,    new Connect.Signal() ) ;
		private final UIVariant yAlign = new UIVariant( "ALIGNMENT_Y",  UI.Alignment.CENTRE,    new Connect.Signal() ) ;

		private final UIVariant text   = new UIVariant( "TEXT",   "",                        new Connect.Signal() ) ;
		private final UIVariant colour = new UIVariant( "COLOUR", MalletColour.white(),      new Connect.Signal() ) ;
		private final UIVariant font   = new UIVariant( "FONT",   new MalletFont( "Arial" ), new Connect.Signal() ) ;

		public Meta()
		{
			super() ;

			int row = rowCount( root() ) ;
			createData( null, row + 5, 1 ) ;

			setData( new UIModelIndex( root(), row++, 0 ), xAlign, UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), yAlign, UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), text, UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), colour, UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), font, UIAbstractModel.Role.User ) ;
		}

		@Override
		public String getType()
		{
			return "UIELEMENT_GUITEXT" ;
		}

		public void setAlignment( final UI.Alignment _x, final UI.Alignment _y )
		{
			if( _x != null && _x != xAlign.toObject( UI.Alignment.class ) )
			{
				xAlign.setObject( _x ) ;
				UIElement.signal( this, xAlign.getSignal() ) ;
			}

			if( _y != null && _y != yAlign.toObject( UI.Alignment.class ) )
			{
				yAlign.setObject( _y ) ;
				UIElement.signal( this, yAlign.getSignal() ) ;
			}
		}

		public void setText( final String _text )
		{
			if( _text != null && text.toString().equals( _text ) == false )
			{
				text.setString( _text ) ;
				UIElement.signal( this, text.getSignal() ) ;
			}
		}

		public void setColour( final MalletColour _colour )
		{
			final MalletColour col = colour.toObject( MalletColour.class ) ;
			if( _colour != null && col.equals( _colour ) == false )
			{
				col.changeColour( _colour.toInt() ) ;
				UIElement.signal( this, colour.getSignal() ) ;
			}
		}

		public void setFont( final MalletFont _font )
		{
			final MalletFont temp = font.toObject( MalletFont.class ) ;
			if( temp.equals( _font ) == false )
			{
				font.setObject( new MalletFont( _font.getFontName(), _font.getStyle(), _font.getPointSize() ) ) ;
				UIElement.signal( this, font.getSignal() ) ;
			}
		}
		
		public UI.Alignment getAlignmentX()
		{
			return xAlign.toObject( UI.Alignment.class ) ;
		}

		public UI.Alignment getAlignmentY()
		{
			return yAlign.toObject( UI.Alignment.class ) ;
		}

		public String getText()
		{
			return text.toString() ;
		}

		public MalletColour getColour( final MalletColour _populate )
		{
			final MalletColour col = colour.toObject( MalletColour.class ) ;
			_populate.changeColour( col.toInt() ) ;
			return _populate ;
		}

		public MalletFont getFont()
		{
			return font.toObject( MalletFont.class )  ;
		}

		public Connect.Signal xAlignChanged()
		{
			return xAlign.getSignal() ;
		}

		public Connect.Signal yAlignChanged()
		{
			return yAlign.getSignal() ;
		}

		public Connect.Signal textChanged()
		{
			return text.getSignal() ;
		}

		public Connect.Signal colourChanged()
		{
			return colour.getSignal() ;
		}

		public Connect.Signal fontChanged()
		{
			return font.getSignal() ;
		}
	}
}
