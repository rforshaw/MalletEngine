package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public class GUIPanelEdge extends GUIDrawEdge
{
	private final MalletColour neutral ;
	private final MalletColour rollover ;
	private final MalletColour clicked ;

	private final Connect.Slot<UIElement> engagedSlot = new Connect.Slot<UIElement>()
	{
		@Override
		public void slot( final UIElement _layout )
		{
			setColour( rollover ) ;
			DrawAssist.forceUpdate( getDraw() ) ;
		}
	} ;

	Connect.Slot<UIElement> disengagedSlot = new Connect.Slot<UIElement>()
	{
		@Override
		public void slot( final UIElement _layout )
		{
			setColour( neutral ) ;
			DrawAssist.forceUpdate( getDraw() ) ;
		}
	} ;

	public GUIPanelEdge( final Meta _meta, final UIElement _parent )
	{
		super( _meta, _parent ) ;
		neutral  = _meta.getNeutralColour( new MalletColour() ) ;
		rollover = _meta.getNeutralColour( new MalletColour() ) ;
		clicked  = _meta.getNeutralColour( new MalletColour() ) ;

		UIElement.connect( _parent, _parent.elementEngaged(), engagedSlot ) ;
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
		setColour( rollover ) ;
		DrawAssist.forceUpdate( getDraw() ) ;
		return InputEvent.Action.PROPAGATE ;
	}

	@Override
	public InputEvent.Action touchReleased( final InputEvent _input )
	{
		final InputEvent.Action action = mouseReleased( _input ) ;
		setColour( neutral ) ;
		return action ;
	}

	@Override
	public InputEvent.Action mousePressed( final InputEvent _input )
	{
		setColour( clicked ) ;
		DrawAssist.forceUpdate( getDraw() ) ;
		return InputEvent.Action.PROPAGATE ;
	}

	@Override
	public InputEvent.Action touchPressed( final InputEvent _input )
	{
		return mousePressed( _input ) ;
	}

	public static class Meta extends GUIDrawEdge.Meta
	{
		private MalletColour rollover = MalletColour.white() ;
		private MalletColour clicked = MalletColour.white() ;

		private final Connect.Signal rolloverChanged = new Connect.Signal() ;
		private final Connect.Signal clickedChanged  = new Connect.Signal() ;

		public Meta() {}

		@Override
		public String getType()
		{
			return "UIELEMENT_GUIPANELEDGE" ;
		}

		public void setNeutralColour( final MalletColour _colour )
		{
			setColour( _colour ) ;
		}

		public void setRolloverColour( final MalletColour _colour )
		{
			if( _colour != null && rollover.equals( _colour ) == false )
			{
				rollover.changeColour( _colour.toInt() ) ;
				UIElement.signal( this, rolloverChanged() ) ;
			}
		}

		public void setClickedColour( final MalletColour _colour )
		{
			if( _colour != null && clicked.equals( _colour ) == false )
			{
				clicked.changeColour( _colour.toInt() ) ;
				UIElement.signal( this, clickedChanged() ) ;
			}
		}

		public MalletColour getNeutralColour( final MalletColour _populate )
		{
			return getColour( _populate ) ;
		}

		public MalletColour getRolloverColour( final MalletColour _populate )
		{
			_populate.changeColour( rollover.toInt() ) ;
			return _populate ;
		}

		public MalletColour getClickedColour( final MalletColour _populate )
		{
			_populate.changeColour( clicked.toInt() ) ;
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
