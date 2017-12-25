package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public class GUIText<T extends UIElement> extends GUIBase<T>
{
	protected UI.Alignment drawAlignmentX = UI.Alignment.CENTRE ;
	protected UI.Alignment drawAlignmentY = UI.Alignment.CENTRE ;

	private final StringBuilder text = new StringBuilder() ;
	private MalletFont font ;
	private MalletColour colour = MalletColour.white() ;

	protected Draw drawText = null ;

	public GUIText( final Meta _meta )
	{
		super() ;
		setLayerOffset( 1 ) ;
		drawAlignmentX = _meta.getAlignmentX() ;
		drawAlignmentY = _meta.getAlignmentY() ;

		text.append( _meta.getText() ) ;
		font = _meta.getFont() ;
		colour = _meta.getColour( colour ) ;
	}

	public GUIText( final String _text, final MalletFont _font )
	{
		super() ;
		setLayerOffset( 1 ) ;
		font = _font ;
		if( _text != null )
		{
			text.append( _text ) ;
		}
	}

	@Override
	public void setParent( final T _parent )
	{
		super.setParent( _parent ) ;
		
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
	@Override
	public void constructDraws()
	{
		if( font != null )
		{
			final Vector3 length = getLength() ;
			final Vector3 offset = getOffset() ;

			final MalletFont.Metrics metrics = font.getMetrics() ;
			offset.x = UI.align( drawAlignmentX, font.stringWidth( text ), length.x ) ;
			offset.y = UI.align( drawAlignmentY, metrics.getHeight(), length.y ) ;

			drawText = DrawAssist.createTextDraw( text,
													font,
													getPosition(),
													getOffset(),
													new Vector3(),
													new Vector3( 1, 1, 1 ),
													getLayer()  ) ;

			DrawAssist.amendTextStart( drawText, 0 ) ;
			DrawAssist.amendTextEnd( drawText, font.stringIndexWidth( text, length.x ) ) ;
			DrawAssist.amendColour( drawText, colour ) ;
			DrawAssist.amendUI( drawText, true ) ;
		}
	}

	/**
		Called when listener receives a valid DrawDelegate
		and when the parent UIElement is flagged as visible.
	*/
	@Override
	public void addDraws( final DrawDelegate<World, Draw> _delegate, final World _world )
	{
		if( drawText != null )
		{
			_delegate.addTextDraw( drawText, _world ) ;
		}
	}

	/**
		Only called if there is a valid DrawDelegate and 
		when the parent UIElement is flagged as invisible.
	*/
	@Override
	public void removeDraws( final DrawDelegate<World, Draw> _delegate )
	{
		_delegate.removeDraw( drawText ) ;
	}

	@Override
	public void refresh()
	{
		super.refresh() ;
		if( drawText != null && getParent().isVisible() )
		{
			final Vector3 length = getLength() ;
			final Vector3 offset = getOffset() ;

			final MalletFont.Metrics metrics = font.getMetrics() ;
			offset.x = UI.align( drawAlignmentX, font.stringWidth( text ), length.x ) ;
			offset.y = UI.align( drawAlignmentY, metrics.getHeight(), length.y ) ;

			DrawAssist.amendTextStart( drawText, 0 ) ;
			DrawAssist.amendTextEnd( drawText, text.length()/*font.stringIndexWidth( text, length.x )*/ ) ;
			DrawAssist.amendOrder( drawText, getLayer() ) ;
			DrawAssist.forceUpdate( drawText ) ;
		}
	}

	public StringBuilder getText()
	{
		return text ;
	}

	public UI.Alignment getAlignmentX()
	{
		return drawAlignmentX ;
	}

	public UI.Alignment getAlignmentY()
	{
		return drawAlignmentY ;
	}

	public Draw getDraw()
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

	public static class Meta extends GUIBase.Meta
	{
		private UI.Alignment xAlign = UI.Alignment.CENTRE ;
		private UI.Alignment yAlign = UI.Alignment.CENTRE ;

		private String text = "" ;
		private MalletColour colour = MalletColour.white() ;
		private MalletFont font = new MalletFont( "Arial" ) ;

		private final Connect.Signal xAlignChanged = new Connect.Signal() ;
		private final Connect.Signal yAlignChanged = new Connect.Signal() ;

		private final Connect.Signal textChanged   = new Connect.Signal() ;
		private final Connect.Signal colourChanged = new Connect.Signal() ;
		private final Connect.Signal fontChanged   = new Connect.Signal() ;
		
		public Meta() {}

		@Override
		public String getType()
		{
			return "UIELEMENT_GUITEXT" ;
		}

		public void setAlignment( final UI.Alignment _x, final UI.Alignment _y )
		{
			if( _x != null && _x != xAlign )
			{
				xAlign = _x ;
				UIElement.signal( this, xAlignChanged() ) ;
			}

			if( _y != null && _y != yAlign )
			{
				yAlign = _y ;
				UIElement.signal( this, yAlignChanged() ) ;
			}
		}

		public void setText( final String _text )
		{
			if( _text != null && text.equals( _text ) == false )
			{
				text = _text ;
				UIElement.signal( this, textChanged() ) ;
			}
		}

		public void setColour( final MalletColour _colour )
		{
			if( _colour != null && colour.equals( _colour ) == false )
			{
				colour.changeColour( _colour.toInt() ) ;
				UIElement.signal( this, colourChanged() ) ;
			}
		}

		public void setFont( final MalletFont _font )
		{
			if( font != null && font.equals( _font ) == false )
			{
				font = new MalletFont( _font.getFontName(), _font.getStyle(), _font.getPointSize() ) ;
				UIElement.signal( this, fontChanged() ) ;
			}
		}
		
		public UI.Alignment getAlignmentX()
		{
			return xAlign ;
		}

		public UI.Alignment getAlignmentY()
		{
			return yAlign ;
		}

		public String getText()
		{
			return text ;
		}

		public MalletColour getColour( final MalletColour _populate )
		{
			_populate.changeColour( colour.toInt() ) ;
			return _populate ;
		}

		public MalletFont getFont()
		{
			return font ;
		}

		public Connect.Signal xAlignChanged()
		{
			return xAlignChanged ;
		}

		public Connect.Signal yAlignChanged()
		{
			return yAlignChanged ;
		}

		public Connect.Signal textChanged()
		{
			return textChanged ;
		}

		public Connect.Signal colourChanged()
		{
			return colourChanged ;
		}

		public Connect.Signal fontChanged()
		{
			return fontChanged ;
		}
	}
}
