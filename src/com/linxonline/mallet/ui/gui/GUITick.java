package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public class GUITick extends GUIDraw
{
	private final Connect.Slot<UICheckbox> checkSlot = new Connect.Slot<UICheckbox>()
	{
		@Override
		public void slot( final UICheckbox _box )
		{
			final DrawDelegate delegate = getDrawDelegate() ;
			if( delegate != null )
			{
				if( _box.isChecked() == true )
				{
					DrawAssist.forceUpdate( getDraw() ) ;
					delegate.addBasicDraw( getDraw(), getWorld() ) ;
				}
				else
				{
					delegate.removeDraw( getDraw() ) ;
				}
			}
		}
	} ;

	public GUITick( final Meta _meta, final UICheckbox _parent )
	{
		super( _meta, _parent ) ;
		setLayerOffset( 1 ) ;

		UIElement.connect( _parent, _parent.checkChanged(), checkSlot ) ;
	}

	@Override
	public void shutdown()
	{
		super.shutdown() ;
		final UICheckbox parent = getParentCheckbox() ;
		UIElement.disconnect( parent, parent.checkChanged(), checkSlot ) ;
	}

	/**
		Called when listener receives a valid DrawDelegate
		and when the parent UIElement is flagged as visible.
	*/
	@Override
	public void addDraws( final DrawDelegate _delegate, final World _world )
	{
		if( getParentCheckbox().isChecked() == true )
		{
			super.addDraws( _delegate, _world ) ;
		}
	}

	UICheckbox getParentCheckbox()
	{
		return ( UICheckbox )getParent() ;
	}

	public static class Meta extends GUIDraw.Meta
	{
		public Meta() {}

		@Override
		public String getType()
		{
			return "UICHECKBOX_GUITICK" ;
		}
	}
}
