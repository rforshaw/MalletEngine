package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.maths.IntVector2 ;
import com.linxonline.mallet.renderer.MalletTexture ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.notification.Notification ;
import com.linxonline.mallet.util.notification.Notification.Notify ;

public class World
{
	private final static Utility utility = new Utility() ;

	private final int index = utility.getGlobalIndex() ;
	private final String id ;

	private final Notification<World> renderNotification = new Notification<World>() ;
	private final Notification<World> displayNotification = new Notification<World>() ;

	private final IntVector2 renderPosition = new IntVector2( 0, 0 ) ;
	private final IntVector2 displayPosition = new IntVector2( 0, 0 ) ;
	private final IntVector2 display = new IntVector2( 1280, 720 ) ;

	private final AttachmentType[] attachments ;
	private final MalletTexture.Meta[] metas ;

	private final List<Camera> cameras ;
	private final List<ABuffer> buffers ;

	public World( final String _id )
	{
		this( _id, new AttachmentType[] { AttachmentType.COLOUR } ) ;
	}
	
	public World( final String _id, final AttachmentType[] _attachments )
	{
		id = _id ;
		attachments = _attachments ;

		final int size = attachments.length ;
		metas = new MalletTexture.Meta[size] ;
		for( int i = 0; i < size; ++i )
		{
			metas[i] = new MalletTexture.Meta( id, i, display.x, display.y ) ;
		}

		cameras = MalletList.<Camera>newList() ;
		buffers = MalletList.<ABuffer>newList() ;
	}

	public Notify<World> attachRenderNotify( final Notify<World> _notify )
	{
		renderNotification.addNotify( _notify ) ;
		_notify.inform( this ) ;
		return _notify ;
	}

	public void dettachRenderNotify( final Notify<World> _notify )
	{
		renderNotification.removeNotify( _notify ) ;
	}

	public Notify<World> attachDisplayNotify( final Notify<World> _notify )
	{
		displayNotification.addNotify( _notify ) ;
		_notify.inform( this ) ;
		return _notify ;
	}

	public void dettachDisplayNotify( final Notify<World> _notify )
	{
		displayNotification.removeNotify( _notify ) ;
	}

	public Camera[] addCameras( final Camera ... _cameras )
	{
		for( final Camera camera : _cameras )
		{
			cameras.add( camera ) ;
		}
		return _cameras ;
	}

	public void removeCameras( final Camera ... _cameras )
	{
		for( final Camera camera : _cameras )
		{
			cameras.remove( camera ) ;
		}
	}

	public List<Camera> getCameras()
	{
		return cameras ;
	}

	public ABuffer[] addBuffers( final ABuffer ... _buffers )
	{
		for( final ABuffer buffer : _buffers )
		{
			insert( buffer, buffers ) ;
		}
		return _buffers ;
	}

	private static void insert( final ABuffer _insert, final List<ABuffer> _list )
	{
		final int size = _list.size() ;
		for( int i = 0; i < size; i++ )
		{
			final ABuffer toCompare = _list.get( i ) ;
			if( _insert.getOrder() <= toCompare.getOrder() )
			{
				_list.add( i, _insert ) ;		// Insert at index location
				return ;
			}
		}

		_list.add( _insert ) ;
	}

	public void removeBuffers( final ABuffer ... _buffers )
	{
		for( final ABuffer buffer : _buffers )
		{
			buffers.remove( buffer ) ;
		}
	}

	public List<ABuffer> getBuffers()
	{
		return buffers ;
	}

	public void setRenderDimensions( final int _x, final int _y, final int _width, final int _height )
	{
		renderPosition.setXY( _x, _y ) ;
		for(int i = 0; i < metas.length; ++i)
		{
			metas[i].set( _width, _height ) ;
		}

		renderNotification.inform( this ) ;
	}

	public IntVector2 getRenderPosition( final IntVector2 _fill )
	{
		_fill.setXY( renderPosition ) ;
		return _fill ;
	}

	public IntVector2 getRenderDimensions( final IntVector2 _fill )
	{
		_fill.setXY( metas[0].dimensions ) ;
		return _fill ;
	}

	public void setDisplayDimensions( final int _x, final int _y, final int _width, final int _height )
	{
		displayPosition.setXY( _x, _y ) ;
		display.setXY( _width, _height ) ;

		displayNotification.inform( this ) ;
	}

	public IntVector2 getDisplayPosition( final IntVector2 _fill )
	{
		_fill.setXY( displayPosition ) ;
		return _fill ;
	}

	public IntVector2 getDisplayDimensions( final IntVector2 _fill )
	{
		_fill.setXY( display ) ;
		return _fill ;
	}

	public MalletTexture.Meta getMeta( final int _index )
	{
		return metas[_index] ;
	}

	public AttachmentType[] getAttachments()
	{
		return attachments ;
	}

	public String getID()
	{
		return id ;
	}

	public int index()
	{
		return index ;
	}

	public enum AttachmentType
	{
		COLOUR,
		DEPTH,
		STENCIL
	}
}
