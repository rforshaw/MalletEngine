package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public class GUITick<T extends UICheckbox> extends GUIDraw<T>
{
	private final Connect.Slot<T> checkSlot = new Connect.Slot<T>()
	{
		@Override
		public void slot( final T _box )
		{
			final DrawDelegate<World, Draw> delegate = getDrawDelegate() ;
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

	public GUITick( final Meta _meta )
	{
		super( _meta ) ;
		setLayerOffset( 1 ) ;
	}

	@Override
	public void setParent( final T _parent )
	{
		super.setParent( _parent ) ;
		UIElement.connect( _parent, _parent.checkChanged(), checkSlot ) ;
	}

	@Override
	public void shutdown()
	{
		super.shutdown() ;
		final T parent = getParent() ;
		UIElement.disconnect( parent, parent.checkChanged(), checkSlot ) ;
	}

	/**
		Called when listener receives a valid DrawDelegate
		and when the parent UIElement is flagged as visible.
	*/
	@Override
	public void addDraws( final DrawDelegate<World, Draw> _delegate, final World _world )
	{
		if( getParent().isChecked() == true )
		{
			super.addDraws( _delegate, _world ) ;
		}
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
