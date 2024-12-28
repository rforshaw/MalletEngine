package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public class GUIPanelEdge extends GUIDrawEdge
{
	private final Colour neutral ;
	private final Colour rollover ;
	private final Colour clicked ;

	private final Connect.Slot<UIElement> engagedSlot = new Connect.Slot<UIElement>()
	{
		@Override
		public void slot( final UIElement _layout )
		{
			setColour( rollover ) ;
		}
	} ;

	Connect.Slot<UIElement> disengagedSlot = new Connect.Slot<UIElement>()
	{
		@Override
		public void slot( final UIElement _layout )
		{
			setColour( neutral ) ;
		}
	} ;

	public GUIPanelEdge( final Meta _meta, final UIElement _parent )
	{
		super( _meta, _parent ) ;
		neutral  = _meta.getNeutralColour( new Colour() ) ;
		rollover = _meta.getRolloverColour( new Colour() ) ;
		clicked  = _meta.getClickedColour( new Colour() ) ;
		setColour( neutral ) ;

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
		return InputEvent.Action.PROPAGATE ;
	}

	@Override
	public InputEvent.Action touchPressed( final InputEvent _input )
	{
		return mousePressed( _input ) ;
	}

	public static class Meta extends GUIDrawEdge.Meta
	{
		private final UIVariant rollover = new UIVariant( "ROLLOVER", Colour.white(), new Connect.Signal() ) ;
		private final UIVariant clicked  = new UIVariant( "CLICKED",  Colour.white(), new Connect.Signal() ) ;

		public Meta()
		{
			super() ;

			int row = rowCount( root() ) ;
			createData( null, row + 2, 1 ) ;

			setData( new UIModelIndex( root(), row++, 0 ), rollover, UIAbstractModel.Role.User ) ;
			setData( new UIModelIndex( root(), row++, 0 ), clicked,  UIAbstractModel.Role.User ) ;
		}

		@Override
		public String getType()
		{
			return "UIELEMENT_GUIPANELEDGE" ;
		}

		public void setNeutralColour( final Colour _colour )
		{
			setColour( _colour ) ;
		}

		public void setRolloverColour( final Colour _colour )
		{
			final Colour col = rollover.toObject( Colour.class ) ;
			if( _colour != null && col.equals( _colour ) == false )
			{
				col.changeColour( _colour.toInt() ) ;
				UIElement.signal( this, rollover.getSignal() ) ;
			}
		}

		public void setClickedColour( final Colour _colour )
		{
			final Colour col = clicked.toObject( Colour.class ) ;
			if( _colour != null && col.equals( _colour ) == false )
			{
				col.changeColour( _colour.toInt() ) ;
				UIElement.signal( this, clicked.getSignal() ) ;
			}
		}

		public Colour getNeutralColour( final Colour _populate )
		{
			return getColour( _populate ) ;
		}

		public Colour getRolloverColour( final Colour _populate )
		{
			final Colour col = rollover.toObject( Colour.class ) ;
			_populate.changeColour( col.toInt() ) ;
			return _populate ;
		}

		public Colour getClickedColour( final Colour _populate )
		{
			final Colour col = clicked.toObject( Colour.class ) ;
			_populate.changeColour( col.toInt() ) ;
			return _populate ;
		}

		public final Connect.Signal neutralChanged()
		{
			return uvChanged() ;
		}

		public final Connect.Signal rolloverChanged()
		{
			return rollover.getSignal() ;
		}
		
		public final Connect.Signal clickedChanged()
		{
			return clicked.getSignal() ;
		}
	}
}
