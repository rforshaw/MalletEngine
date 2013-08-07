package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.id.IDInterface ; 			// IDInterface to folder agnostic place
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.resources.texture.* ;
import com.linxonline.mallet.util.sort.QuickSort ;
import com.linxonline.mallet.util.sort.SortInterface ;

public abstract class Basic2DRender extends EventUpdater implements RenderInterface
{
	protected static final String[] EVENT_TYPES = { "DRAW", "CAMERA" } ;

	protected static final int ALIGN_LEFT = 0 ;
	protected static final int ALIGN_RIGHT = 1 ;
	protected static final int ALIGN_CENTRE = 2 ;

	protected static final String BLANK_TEXT = "" ;
	protected static final Vector2 DEFAULT_OFFSET = new Vector2( 0, 0 ) ;
	protected static final Vector2 DEFAULT_ONE = new Vector2( 1.0f, 1.0f ) ;

	protected ArrayList<RenderData> content = new ArrayList<RenderData>() ;
	protected final HashMap<Integer, RenderData> hashedContent = new HashMap<Integer, RenderData>() ;

	private final EventMessenger messenger = new EventMessenger() ;
	public  final RenderInfo renderInfo = new RenderInfo( new Vector2( 800, 600 ),
														new Vector2( 800, 600 ),
														new Vector3( 400, 300, 0 ) ) ;

	public Basic2DRender() {}

	@Override
	public abstract void start() ;
	
	@Override
	public abstract void shutdown() ;

	@Override
	public void setRenderDimensions( final int _width, final int _height )
	{
		renderInfo.setRenderDimensions( new Vector2( _width, _height ) ) ;
	}

	@Override
	public void setDisplayDimensions( final int _width, final int _height )
	{
		renderInfo.setDisplayDimensions( new Vector2( _width, _height ) ) ;
	}

	@Override
	public void setCameraPosition( final Vector3 _position )
	{
		renderInfo.setCameraPosition( _position ) ;
	}

	@Override
	public abstract void draw() ;

	protected void useEvent( final Event _event )
	{
		if( _event.isEventByString( EVENT_TYPES[0] ) == true )
		{
			useEventInDraw( _event ) ;
		}
		else if( _event.isEventByString( EVENT_TYPES[1] ) == true )
		{
			useEventInCamera( _event ) ;
		}
	}

	protected void useEventInCamera( final Event _event )
	{
		final Settings camera = ( Settings )_event.getVariable() ;
		final int type = camera.getInteger( "REQUEST_TYPE", -1 ) ;
		switch( type )
		{
			case CameraRequestType.SET_CAMERA_POSITION :
			{
				renderInfo.setCameraPosition( camera.getObject( "POS", Vector3.class, null ) ) ;
				break ;
			}
			case CameraRequestType.UPDATE_CAMERA_POSITION :
			{
				renderInfo.addToCameraPosition( camera.getObject( "ACC", Vector3.class, null ) ) ;
				break ;
			}
		}
	}
	
	protected void useEventInDraw( final Event _event )
	{
		final Settings draw = ( Settings )_event.getVariable() ;
		final int type = draw.getInteger( "REQUEST_TYPE", -1 ) ;

		switch( type )
		{
			case DrawRequestType.CREATE_DRAW :
			{
				createDraw( draw ) ;
				break ;
			}
			case DrawRequestType.MODIFY_EXISTING_DRAW :
			{
				modifyDraw( draw ) ;
				break ;
			}
			case DrawRequestType.REMOVE_DRAW :
			{
				removeDraw( draw ) ;
				break ;
			}
		}
	}

	protected void createDraw( final Settings _draw )
	{
		final int type = _draw.getInteger( "TYPE", -1 ) ;
		switch( type )
		{
			case DrawRequestType.TEXTURE :
			{
				createTexture( _draw ) ;
				break ;
			}
			case DrawRequestType.GEOMETRY :
			{
				createGeometry( _draw ) ;
				break ;
			}
			case DrawRequestType.TEXT :
			{
				createText( _draw ) ;
				break ;
			}
		}
	}

	protected void modifyDraw( final Settings _draw ) {}

	protected void removeDraw( final Settings _draw )
	{
		final Integer id = _draw.getInteger( "ID", -1 ) ;
		if( hashedContent.containsKey( id ) == true )
		{
			final RenderData data = hashedContent.get( id ) ;
			content.remove( data ) ;
			hashedContent.remove( id ) ;
		}
	}

	protected abstract void createTexture( final Settings _draw ) ;
	protected abstract void createGeometry( final Settings _draw ) ;
	protected abstract void createText( final Settings _draw ) ;

	protected void passIDToCallback( final int _id, final IDInterface _idInterface )
	{
		if( _idInterface != null )
		{
			_idInterface.recievedID( _id ) ;
		}
	}

	protected void insert( final RenderData _data )
	{
		hashedContent.put( _data.id, _data ) ;
		for( final RenderData data : content )
		{
			if( _data.layer <= data.layer )
			{
				final int index = content.indexOf( data ) ;
				content.add( index, _data ) ;
				return ;
			}
		}

		content.add( _data ) ;
	}

	@Override
	public final void passEvent( final Event _event ) {}

	@Override
	public String[] getWantedEventTypes()
	{
		return EVENT_TYPES ;
	}

	public void sort() 
	{
		content = QuickSort.quicksort( content ) ;
	}

	public void clear()
	{
		content.clear() ;
		hashedContent.clear() ;
	}

	protected class RenderData implements SortInterface
	{
		public final int id ;
		public final int type ;
		public final int layer ;
		public final Vector3 position ;
		public final Settings drawData ;

		public DrawInterface drawCall = null ;

		public RenderData( final int _id,
						  final int _type,
						  final Settings _draw,
						  final Vector3 _position,
						  final int _layer )
		{
			id = _id ;
			type = _type ;
			layer = _layer ;
			drawData = _draw ;
			position = _position ;
			drawData.addInteger( "ID", _id ) ;
		}

		public int sortValue() { return layer ; }
	}

	protected interface DrawInterface
	{
		public void draw( final Settings _settings, final Vector2 _position ) ;
	}
}