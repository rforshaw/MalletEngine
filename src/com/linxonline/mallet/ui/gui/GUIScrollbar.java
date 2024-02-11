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

	private final Draw xBar = new Draw() ;
	private final Draw yBar = new Draw() ;

	private DrawUpdater updater ;
	private GeometryBuffer geometry ;

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

		parent.getInternalCamera().getHUDPosition( offset ) ;
		updateLengths( parent.getScrollbarLength(), parent.getScrollWidth() ) ;

		{
			final Vector3 position = getPosition() ;
			xBar.setPositionInstant( position.x, position.y, position.z ) ;

			final Vector3 offset = getOffset() ;
			xBar.setOffsetInstant( offset.x, offset.y, offset.z ) ;

			xBar.setShape( Shape.constructPlane( xLength, uv.min, uv.max ) ) ;
		}

		{
			final Vector3 position = getPosition() ;
			yBar.setPositionInstant( position.x, position.y, position.z ) ;

			final Vector3 offset = getOffset() ;
			yBar.setOffsetInstant( offset.x, offset.y, offset.z ) ;

			yBar.setShape( Shape.constructPlane( yLength, uv.min, uv.max ) ) ;
		}
	}

	@Override
	public void addDraws( final World _world )
	{
		if( updater != null )
		{
			// Remove the draw object from the previous 
			// updater the draw may have changed significantly.
			geometry.removeDraws( xBar, yBar ) ;
		}

		final Shape shape = ( Shape )xBar.getShape() ;
		final int layer = getLayer() ;

		updater = GUI.getDrawUpdater( _world, sheet, shape, layer ) ;
		geometry = updater.getBuffer( 0 ) ;
		geometry.addDraws( xBar, yBar ) ;
	}

	@Override
	public void removeDraws()
	{
		geometry.removeDraws( xBar, yBar ) ;
	}

	@Override
	public void layerUpdated( int _layer )
	{
		if( updater != null )
		{
			geometry.removeDraws( xBar, yBar ) ;
		}

		final Shape shape = ( Shape )xBar.getShape() ;

		updater = GUI.getDrawUpdater( getWorld(), sheet, shape, _layer ) ;
		geometry = updater.getBuffer( 0 ) ;
		geometry.addDraws( xBar, yBar ) ;
	}

	@Override
	public void refresh()
	{
		super.refresh() ;
		final UIList parent = getParentList() ;

		updatePosition( offset ) ;
		updateLengths( parent.getScrollbarLength(), parent.getScrollWidth() ) ;

		final Vector3 position = parent.getPosition() ;

		xBar.setPosition( position.x, position.y, position.z ) ;
		xBar.setOffset( offset.x, offset.y, offset.z ) ;
		Shape.updatePlaneGeometry( ( Shape )xBar.getShape(), xLength ) ;

		yBar.setPosition( position.x, position.y, position.z ) ;
		yBar.setOffset( offset.x, offset.y, offset.z ) ;
		Shape.updatePlaneGeometry( ( Shape )yBar.getShape(), yLength ) ;

		updater.forceUpdate() ;
	}

	UIList getParentList()
	{
		return ( UIList )getParent() ;
	}

	public Draw getXBar()
	{
		return xBar ;
	}

	public Draw getYBar()
	{
		return yBar ;
	}

	private void updatePosition( final Vector3 _position )
	{
		final UIList parent = getParentList() ;
		final Vector3 length = parent.getLength() ;
		final Vector3 absLength = parent.getAbsoluteLength() ;

		parent.getInternalCamera().getHUDPosition( _position ) ;
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
		public Meta()
		{
			super() ;
		}

		@Override
		public String getType()
		{
			return "UILIST_GUISCROLLBAR" ;
		}
	}
}
