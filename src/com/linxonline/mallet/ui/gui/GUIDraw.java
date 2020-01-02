package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public class GUIDraw extends GUIComponent
{
	private final Vector3 aspectRatio = new Vector3() ;		// Visual elements aspect ratio
	protected boolean retainRatio = false ;

	protected UI.Alignment drawAlignmentX = UI.Alignment.LEFT ;
	protected UI.Alignment drawAlignmentY = UI.Alignment.LEFT ;

	private MalletColour colour ;
	private final MalletTexture sheet ;
	private final UIElement.UV uv ;

	protected Draw draw = null ;

	public GUIDraw( final Meta _meta, final UIElement _parent )
	{
		super( _meta, _parent ) ;
		retainRatio    = _meta.isRetainRatio() ;
		drawAlignmentX = _meta.getAlignmentX() ;
		drawAlignmentY = _meta.getAlignmentY() ;
		colour         = _meta.getColour( new MalletColour() ) ;
		sheet          = new MalletTexture( _meta.getSheet() ) ;
		uv             = _meta.getUV( new UIElement.UV() ) ;

		updateLength( _parent.getLength(), getLength() ) ;
		updateOffset( _parent.getOffset(), getOffset() ) ;
		constructDraws() ;
	}

	public void setRetainRatio( final boolean _ratio )
	{
		retainRatio = _ratio ;
	}

	public void setAlignment( final UI.Alignment _x, final UI.Alignment _y )
	{
		drawAlignmentX = ( _x == null ) ? UI.Alignment.LEFT : _x ;
		drawAlignmentY = ( _y == null ) ? UI.Alignment.LEFT : _y ;
	}

	public void setColour( final MalletColour _colour )
	{
		colour = ( _colour != null ) ? _colour : MalletColour.white() ;
		final Draw draw = getDraw() ;
		if( draw != null )
		{
			final Shape shape = DrawAssist.getDrawShape( getDraw() ) ;
			if( shape != null )
			{
				GUI.updateColour( shape, colour ) ;
			}
		}
	}

	/**
		Can be used to construct Draw objects before a 
		DrawDelegate is provided by the Rendering System.
	*/
	private void constructDraws()
	{
		if( sheet != null && uv != null )
		{
			draw = DrawAssist.createDraw( getPosition(),
											getOffset(),
											new Vector3(),
											new Vector3( 1, 1, 1 ),
											getLayer() ) ;
			DrawAssist.amendUI( draw, true ) ;
			DrawAssist.amendShape( draw, Shape.constructPlane( getLength(), uv.min, uv.max ) ) ;
			setColour( getColour() ) ;

			final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
			ProgramAssist.mapUniform( program, "inTex0", sheet ) ;

			DrawAssist.attachProgram( draw, program ) ;
		}
	}

	/**
		Called when listener receives a valid DrawDelegate
		and when the parent UIElement is flagged as visible.
	*/
	@Override
	public void addDraws( final DrawDelegate _delegate, final World _world )
	{
		if( draw != null )
		{
			_delegate.addBasicDraw( draw, _world ) ;
		}
	}

	/**
		Only called if there is a valid DrawDelegate and 
		when the parent UIElement is flagged as invisible.
	*/
	@Override
	public void removeDraws( final DrawDelegate _delegate )
	{
		_delegate.removeDraw( draw ) ;
	}

	@Override
	public void refresh()
	{
		super.refresh() ;
		final UIElement parent = getParent() ;

		if( draw != null && parent.isVisible() == true )
		{
			updateLength( parent.getLength(), getLength() ) ;
			updateOffset( parent.getOffset(), getOffset() ) ;

			Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), getLength() ) ;
			DrawAssist.amendOrder( draw, getLayer() ) ;
			DrawAssist.forceUpdate( draw ) ;
		}
	}

	private void updateLength( final Vector3 _length, final Vector3 _toUpdate )
	{
		if( uv == null || retainRatio == false )
		{
			_toUpdate.setXYZ( _length ) ;
			return ;
		}

		UI.calcSubDimension( aspectRatio, sheet, uv ) ;
		UI.fill( UI.Modifier.RETAIN_ASPECT_RATIO, _toUpdate, aspectRatio, _length ) ;
	}

	private void updateOffset( final Vector3 _offset, final Vector3 _toUpdate )
	{
		UI.align( drawAlignmentX, drawAlignmentY, _toUpdate, getLength(), getParent().getLength() ) ;
		_toUpdate.add( _offset ) ;
	}

	public void setDraw( final Draw _draw )
	{
		draw = _draw ;
	}

	public Draw getDraw()
	{
		return draw ;
	}

	public UI.Alignment getAlignmentX()
	{
		return drawAlignmentX ;
	}

	public UI.Alignment getAlignmentY()
	{
		return drawAlignmentY ;
	}

	public MalletColour getColour()
	{
		return colour ;
	}

	public MalletTexture getTexture()
	{
		return sheet ;
	}

	public static class Meta extends GUIComponent.Meta
	{
		private final UIVariant retainRatio = new UIVariant( "RETAIN_RATIO", false,                new Connect.Signal() ) ;
		private final UIVariant xAlign      = new UIVariant( "ALIGNMENT_X",  UI.Alignment.LEFT,    new Connect.Signal() ) ;
		private final UIVariant yAlign      = new UIVariant( "ALIGNMENT_Y",  UI.Alignment.LEFT,    new Connect.Signal() ) ;
		private final UIVariant sheet       = new UIVariant( "TEXTURE",      "",                   new Connect.Signal() ) ;
		private final UIVariant colour      = new UIVariant( "COLOUR",       MalletColour.white(), new Connect.Signal() ) ;
		private final UIVariant uv          = new UIVariant( "UV",           new UIElement.UV( 0.0f, 0.0f, 1.0f, 1.0f ), new Connect.Signal() ) ;

		public Meta()
		{
			super() ;

			int row = rowCount( root() ) ;
			createData( null, row + 6, 1 ) ;

			setData( new UIModelIndex( root(), row++, 0 ), retainRatio, UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), xAlign,      UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), yAlign,      UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), sheet,       UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), colour,      UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), uv,          UIAbstractModel.Role.User ) ;
		}

		@Override
		public String getType()
		{
			return "UIELEMENT_GUIDRAW" ;
		}

		public void setRetainRatio( final boolean _retain )
		{
			if( retainRatio.toBool() != _retain )
			{
				retainRatio.setBool( _retain ) ;
				UIElement.signal( this, retainRatio.getSignal() ) ;
			}
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

		public void setSheet( final String _path )
		{
			if( _path != null && sheet.toString().equals( _path ) == false )
			{
				sheet.setString( _path ) ;
				UIElement.signal( this, sheet.getSignal() ) ;
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

		public void setUV( UIElement.UV _uv )
		{
			final Vector2 min = _uv.min ;
			final Vector2 max = _uv.max ;
			setUV( min.x, min.y, max.x, max.y ) ;
		}

		public void setUV( final float _minX, final float _minY,
						   final float _maxX, final float _maxY )
		{
			final UIElement.UV temp = uv.toObject( UIElement.UV.class ) ;
			if( UI.applyVec2( temp.min, _minX, _minY ) || 
				UI.applyVec2( temp.max, _maxX, _maxY ) )
			{
				UIElement.signal( this, uv.getSignal() ) ;
			}
		}

		public boolean isRetainRatio()
		{
			return retainRatio.toBool() ;
		}

		public UI.Alignment getAlignmentX()
		{
			return xAlign.toObject( UI.Alignment.class ) ;
		}

		public UI.Alignment getAlignmentY()
		{
			return yAlign.toObject( UI.Alignment.class ) ;
		}

		public String getSheet()
		{
			return sheet.toString() ;
		}

		public MalletColour getColour( final MalletColour _populate )
		{
			final MalletColour col = colour.toObject( MalletColour.class ) ;
			_populate.changeColour( col.toInt() ) ;
			return _populate ;
		}

		public UIElement.UV getUV( final UIElement.UV _populate )
		{
			final UIElement.UV temp = uv.toObject( UIElement.UV.class ) ;
			_populate.min.setXY( temp.min ) ;
			_populate.max.setXY( temp.max ) ;
			return _populate ;
		}

		public Connect.Signal retainRatioChanged()
		{
			return retainRatio.getSignal() ;
		}

		public Connect.Signal xAlignChanged()
		{
			return xAlign.getSignal() ;
		}

		public Connect.Signal yAlignChanged()
		{
			return yAlign.getSignal() ;
		}

		public Connect.Signal sheetChanged()
		{
			return sheet.getSignal() ;
		}

		public Connect.Signal colourChanged()
		{
			return colour.getSignal() ;
		}

		public Connect.Signal uvChanged()
		{
			return uv.getSignal() ;
		}
	}
}
