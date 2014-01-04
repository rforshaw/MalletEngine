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

/**
	Implements the boiler plate code required by most 2D renderers.
	Handles the event calls for drawing objects to the screen.
	
	Use G2DRenderer or GLRenderer as an example on how to extend this class. 
*/
public abstract class Basic2DRender extends EventUpdater implements RenderInterface
{
	protected static final String[] EVENT_TYPES = { "DRAW", "CAMERA" } ;

	protected static final int ALIGN_LEFT = 0 ;
	protected static final int ALIGN_RIGHT = 1 ;
	protected static final int ALIGN_CENTRE = 2 ;

	protected static final String BLANK_TEXT = "" ;
	protected static final Vector2 DEFAULT_OFFSET = new Vector2( 0, 0 ) ;
	protected static final Vector2 DEFAULT_ONE = new Vector2( 1.0f, 1.0f ) ;

	protected float accumulatedDeltaTime = 0.0f ;
	protected final RenderState oldState = new RenderState() ;
	protected final RenderState currentState = new RenderState() ;

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
		System.out.println( "SETTING CAMERA POSITION: " + _position ) ;
		renderInfo.setCameraPosition( _position ) ;
	}

	@Override
	public void updateState()
	{
		// Make a copy of the currentState for interpolation
		// between frames.
		oldState.copy( currentState ) ;
		accumulatedDeltaTime = 0.0f ;
	}

	@Override
	public abstract void draw( final float _dt ) ;

	protected void useEvent( final Event _event )
	{
		if( _event.isEventByString( EVENT_TYPES[0] ) == true )			// DRAW
		{
			useEventInDraw( _event ) ;
		}
		else if( _event.isEventByString( EVENT_TYPES[1] ) == true )		// CAMERA
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
				renderInfo.setCameraPosition( camera.<Vector3>getObject( "POS", null ) ) ;
				break ;
			}
			case CameraRequestType.UPDATE_CAMERA_POSITION :
			{
				renderInfo.addToCameraPosition( camera.<Vector3>getObject( "ACC", null ) ) ;
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
		currentState.remove( id ) ;
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
		currentState.insert( _data.id, _data ) ;
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
		currentState.sort() ;
	}

	public void clear()
	{
		currentState.clear() ;
	}

	protected void CalculateInterpolatedPosition( final float _dt, RenderData _old, RenderData _current, Vector2 _position )
	{
		final Vector3 oldPos = _old.position ;
		final Vector3 currentPos = _current.position ;

		_position.setXY( currentPos.x - oldPos.x, currentPos.y - oldPos.y ) ;
		_position.multiply( _dt ) ;
		_position.setXY( oldPos.x + _position.x, oldPos.y + _position.y ) ;
	}

	protected class RenderData implements SortInterface
	{
		public int id ;
		public int type ;
		public int layer ;
		public final Vector3 position ;
		public Settings drawData ;

		public DrawInterface drawCall = null ;

		public RenderData()
		{
			position = new Vector3() ;
		}
		
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

		public void copy( final RenderData _data )
		{
			id = _data.id ;
			type = _data.type ;
			layer = _data.layer ;

			position.setXYZ( _data.position ) ;

			drawData = _data.drawData ;
			drawCall = _data.drawCall ;
		}

		public int sortValue()
		{
			return layer ;
		}
	}

	protected class RenderState implements RenderStateInterface<Integer, RenderData>
	{
		protected ArrayList<RenderData> content = new ArrayList<RenderData>() ;
		protected final HashMap<Integer, RenderData> hashedContent = new HashMap<Integer, RenderData>() ;
	
		public void add( Integer _id, RenderData _data )
		{
			content.add( _data ) ;
		}

		public void insert( Integer _id, RenderData _data )
		{
			hashedContent.put( _id, _data ) ;
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

		public void remove( Integer _id )
		{
			if( hashedContent.containsKey( _id ) == true )
			{
				final RenderData data = hashedContent.get( _id ) ;
				content.remove( data ) ;
				hashedContent.remove( _id ) ;
			}
		}

		public void copy( RenderStateInterface<Integer, RenderData> _state ) {}

		public void copy( RenderState _state )
		{
			int currentSize = _state.size() ;
			int oldSize = size() ;

			if( oldSize < currentSize )
			{
				int toAdd = currentSize - oldSize ;
				for( int i = 0; i < toAdd; ++i )
				{
					add( null, new RenderData() ) ;
				}
			}
			else if( oldSize > currentSize )
			{
				// Remove some RenderData's
			}
			
			for( int i = 0; i < currentSize; ++i )
			{
				getDataAt( i ).copy( _state.getDataAt( i ) ) ;
			}
		}

		public void clear()
		{
			content.clear() ;
			hashedContent.clear() ;
		}

		public void sort()
		{
			content = QuickSort.quicksort( content ) ;
		}

		public int size()
		{
			return content.size() ;
		}
		
		public RenderData getDataAt( int _index )
		{
			return content.get( _index ) ;
		}
		
		public RenderData getData( Integer _id )
		{
			return null ;
		}
	}

	protected interface DrawInterface
	{
		public void draw( final Settings _settings, final Vector2 _position ) ;
	}
}