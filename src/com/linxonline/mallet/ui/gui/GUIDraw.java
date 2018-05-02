package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public class GUIDraw extends GUIBase
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
		super( _parent ) ;
		retainRatio    = _meta.isRetainRatio() ;
		drawAlignmentX = _meta.getAlignmentX() ;
		drawAlignmentY = _meta.getAlignmentY() ;
		colour         = _meta.getColour( new MalletColour() ) ;
		sheet          = new MalletTexture( _meta.getSheet() ) ;
		uv             = _meta.getUV( new UIElement.UV() ) ;

		updateLength( _parent.getLength(), getLength() ) ;
		updateOffset( _parent.getOffset(), getOffset() ) ;
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
	@Override
	public void constructDraws()
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
			ProgramAssist.map( program, "inTex0", sheet ) ;

			DrawAssist.attachProgram( draw, program ) ;
		}

		super.constructDraws() ;
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

	public static class Meta extends GUIBase.Meta
	{
		private boolean retainRatio = false ;
		private UI.Alignment xAlign = UI.Alignment.LEFT ;
		private UI.Alignment yAlign = UI.Alignment.LEFT ;

		private String sheet = "" ;
		private MalletColour colour = MalletColour.white() ;
		private UIElement.UV uv = new UIElement.UV( 0.0f, 0.0f, 1.0f, 1.0f ) ;

		private final Connect.Signal retainRatioChanged = new Connect.Signal() ;
		private final Connect.Signal xAlignChanged      = new Connect.Signal() ;
		private final Connect.Signal yAlignChanged      = new Connect.Signal() ;

		private final Connect.Signal sheetChanged       = new Connect.Signal() ;
		private final Connect.Signal colourChanged      = new Connect.Signal() ;
		private final Connect.Signal uvChanged          = new Connect.Signal() ;
		
		public Meta() {}

		@Override
		public String getType()
		{
			return "UIELEMENT_GUIDRAW" ;
		}

		public void setRetainRatio( final boolean _retain )
		{
			if( retainRatio != _retain )
			{
				retainRatio = _retain ;
				UIElement.signal( this, retainRatioChanged() ) ;
			}
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

		public void setSheet( final String _path )
		{
			if( _path != null && sheet.equals( _path ) == false )
			{
				sheet = _path ;
				UIElement.signal( this, sheetChanged() ) ;
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

		public void setUV( UIElement.UV _uv )
		{
			final Vector2 min = _uv.min ;
			final Vector2 max = _uv.max ;
			setUV( min.x, min.y, max.x, max.y ) ;
		}

		public void setUV( final float _minX, final float _minY,
						   final float _maxX, final float _maxY )
		{
			if( UI.applyVec2( uv.min, _minX, _minY ) || 
				UI.applyVec2( uv.max, _maxX, _maxY ) )
			{
				UIElement.signal( this, uvChanged() ) ;
			}
		}

		public boolean isRetainRatio()
		{
			return retainRatio ;
		}

		public UI.Alignment getAlignmentX()
		{
			return xAlign ;
		}

		public UI.Alignment getAlignmentY()
		{
			return yAlign ;
		}

		public String getSheet()
		{
			return sheet ;
		}

		public MalletColour getColour( final MalletColour _populate )
		{
			_populate.changeColour( colour.toInt() ) ;
			return _populate ;
		}

		public UIElement.UV getUV( final UIElement.UV _populate )
		{
			_populate.min.setXY( uv.min ) ;
			_populate.max.setXY( uv.max ) ;
			return _populate ;
		}

		public Connect.Signal retainRatioChanged()
		{
			return retainRatioChanged ;
		}

		public Connect.Signal xAlignChanged()
		{
			return xAlignChanged ;
		}

		public Connect.Signal yAlignChanged()
		{
			return yAlignChanged ;
		}

		public Connect.Signal sheetChanged()
		{
			return sheetChanged ;
		}

		public Connect.Signal colourChanged()
		{
			return colourChanged ;
		}

		public Connect.Signal uvChanged()
		{
			return uvChanged ;
		}
	}
}
