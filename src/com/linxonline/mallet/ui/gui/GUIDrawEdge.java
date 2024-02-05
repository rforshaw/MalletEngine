package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public class GUIDrawEdge extends GUIDraw
{
	private final float edge ;

	public GUIDrawEdge( final Meta _meta, final UIElement _parent )
	{
		super( _meta, _parent ) ;
		edge = _meta.getEdge() ;

		final Draw draw = getDraw() ;
		draw.setShape( GUI.constructEdge( getLength(), edge ) ) ;
		setColour( getColour() ) ;
	}

	@Override
	public void refresh()
	{
		//super.refresh() ;
		final UIElement parent = getParent() ;
		final Vector3 position = getPosition() ;
		final Vector3 offset = getOffset() ;
		final Vector3 length = getLength() ;

		updateLength( parent.getLength(), getLength() ) ;
		updateOffset( parent.getOffset(), getOffset() ) ;

		final DrawUpdater updater = getUpdater() ;
		if( updater != null && parent.isVisible() == true )
		{
			final Draw draw = getDraw() ;
			draw.setPositionInstant( position.x, position.y, position.z ) ;
			draw.setOffsetInstant( offset.x, offset.y, offset.z ) ;

			GUI.updateEdge( ( Shape )draw.getShape(), getLength(), edge ) ;
			updater.forceUpdate() ;
		}
	}

	private void updateLength( final Vector3 _length, final Vector3 _toUpdate )
	{
		_toUpdate.setXYZ( _length ) ;
	}

	private void updateOffset( final Vector3 _offset, final Vector3 _toUpdate )
	{
		UI.align( drawAlignmentX, drawAlignmentY, _toUpdate, getLength(), getParent().getLength() ) ;
		_toUpdate.add( _offset ) ;
	}

	public static class Meta extends GUIDraw.Meta
	{
		private final UIVariant edge = new UIVariant( "EDGE", 1.0f, new Connect.Signal() ) ;

		public Meta()
		{
			super() ;

			int row = rowCount( root() ) ;
			createData( null, row + 1, 1 ) ;

			setData( new UIModelIndex( root(), row++, 0 ), edge, UIAbstractModel.Role.User ) ;
		}

		@Override
		public String getType()
		{
			return "UIELEMENT_GUIDRAWEDGE" ;
		}

		public void setEdge( final float _edge )
		{
			if( Math.abs( edge.toFloat() - _edge ) > 0.001f )
			{
				edge.setFloat( _edge ) ;
				UIElement.signal( this, edge.getSignal() ) ;
			}
		}

		public float getEdge()
		{
			return edge.toFloat() ;
		}

		public final Connect.Signal edgeChanged()
		{
			return edge.getSignal() ;
		}
	}
}
