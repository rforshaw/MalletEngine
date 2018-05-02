package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public class GUIPanelDraw extends GUIDraw
{
	private final UIElement.UV neutral ;
	private final UIElement.UV rollover ;
	private final UIElement.UV clicked ;

	private final Connect.Slot<UIElement> engagedSlot = new Connect.Slot<UIElement>()
	{
		@Override
		public void slot( final UIElement _layout )
		{
			Shape.updatePlaneUV( DrawAssist.getDrawShape( getDraw() ), rollover.min, rollover.max ) ;
			DrawAssist.forceUpdate( getDraw() ) ;
		}
	} ;

	private final Connect.Slot<UIElement> disengagedSlot = new Connect.Slot<UIElement>()
	{
		@Override
		public void slot( final UIElement _layout )
		{
			Shape.updatePlaneUV( DrawAssist.getDrawShape( getDraw() ), neutral.min, neutral.max ) ;
			DrawAssist.forceUpdate( getDraw() ) ;
		}
	} ;

	public GUIPanelDraw( final Meta _meta, final UIElement _parent )
	{
		super( _meta, _parent ) ;
		neutral  = _meta.getNeutralUV( new UIElement.UV() ) ;
		rollover = _meta.getRolloverUV( new UIElement.UV() ) ;
		clicked  = _meta.getClickedUV( new UIElement.UV() ) ;

		UIElement.connect( _parent, _parent.elementEngaged(),    engagedSlot ) ;
		UIElement.connect( _parent, _parent.elementDisengaged(), disengagedSlot ) ;
	}

	@Override
	public void shutdown()
	{
		super.shutdown() ;
		final UIElement parent = getParent() ;
		UIElement.disconnect( parent, parent.elementEngaged(),    engagedSlot ) ;
		UIElement.disconnect( parent, parent.elementDisengaged(), disengagedSlot ) ;
	}

	@Override
	public InputEvent.Action mouseReleased( final InputEvent _input )
	{
		Shape.updatePlaneUV( DrawAssist.getDrawShape( getDraw() ), rollover.min, rollover.max ) ;
		DrawAssist.forceUpdate( getDraw() ) ;
		return InputEvent.Action.PROPAGATE ;
	}

	@Override
	public InputEvent.Action touchReleased( final InputEvent _input )
	{
		return mouseReleased( _input ) ;
	}

	@Override
	public InputEvent.Action mousePressed( final InputEvent _input )
	{
		Shape.updatePlaneUV( DrawAssist.getDrawShape( getDraw() ), clicked.min, clicked.max ) ;
		DrawAssist.forceUpdate( getDraw() ) ;
		return InputEvent.Action.PROPAGATE ;
	}

	@Override
	public InputEvent.Action touchPressed( final InputEvent _input )
	{
		return mousePressed( _input ) ;
	}

	public static class Meta extends GUIDraw.Meta
	{
		private UIElement.UV rollover = new UIElement.UV( 0.0f, 0.0f, 1.0f, 1.0f ) ;
		private UIElement.UV clicked = new UIElement.UV( 0.0f, 0.0f, 1.0f, 1.0f ) ;

		private final Connect.Signal rolloverChanged = new Connect.Signal() ;
		private final Connect.Signal clickedChanged  = new Connect.Signal() ;

		public Meta() {}

		@Override
		public String getType()
		{
			return "UIELEMENT_GUIPANELDRAW" ;
		}

		public void setNeutralUV( UIElement.UV _uv )
		{
			setUV( _uv ) ;
		}

		public void setNeutralUV( final float _minX, final float _minY,
								  final float _maxX, final float _maxY )
		{
			setUV( _minX, _minY, _maxX, _maxY ) ;
		}

		public void setRolloverUV( UIElement.UV _uv )
		{
			final Vector2 min = _uv.min ;
			final Vector2 max = _uv.max ;
			setRolloverUV( min.x, min.y, max.x, max.y ) ;
		}

		public void setRolloverUV( final float _minX, final float _minY,
								   final float _maxX, final float _maxY )
		{
			final boolean b1 = UI.applyVec2( rollover.min, _minX, _minY ) ;
			final boolean b2 = UI.applyVec2( rollover.max, _maxX, _maxY ) ;
			if( b1 == true || b2 == true )
			{
				UIElement.signal( this, rolloverChanged() ) ;
			}
		}

		public void setClickedUV( UIElement.UV _uv )
		{
			final Vector2 min = _uv.min ;
			final Vector2 max = _uv.max ;
			setClickedUV( min.x, min.y, max.x, max.y ) ;
		}

		public void setClickedUV( final float _minX, final float _minY,
								  final float _maxX, final float _maxY )
		{
			final boolean b1 = UI.applyVec2( clicked.min, _minX, _minY ) ;
			final boolean b2 = UI.applyVec2( clicked.max, _maxX, _maxY ) ;
			if( b1 == true || b2 == true )
			{
				UIElement.signal( this, clickedChanged() ) ;
			}
		}

		public UIElement.UV getNeutralUV( final UIElement.UV _populate )
		{
			return getUV( _populate ) ;
		}

		public UIElement.UV getRolloverUV( final UIElement.UV _populate )
		{
			_populate.min.setXY( rollover.min ) ;
			_populate.max.setXY( rollover.max ) ;
			return _populate ;
		}

		public UIElement.UV getClickedUV( final UIElement.UV _populate )
		{
			_populate.min.setXY( clicked.min ) ;
			_populate.max.setXY( clicked.max ) ;
			return _populate ;
		}

		public Connect.Signal neutralChanged()
		{
			return uvChanged() ;
		}

		public Connect.Signal rolloverChanged()
		{
			return rolloverChanged ;
		}
		
		public Connect.Signal clickedChanged()
		{
			return clickedChanged ;
		}
	}
}
