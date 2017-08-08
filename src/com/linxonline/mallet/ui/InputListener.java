package com.linxonline.mallet.ui ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.input.InputEvent ;

import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.World ;
import com.linxonline.mallet.renderer.Camera ;
import com.linxonline.mallet.renderer.DrawDelegate ;

public abstract class InputListener<T extends UIElement> extends BaseListener<T>
{
	/**
		Called when parent UIElement is refreshing itself.
	*/
	@Override
	public void refresh() {}

	/**
		Called when parent UIElement has been flagged for shutdown.
		Clean up any resources you may have allocated.
	*/
	@Override
	public void shutdown() {}

	public Camera getCamera()
	{
		return getParent().getCamera() ;
	}
}
