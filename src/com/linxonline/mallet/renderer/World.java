package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.Comparator ;
import java.util.Collections ;

import com.linxonline.mallet.maths.IntVector2 ;
import com.linxonline.mallet.renderer.Texture ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.notification.Notification ;
import com.linxonline.mallet.util.notification.Notification.Notify ;

public final class World implements IManageBuffers
{
	private final static Utility utility = new Utility() ;

	private final int index = utility.getGlobalIndex() ;
	private final int order ;
	private final String id ;

	private final Notification<World> renderNotification = new Notification<World>() ;

	private final AttachmentType[] attachments ;
	private final Texture.Meta[] metas ;

	private final List<Camera> cameras ;
	private final List<ICompatibleBuffer> buffers ;

	private final Colour clearColour = new Colour( 0, 0, 0, 0 ) ;

	public World( final String _id )
	{
		this( _id, 0 ) ;
	}

	public World( final String _id, final int _order )
	{
		this( _id, _order, new AttachmentType[] { AttachmentType.COLOUR } ) ;
	}

	public World( final String _id, final int _order, final AttachmentType[] _attachments )
	{
		id = _id ;
		order = _order ;
		attachments = _attachments ;

		final int size = attachments.length ;
		metas = new Texture.Meta[size] ;
		for( int i = 0; i < size; ++i )
		{
			metas[i] = new Texture.Meta( id, i, 1280, 720 ) ;
		}

		cameras = MalletList.<Camera>newList() ;
		buffers = MalletList.<ICompatibleBuffer>newList() ;
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

	public Camera addCamera( final Camera _camera )
	{
		cameras.add( _camera ) ;
		return _camera ;
	}

	public void removeCamera( final Camera _camera )
	{
		cameras.remove( _camera ) ;
	}

	public List<Camera> getCameras()
	{
		return cameras ;
	}

	@Override
	public <T extends IManageCompatible> T addBuffer( final T _buffer )
	{
		insert( _buffer, buffers ) ;
		return _buffer ;
	}

	private static void insert( final IManageCompatible _insert, final List<ICompatibleBuffer> _list )
	{
		final int size = _list.size() ;
		for( int i = 0; i < size; i++ )
		{
			final ICompatibleBuffer toCompare = _list.get( i ) ;
			if( _insert.getOrder() <= toCompare.getOrder() )
			{
				_list.add( i, _insert ) ;		// Insert at index location
				return ;
			}
		}

		_list.add( _insert ) ;
	}

	@Override
	public <T extends IManageCompatible> void removeBuffer( final T _buffer )
	{
		buffers.remove( _buffer ) ;
	}

	@Override
	public List<ICompatibleBuffer> getBuffers()
	{
		return buffers ;
	}

	@Override
	public void requestUpdate()
	{
		WorldAssist.update( this ) ;
	}

	/**
		Trigger a call to sort the order in which
		the cameras are rendered to.
		This is the same as if you called:
		Collections.sort( cameras, ... ) ;
	*/
	public void sortCameras( final Comparator<Camera> _c )
	{
		Collections.sort( cameras, _c ) ;
	}

	/**
		Trigger a call to sort the order in which
		the buffers are rendered to.
		This is the same as if you called:
		Collections.sort( buffers, ... ) ;
	*/
	public void sortBuffers( final Comparator<ICompatibleBuffer> _c )
	{
		Collections.sort( buffers, _c ) ;
	}

	public void setClearColour( final Colour _colour )
	{
		clearColour.changeColour( ( _colour != null ) ? _colour : Colour.black() ) ;
	}

	public Colour getClearColour()
	{
		return clearColour ;
	}

	public void setRenderDimensions( final int _x, final int _y, final int _width, final int _height )
	{
		for(int i = 0; i < metas.length; ++i)
		{
			metas[i].set( _width, _height ) ;
		}

		renderNotification.inform( this ) ;
	}

	public IntVector2 getRenderDimensions( final IntVector2 _fill )
	{
		_fill.setXY( metas[0].dimensions ) ;
		return _fill ;
	}

	public Texture.Meta getMeta( final int _index )
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

	public int getOrder()
	{
		return order ;
	}

	public int index()
	{
		return index ;
	}

	@Override
	public boolean equals( final Object _obj )
	{
		if( this == _obj )
		{
			return true ;
		}

		if( _obj == null )
		{
			return false ;
		}

		if( !( _obj instanceof World ) )
		{
			return false ;
		}

		final World w = ( World )_obj ;
		for( int i = 0; i < attachments.length; ++i )
		{
			if( attachments[i] != w.attachments[i] )
			{
				return false ;
			}
		}

		return true ;
	}

	@Override
	public int hashCode()
	{
		return index ;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder buffer = new StringBuilder() ;
		buffer.append( "World: " ) ; buffer.append( id ) ;
		return buffer.toString() ;
	}

	public enum AttachmentType
	{
		COLOUR,
		DEPTH,
		STENCIL
	}
}
