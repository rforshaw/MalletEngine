package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.ui.* ;

public class GUITick extends GUIDraw
{
	private final Connect.Slot<UICheckbox> checkSlot = ( final UICheckbox _box ) ->
	{
		if( _box.isChecked() == true )
		{
			addDraws( getWorld() ) ;
		}
		else
		{
			removeDraws() ;
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
	public void addDraws( final World _world )
	{
		if( getParentCheckbox().isChecked() == true )
		{
			super.addDraws( _world ) ;
		}
	}

	UICheckbox getParentCheckbox()
	{
		return ( UICheckbox )getParent() ;
	}

	public static class Meta extends GUIDraw.Meta
	{
		public Meta()
		{
			super() ;
		}

		@Override
		public String getType()
		{
			return "UICHECKBOX_GUITICK" ;
		}
	}
}
