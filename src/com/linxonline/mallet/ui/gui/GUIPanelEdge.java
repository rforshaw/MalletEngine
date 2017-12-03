package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public class GUIPanelEdge<T extends UIElement> extends GUIDrawEdge<T>
{
	private final MalletColour neutral ;
	private final MalletColour rollover ;
	private final MalletColour clicked ;

	private final Connect.Slot<T> engagedSlot = new Connect.Slot<T>()
	{
		@Override
		public void slot( final T _layout )
		{
			setColour( rollover ) ;
			DrawAssist.forceUpdate( getDraw() ) ;
		}
	} ;

	Connect.Slot<T> disengagedSlot = new Connect.Slot<T>()
	{
		@Override
		public void slot( final T _layout )
		{
			setColour( neutral ) ;
			DrawAssist.forceUpdate( getDraw() ) ;
		}
	} ;

	public GUIPanelEdge( final Meta _meta )
	{
		super( _meta ) ;
		neutral  = _meta.getNeutralColour( new MalletColour() ) ;
		rollover = _meta.getNeutralColour( new MalletColour() ) ;
		clicked  = _meta.getNeutralColour( new MalletColour() ) ;
	}

	public GUIPanelEdge( final MalletTexture _sheet,
						 final float _edge,
						 final MalletColour _neutral,
						 final MalletColour _rollover,
						 final MalletColour _clicked )
	{
		super( _sheet, _edge ) ;
		neutral  = ( _neutral != null )  ? _neutral  : MalletColour.white() ;
		rollover = ( _rollover != null ) ? _rollover : MalletColour.white() ;
		clicked  = ( _clicked != null )  ? _clicked  : MalletColour.white() ;

		setColour( neutral ) ;
	}

	@Override
	public void setParent( T _parent )
	{
		UIElement.connect( _parent, _parent.elementEngaged(), engagedSlot ) ;
		UIElement.connect( _parent, _parent.elementDisengaged(), disengagedSlot ) ;

		super.setParent( _parent ) ;
	}

	@Override
	public void shutdown()
	{
		super.shutdown() ;
		final T parent = getParent() ;
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
