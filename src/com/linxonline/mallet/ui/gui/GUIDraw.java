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

	private Colour colour ;
	private final Texture sheet ;
	private final UIElement.UV uv ;

	private DrawUpdater updater ;
	private GeometryBuffer geometry = null ;
	private final Draw draw = new Draw() ;

	public GUIDraw( final Meta _meta, final UIElement _parent )
	{
		super( _meta, _parent ) ;
		retainRatio = _meta.isRetainRatio() ;
		drawAlignmentX = _meta.getAlignmentX() ;
		drawAlignmentY = _meta.getAlignmentY() ;
		colour = _meta.getColour( new Colour() ) ;
		sheet = new Texture( _meta.getSheet(), Texture.Filter.LINEAR, Texture.Wrap.CLAMP_EDGE ) ;
		uv = _meta.getUV( new UIElement.UV() ) ;

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

	public void setColour( final Colour _colour )
	{
		colour = ( _colour != null ) ? _colour : Colour.white() ;

		final Draw draw = getDraw() ;
		final Shape shape = ( Shape )draw.getShape() ;
		if( shape != null )
		{
			GUI.updateColour( shape, colour ) ;
		}

		if( updater != null )
		{
			updater.forceUpdate() ;
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
			final Vector3 position = getPosition() ;
			draw.setPositionInstant( position.x, position.y, position.z ) ;

			final Vector3 offset = getOffset() ;
			draw.setOffsetInstant( offset.x, offset.y, offset.z ) ;

			draw.setShape( Shape.constructPlane( getLength(), uv.min, uv.max ) ) ;
			setColour( getColour() ) ;
		}
	}

	@Override
	public void addDraws( final World _world )
	{
		final int layer = getLayer() ;
		final Shape shape = ( Shape )draw.getShape() ;

		updater = GUI.getDrawUpdater( _world, sheet, shape, layer ) ;

		geometry = updater.getBuffer( 0 ) ;
		geometry.addDraws( draw ) ;
	}

	@Override
	public void removeDraws()
	{
		if( updater != null )
		{
			geometry.removeDraws( draw ) ;
			updater.forceUpdate() ;
		}
	}

	@Override
	public void layerUpdated( int _layer )
	{
		if( updater != null )
		{
			geometry.removeDraws( draw ) ;
			updater.forceUpdate() ;
		}

		final Shape shape = ( Shape )draw.getShape() ;
		updater = GUI.getDrawUpdater( getWorld(), sheet, shape, _layer ) ;

		geometry = updater.getBuffer( 0 ) ;
		geometry.addDraws( draw ) ;
	}

	@Override
	public void refresh()
	{
		super.refresh() ;
		final UIElement parent = getParent() ;

		final Vector3 position = getPosition() ;
		final Vector3 offset = getOffset() ;
		final Vector3 length = getLength() ;

		updateLength( parent.getLength(), length ) ;
		updateOffset( parent.getOffset(), offset ) ;

		if( updater != null && parent.isVisible() == true )
		{
			Shape.updatePlaneGeometry( ( Shape )draw.getShape(), length ) ;

			draw.setPositionInstant( position.x, position.y, position.z ) ;
			draw.setOffsetInstant( offset.x, offset.y, offset.z ) ;
			updater.forceUpdate() ;
		}
	}

	@Override
	public void shutdown()
	{
		super.shutdown() ;
		updater = null ;
		geometry = null ;
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

	public DrawUpdater getUpdater()
	{
		return updater ;
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

	public Colour getColour()
	{
		return colour ;
	}

	public Texture getTexture()
	{
		return sheet ;
	}

	public static class Meta extends GUIComponent.Meta
	{
		private final UIVariant retainRatio = new UIVariant( "RETAIN_RATIO", false,                new Connect.Signal() ) ;
		private final UIVariant xAlign      = new UIVariant( "ALIGNMENT_X",  UI.Alignment.LEFT,    new Connect.Signal() ) ;
		private final UIVariant yAlign      = new UIVariant( "ALIGNMENT_Y",  UI.Alignment.LEFT,    new Connect.Signal() ) ;
		private final UIVariant sheet       = new UIVariant( "TEXTURE",      "",                   new Connect.Signal() ) ;
		private final UIVariant colour      = new UIVariant( "COLOUR",       Colour.white(), new Connect.Signal() ) ;
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

		public void setColour( final Colour _colour )
		{
			final Colour col = colour.toObject( Colour.class ) ;
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

		public Colour getColour( final Colour _populate )
		{
			final Colour col = colour.toObject( Colour.class ) ;
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
