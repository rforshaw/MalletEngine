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
		constructDraws() ;
	}

	/**
		Can be used to construct Draw objects before a 
		DrawDelegate is provided by the Rendering System.
	*/
	private void constructDraws()
	{
		final UIElement parent = getParent() ;
		final MalletTexture sheet = getTexture() ;

		if( sheet != null )
		{
			final Draw draw = DrawAssist.createDraw( getPosition(),
													 getOffset(),
													 new Vector3(),
													 new Vector3( 1, 1, 1 ),
													 parent.getLayer() ) ;
			DrawAssist.amendUI( draw, true ) ;
			draw.setShape( GUI.constructEdge( getLength(), edge ) ) ;
			setColour( getColour() ) ;

			final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
			program.mapUniform( "inTex0", sheet ) ;

			draw.setProgram( program ) ;
			setDraw( draw ) ;
		}
	}

	@Override
	public void refresh()
	{
		super.refresh() ;
		final UIElement parent = getParent() ;
		final Vector3 position = getPosition() ;
		final Vector3 offset = getOffset() ;
		final Vector3 length = getLength() ;

		updateLength( parent.getLength(), getLength() ) ;
		updateOffset( parent.getOffset(), getOffset() ) ;

		final Draw draw = getDraw() ;
		if( draw != null && parent.isVisible() == true )
		{
			draw.setPosition( position.x, position.y, position.z ) ;
			draw.setOffset( offset.x, offset.y, offset.z ) ;

			draw.setOrder( getLayer() ) ;
			GUI.updateEdge( draw.getShape(), getLength(), edge ) ;
			DrawAssist.forceUpdate( draw ) ;
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

		public Connect.Signal edgeChanged()
		{
			return edge.getSignal() ;
		}
	}
}
