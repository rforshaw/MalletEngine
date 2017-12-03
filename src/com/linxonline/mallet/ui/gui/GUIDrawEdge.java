package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public class GUIDrawEdge<T extends UIElement> extends GUIDraw<T>
{
	private final float edge ;

	public GUIDrawEdge( final Meta _meta )
	{
		super( _meta ) ;
		edge = _meta.getEdge() ;
	}

	public GUIDrawEdge( final MalletTexture _sheet, final float _edge )
	{
		super( _sheet, null ) ;
		edge = _edge ;
	}

	/**
		Can be used to construct Draw objects before a 
		DrawDelegate is provided by the Rendering System.
	*/
	public void constructDraws()
	{
		super.constructDraws() ;

		final T parent = getParent() ;
		final MalletTexture sheet = getTexture() ;

		if( sheet != null )
		{
			final Draw draw = DrawAssist.createDraw( parent.getPosition(),
														getOffset(),
														new Vector3(),
														new Vector3( 1, 1, 1 ),
														parent.getLayer() ) ;
			DrawAssist.amendUI( draw, true ) ;
			DrawAssist.amendShape( draw, GUI.constructEdge( getLength(), edge ) ) ;
			setColour( getColour() ) ;

			final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
			ProgramAssist.map( program, "inTex0", sheet ) ;

			DrawAssist.attachProgram( draw, program ) ;
			setDraw( draw ) ;
		}
	}

	@Override
	public void refresh()
	{
		super.refresh() ;
		final T parent = getParent() ;
		updateLength( parent.getLength(), getLength() ) ;
		updateOffset( parent.getOffset(), getOffset() ) ;

		final Draw draw = getDraw() ;
		if( draw != null && parent.isVisible() == true )
		{
			DrawAssist.amendOrder( draw, getLayer() ) ;
			GUI.updateEdge( DrawAssist.getDrawShape( draw ), getLength(), edge ) ;
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
		private float edge = 1.0f ;

		private final Connect.Signal edgeChanged = new Connect.Signal() ;

		public Meta() {}

		public void setEdge( final float _edge )
		{
			if( Math.abs( edge - _edge ) > 0.001f )
			{
				edge = _edge ;
				UIElement.signal( this, edgeChanged() ) ;
			}
		}

		public float getEdge()
		{
			return edge ;
		}

		public Connect.Signal edgeChanged()
		{
			return edgeChanged ;
		}
	}
}
