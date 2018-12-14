package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.IntVector2 ;

public class CoreWorld implements World
{
	private final int order ;

	private final IntVector2 renderPosition = new IntVector2( 0, 0 ) ;
	private final MalletTexture.Meta meta ;

	private final IntVector2 displayPosition = new IntVector2( 0, 0 ) ;
	private final IntVector2 display = new IntVector2( 1280, 720 ) ;

	private final Listener listener ;
	
	public CoreWorld( final String _id, final int _order, final Listener _listener )
	{
		listener = _listener ;
		meta = new MalletTexture.Meta( _id, 1280, 720 ) ;
		order = _order ;
	}

	public void setRenderDimensions( final int _x, final int _y, final int _width, final int _height )
	{
		renderPosition.setXY( _x, _y ) ;
		meta.set( _width, _height ) ;
		listener.renderChanged( this ) ;
	}

	public void setDisplayDimensions( final int _x, final int _y, final int _width, final int _height )
	{
		displayPosition.setXY( _x, _y ) ;
		display.setXY( _width, _height ) ;
		listener.displayChanged( this ) ;
	}

	/**
		Unique identifier to acquire the world via a DrawDelegate.
	*/
	public String getID()
	{
		return meta.getPath() ;
	}

	/**
		Used to inform the World State what order it should be called in.
	*/
	public int getOrder()
	{
		return order ;
	}

	public IntVector2 getRenderPosition()
	{
		return renderPosition ;
	}

	public IntVector2 getRender()
	{
		return meta.dimensions ;
	}

	public IntVector2 getDisplayPosition()
	{
		return displayPosition ;
	}

	public IntVector2 getDisplay()
	{
		return display ;
	}

	public interface Listener
	{
		public void renderChanged( final CoreWorld _world ) ;
		public void displayChanged( final CoreWorld _world ) ;
		public void orderChanged( final CoreWorld _world ) ;
	}
}
