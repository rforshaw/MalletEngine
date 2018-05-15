package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public class GUIScrollbar extends GUIComponent
{
	private final Vector3 xLength = new Vector3() ;
	private final Vector3 yLength = new Vector3() ;
	private final Vector3 offset = new Vector3() ;			// Offset within the UIElement

	private final MalletTexture sheet ;
	private final UIElement.UV uv ;

	protected Draw xBar = null ;
	protected Draw yBar = null ;

	public GUIScrollbar( final MalletTexture _sheet, final UIElement.UV _uv, final UIList _parent )
	{
		super( UIFactory.createMeta( "GUISCROLLBAR" ), _parent ) ;
		sheet = _sheet ;
		uv = _uv ;
	}

	/**
		Can be used to construct Draw objects before a 
		DrawDelegate is provided by the Rendering System.
	*/
	public void constructDraws()
	{
		final UIList parent = getParentList() ;
		final int layer = parent.getLayer() ;

		CameraAssist.getUIPosition( parent.getInternalCamera(), offset ) ;
		updateLengths( parent.getScrollbarLength(),
						parent.getScrollWidth() ) ;

		{
			xBar = DrawAssist.createDraw( parent.getPosition(),
											offset,
											new Vector3(),
											new Vector3( 1, 1, 1 ),
											layer ) ;
			DrawAssist.amendUI( xBar, true ) ;
			DrawAssist.amendShape( xBar, Shape.constructPlane( xLength, uv.min, uv.max ) ) ;

			final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
			ProgramAssist.map( program, "inTex0", sheet ) ;

			DrawAssist.attachProgram( xBar, program ) ;
		}

		{
			yBar = DrawAssist.createDraw( parent.getPosition(),
										offset,
										new Vector3(),
										new Vector3( 1, 1, 1 ),
										layer ) ;
			DrawAssist.amendUI( yBar, true ) ;
			DrawAssist.amendShape( yBar, Shape.constructPlane( yLength, uv.min, uv.max ) ) ;

			final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
			ProgramAssist.map( program, "inTex0", sheet ) ;

			DrawAssist.attachProgram( yBar, program ) ;
		}
	}

	/**
		Called when listener receives a valid DrawDelegate
		and when the parent UIElement is flagged as visible.
	*/
	@Override
	public void addDraws( final DrawDelegate _delegate, final World _world )
	{
		if( xBar != null )
		{
			_delegate.addBasicDraw( xBar, _world ) ;
		}

		if( yBar != null )
		{
			_delegate.addBasicDraw( yBar, _world ) ;
		}
	}

	/**
		Only called if there is a valid DrawDelegate and 
		when the parent UIElement is flagged as invisible.
	*/
	@Override
	public void removeDraws( final DrawDelegate _delegate )
	{
		_delegate.removeDraw( xBar ) ;
		_delegate.removeDraw( yBar ) ;
	}

	@Override
	public void refresh()
	{
		super.refresh() ;
		final UIList parent = getParentList() ;

		updatePosition( offset ) ;
		updateLengths( parent.getScrollbarLength(),
						parent.getScrollWidth() ) ;

		if( xBar != null )
		{
			DrawAssist.amendOrder( xBar, parent.getLayer() ) ;
			Shape.updatePlaneGeometry( DrawAssist.getDrawShape( xBar ), xLength ) ;
			DrawAssist.forceUpdate( xBar ) ;
		}

		if( yBar != null )
		{
			DrawAssist.amendOrder( yBar, parent.getLayer() ) ;
			Shape.updatePlaneGeometry( DrawAssist.getDrawShape( yBar ), yLength ) ;
			DrawAssist.forceUpdate( yBar ) ;
		}
	}

	UIList getParentList()
	{
		return ( UIList )getParent() ;
	}

	private void updatePosition( final Vector3 _position )
	{
		final UIList parent = getParentList() ;
		final Vector3 length = parent.getLength() ;
		final Vector3 absLength = parent.getAbsoluteLength() ;

		CameraAssist.getUIPosition( parent.getInternalCamera(), _position ) ;
		_position.setXYZ( ( absLength.x > 0.0f ) ? ( _position.x * length.x ) / absLength.x : 0.0f,
							( absLength.y > 0.0f ) ? ( _position.y * length.y ) / absLength.y : 0.0f,
							( absLength.z > 0.0f ) ? ( _position.z * length.z ) / absLength.z : 0.0f ) ;

	}

	private void updateLengths( final Vector3 _length, final Vector3 _width )
	{
		xLength.setXYZ( _width.x, _length.y, _width.z ) ;
		yLength.setXYZ( _length.x, _width.y, _width.z ) ;
	}

	public static class Meta extends GUIComponent.Meta
	{
		public Meta() {}

		@Override
		public String getType()
		{
			return "UILIST_GUISCROLLBAR" ;
		}
	}
}
