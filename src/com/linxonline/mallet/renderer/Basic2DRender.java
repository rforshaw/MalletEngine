package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.id.IDInterface ; 			// IDInterface to folder agnostic place
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.resources.* ;
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

	protected int renderIter = 0 ;			// What render iteration we are currently on
	protected float updateDT = 0.0f ;		// Delta time of the update cycle
	protected float drawDT = 0.0f ;			// Delta time of the render cycle

	protected final Vector3 cameraScale = new Vector3( 1, 1, 1 ) ;
	protected final RenderState state = new RenderState() ;

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
	public void updateState( final float _dt )
	{
		// Make a copy of the currentState for interpolation
		// between frames.
		state.retireCurrentState() ;
		updateDT = _dt ;
		renderIter = 0 ;
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
			case CameraRequestType.SET_CAMERA_SCALE :
			{
				cameraScale.setXYZ( camera.<Vector3>getObject( "SCALE", null ) ) ;
				break ;
			}
			case CameraRequestType.UPDATE_CAMERA_SCALE :
			{
				cameraScale.add( camera.<Vector3>getObject( "SCALE", null ) ) ;
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
		state.remove( id ) ;
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
		state.insert( _data.id, _data ) ;
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
		state.sort() ;
	}

	public void clear()
	{
		state.clear() ;
	}

	protected void CalculateInterpolatedPosition( Vector3 _old, Vector3 _current, Vector2 _position )
	{
		final int renderDiff = ( int )( updateDT / drawDT ) ;
		final float xDiff = ( _current.x - _old.x ) / renderDiff ;
		final float yDiff = ( _current.y - _old.y ) / renderDiff ;

		_position.x = _old.x + ( xDiff * renderIter ) ;
		_position.y = _old.y + ( yDiff * renderIter ) ;
	}

	protected class RenderState implements RenderStateInterface<Integer, RenderData>
	{
		protected final ArrayList<Vector3> oldState = new ArrayList<Vector3>() ;							// Old State
		protected ArrayList<RenderData> content = new ArrayList<RenderData>() ;								// Current State - loopable
		protected final HashMap<Integer, RenderData> hashedContent = new HashMap<Integer, RenderData>() ;	// Current State - searchable

		@Override
		public void add( Integer _id, RenderData _data )
		{
			insert( _id, _data ) ;
		}

		@Override
		public void insert( Integer _id, RenderData _data )
		{
			hashedContent.put( _id, _data ) ;
			for( final RenderData data : content )
			{
				if( _data.layer <= data.layer )
				{
					final int index = content.indexOf( data ) ;
					content.add( index, _data ) ;		// Insert at index location
					return ;
				}
			}

			content.add( _data ) ;						// Add to end of array
		}

		@Override
		public void remove( Integer _id )
		{
			final RenderData data = getData( _id ) ;
			if( data != null )
			{
				data.unregisterResources() ;		// Decrement resource count
				hashedContent.remove( _id ) ;
				content.remove( data ) ;
			}
		}

		@Override
		public void retireCurrentState()
		{
			final int size = content.size() ;
			setOldStateSize( size ) ;

			for( int i = 0; i < size; ++i )
			{
				// Set the positions of old-state to the possitions
				// of the current-state. Current-state data will 
				// soon be updated.
				final RenderData data = content.get( i ) ;
				oldState.get( i ).setXYZ( data.position ) ;
			}
		}

		protected void setOldStateSize( final int _size )
		{
			final int oldSize = oldState.size() ;
			if( oldSize < _size )
			{
				// Add new vector states for interpolation
				int toAdd = _size - oldSize ;
				for( int i = 0; i < toAdd; ++i )
				{
					oldState.add( new Vector3() ) ;
				}
			}
			else if( oldSize > _size )
			{
				// Remove vector states
				int toRemove = oldSize - _size ;
				oldState.subList( 0, toRemove ).clear() ;
			}
		}

		@Override
		public void clear()
		{
			final int size = content.size() ;
			for( int i = 0; i < size; ++i )
			{
				content.get( i ).unregisterResources() ;
			}

			oldState.clear() ;
			content.clear() ;
			hashedContent.clear() ;
		}

		public boolean isStateStable()
		{
			return oldState.size() == content.size() ;
		}

		public void draw()
		{
			final Vector2 position = new Vector2() ;
			final int size = content.size() ;
			for( int i = 0; i < size; ++i )
			{
				final RenderData data = state.getCurrentPosition( i, position ) ;
				data.drawCall.draw( data.drawData, position ) ;
			}
		}
		
		protected RenderData getCurrentPosition( final int _index, final Vector2  _position )
		{
			final Vector3 old = oldState.get( _index ) ;
			final RenderData data = content.get( _index ) ;
			final Vector3 current = data.position ;

			final int renderDiff =  ( int )( updateDT / drawDT ) ;
			final float xDiff = ( current.x - old.x ) / renderDiff ;
			final float yDiff = ( current.y - old.y ) / renderDiff ;

			_position.x = old.x + ( xDiff * renderIter ) ;
			_position.y = old.y + ( yDiff * renderIter ) ;

			return data ;
		}

		@Override
		public void sort()
		{
			content = QuickSort.quicksort( content ) ;
		}

		@Override
		public RenderData getData( Integer _id )
		{
			if( hashedContent.containsKey( _id ) == true )
			{
				return hashedContent.get( _id ) ;
			}

			return null ;
		}
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

		public void unregisterResources() {}
	}
	
	protected interface DrawInterface
	{
		public void draw( final Settings _settings, final Vector2 _position ) ;
	}
}