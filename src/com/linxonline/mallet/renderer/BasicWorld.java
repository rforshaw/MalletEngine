package com.linxonline.mallet.renderer ;

import java.util.Set ;

import com.linxonline.mallet.maths.IntVector2 ;

import com.linxonline.mallet.util.notification.Notification ;
import com.linxonline.mallet.util.notification.Notification.Notify ;

/**
	Implements common requirements for using World.
	Basic World manages draw object state and Camera state.
*/
public abstract class BasicWorld<D extends DrawData, C extends CameraData> implements World
{
	private final Notification<World> renderNotification = new Notification<World>() ;
	private final Notification<World> displayNotification = new Notification<World>() ;

	private final int order ;

	private final IntVector2 renderPosition = new IntVector2( 0, 0 ) ;
	private final MalletTexture.Meta meta ;

	private final IntVector2 displayPosition = new IntVector2( 0, 0 ) ;
	private final IntVector2 display = new IntVector2( 1280, 720 ) ;

	public BasicWorld( final String _id, final int _order )
	{
		meta = new MalletTexture.Meta( _id, 1280, 720 ) ;
		order = _order ;
	}

	public Notify<World> addRenderNotify( final Notify<World> _notify )
	{
		renderNotification.addNotify( _notify ) ;
		_notify.inform( this ) ;
		return _notify ;
	}

	public void removeRenderNotify( final Notify<World> _notify )
	{
		renderNotification.removeNotify( _notify ) ;
	}
	
	public Notify<World> addDisplayNotify( final Notify<World> _notify )
	{
		displayNotification.addNotify( _notify ) ;
		_notify.inform( this ) ;
		return _notify ;
	}

	public void removeDisplayNotify( final Notify<World> _notify )
	{
		displayNotification.removeNotify( _notify ) ;
	}

	public void setRenderDimensions( final int _x, final int _y, final int _width, final int _height )
	{
		renderPosition.setXY( _x, _y ) ;
		meta.set( _width, _height ) ;

		renderNotification.inform( this ) ;
	}

	public void setDisplayDimensions( final int _x, final int _y, final int _width, final int _height )
	{
		displayPosition.setXY( _x, _y ) ;
		display.setXY( _width, _height ) ;

		displayNotification.inform( this ) ;
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

	public MalletTexture.Meta getMeta()
	{
		return meta ;
	}

	public abstract void init() ;

	public abstract void addDraw( final D _data ) ;

	public abstract void removeDraw( final D _data ) ;

	public abstract void addCamera( final C _camera ) ;

	public abstract void removeCamera( final C _camera ) ;

	public abstract C getCamera( final String _id ) ;

	/**
		Update the draw objects position, rotation, and scale.
		This will eventually call DrawData.upload
	*/
	public abstract void update( final int _diff, final int _iteration ) ;

	/**
		Update the cameras position, rotation, and scale.
		Call the cameras custom draw interface to begin 
		rendering to the framebuffer.
	*/
	public abstract void draw() ;

	public abstract void shutdown() ;

	public abstract void sort() ;

	public abstract void clean( final Set<String> _activeKeys ) ;

	public abstract void clear() ;

	public String toString()
	{
		return meta.toString() ;
	}
}
