package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.id.IDInterface ; 			// IDInterface to folder agnostic place
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.resources.* ;

import com.linxonline.mallet.util.caches.Cacheable ;

import com.linxonline.mallet.util.sort.SortInterface ;
import com.linxonline.mallet.util.sort.QuickSort ;

/**
	Implements the boiler plate code required by most 2D renderers.
	Handles the event calls for drawing objects to the screen.
	
	Use G2DRenderer or GLRenderer as an example on how to extend this class. 
*/
public abstract class Basic2DRender extends EventUpdater implements RenderInterface
{
	protected static final int ALIGN_LEFT = 0 ;
	protected static final int ALIGN_RIGHT = 1 ;
	protected static final int ALIGN_CENTRE = 2 ;

	protected static final String BLANK_TEXT = "" ;
	protected static final Vector2 DEFAULT_OFFSET = new Vector2( 0, 0 ) ;
	protected static final Vector2 DEFAULT_ONE = new Vector2( 1.0f, 1.0f ) ;

	protected int renderIter = 0 ;			// What render iteration we are currently on
	protected float updateDT = 0.0f ;		// Delta time of the update cycle
	protected float drawDT = 0.0f ;			// Delta time of the render cycle

	protected final RenderState state = new RenderState() ;;
	protected final Vector3 cameraScale = new Vector3( 1, 1, 1 ) ;

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
		state.updateState() ;
		updateDT = _dt ;
		renderIter = 0 ;
	}

	@Override
	public abstract void draw( final float _dt ) ;

	@Override
	protected void useEvent( final Event<?> _event )
	{
		final EventType type = _event.getEventType() ;
		if( EventType.equals( "DRAW", type ) == true )
		{
			useEventInDraw( _event ) ;			// EventType[0]
		}
		else if( EventType.equals( "CAMERA", type ) == true )
		{
			useEventInCamera( _event ) ;		// EventType[1]
		}
	}

	protected void useEventInCamera( final Event<?> _event )
	{
		final Settings camera = ( Settings )_event.getVariable() ;
		final CameraRequestType type = camera.getObject( "REQUEST_TYPE", null ) ;

		switch( type )
		{
			case SET_CAMERA_POSITION :
			{
				renderInfo.setCameraPosition( camera.<Vector3>getObject( "POS", null ) ) ;
				break ;
			}
			case UPDATE_CAMERA_POSITION :
			{
				renderInfo.addToCameraPosition( camera.<Vector3>getObject( "ACC", null ) ) ;
				break ;
			}
			case SET_CAMERA_SCALE :
			{
				cameraScale.setXYZ( camera.<Vector3>getObject( "SCALE", null ) ) ;
				break ;
			}
			case UPDATE_CAMERA_SCALE :
			{
				cameraScale.add( camera.<Vector3>getObject( "SCALE", null ) ) ;
				break ;
			}
		}
	}

	protected void useEventInDraw( final Event<?> _event )
	{
		final Settings draw = ( Settings )_event.getVariable() ;
		final DrawRequestType type = draw.getObject( "REQUEST_TYPE", null ) ;

		switch( type )
		{
			case CREATE_DRAW           : createDraw( draw ) ;    break ;
			case MODIFY_EXISTING_DRAW  : modifyDraw( draw ) ;    break ;
			case REMOVE_DRAW           : removeDraw( draw ) ;    break ;
			case CREATE_SHADER_PROGRAM : createProgram( draw ) ; break ;
			case GARBAGE_COLLECT_DRAW  : clean() ;               break ;
		}
	}

	protected void createDraw( final Settings _draw )
	{
		final DrawRequestType type = _draw.getObject( "TYPE", null ) ;
		switch( type )
		{
			case TEXTURE  : createTexture( _draw ) ; break ;
			case GEOMETRY : createGeometry( _draw ) ; break ;
			case TEXT     : createText( _draw ) ; break ;
		}
	}

	protected void modifyDraw( final Settings _draw ) {}

	protected void removeDraw( final Settings _draw )
	{
		final int id = _draw.getInteger( "ID", -1 ) ;
		state.remove( id ) ;
	}

	protected void createProgram( final Settings _draw ) {}
	protected void removeProgram( final Settings _draw ) {}

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
		state.insert( _data.getID(), _data ) ;
	}

	@Override
	public final void passEvent( final Event<?> _event ) {}

	@Override
	public ArrayList<EventType> getWantedEventTypes()
	{
		final ArrayList<EventType> types = new ArrayList<EventType>() ;
		types.add( EventType.get( "DRAW" ) ) ;
		types.add( EventType.get( "CAMERA" ) ) ;
		return types ;
	}

	/**
		Sort the contents of the state based on their layer.
	*/
	public void sort() 
	{
		state.sort() ;
	}

	/**
		Clear the contents of the state, this will 
		remove anything that is being drawn.
		This does not remove the resources that were 
		in use.
	*/
	public void clear()
	{
		state.clear() ;
	}

	/**
		Remove resources that aren't being used.
	*/
	public abstract void clean() ;
	
	protected void calculateInterpolatedPosition( final Vector3 _old, final Vector3 _current, final Vector2 _position )
	{
		// Calculate the how many render iterations must take place to reach 
		// the current states positions.
		// xDiff & yDiff represents the distance change for one render iteration.
		// Linearly interpolate to the current state positions.
		final int renderDiff = ( int )( updateDT / drawDT ) ;
		final float xDiff = ( _current.x - _old.x ) / renderDiff ;
		final float yDiff = ( _current.y - _old.y ) / renderDiff ;

		_position.x = _old.x + ( xDiff * renderIter ) ;
		_position.y = _old.y + ( yDiff * renderIter ) ;
	}

	protected class RenderState implements RenderStateInterface<Integer, RenderData>
	{
		protected final ArrayList<Integer> toRemove = new ArrayList<Integer>() ;
		protected final ArrayList<Vector3> oldState = new ArrayList<Vector3>() ;								// Old State
		protected ArrayList<RenderData> content = new ArrayList<RenderData>() ;									// Current State - loopable
		protected final HashMap<Integer, RenderData> hashedContent = new HashMap<Integer, RenderData>() ;		// Current State - searchable

		protected final Vector2 position = new Vector2() ;

		public RenderState() {}

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
				if( _data.getLayer() <= data.getLayer() )
				{
					final int index = content.indexOf( data ) ;
					content.add( index, _data ) ;		// Insert at index location
					oldState.add( index, new Vector3( _data.getPosition() ) ) ;
					return ;
				}
			}

			content.add( _data ) ;						// Add to end of array
			oldState.add( new Vector3( _data.getPosition() ) ) ;
		}

		/**
			Remove the passed in id from the render state.
			This will add the id to a queue, which will get the 
			content associated with the id to be removed at 
			the most opportune moment. Note: resources used 
			by the id will not be automatically destroyed too.
		*/
		@Override
		public void remove( Integer _id )
		{
			//Logger.println( getName() + " - Request Remove: " + _id, Logger.Verbosity.MINOR ) ;
			toRemove.add( _id ) ;
		}

		/**
			Remove the Render Data from the Render State.
			Making sure to decrement the resource counts.
		*/
		public void removeRenderData()
		{
			for( final Integer id : toRemove )
			{
				//Logger.println( getName() + " - Remove: " + id, Logger.Verbosity.MINOR ) ;
				final RenderData data = getData( id ) ;
				if( data != null )
				{
					hashedContent.remove( id ) ;
					final int index = content.indexOf( data ) ;
					content.remove( index ) ;
					oldState.remove( index ) ;
					data.removeResources() ;		// Remove any references to resources
				}
			}
			toRemove.clear() ;
		}

		@Override
		public void retireCurrentState()
		{
			final int size = content.size() ;
			for( int i = 0; i < size; ++i )
			{
				// Set the positions of old-state to the possitions
				// of the current-state. Current-state data will 
				// soon be updated.
				final RenderData data = content.get( i ) ;
				oldState.get( i ).setXYZ( data.getPosition() ) ;
			}
		}

		/**
			Interpolate the render state based on position.
			We need to ensure that the array used to store the 
			states old positions are the same size as the current state.
		*/
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
			// Ensure that we unregister all the resources
			// we are currently using. If we don't, we'll 
			// most likely be leaking memory!
			final int size = content.size() ;
			for( int i = 0; i < size; ++i )
			{
				content.get( i ).removeResources() ;
			}

			toRemove.clear() ;
			oldState.clear() ;
			content.clear() ;
			hashedContent.clear() ;
		}

		public boolean isStateStable()
		{
			return oldState.size() == content.size() ;
		}

		public void updateState()
		{
			retireCurrentState() ;
		}

		public void draw()
		{
			position.setXY( 0.0f, 0.0f ) ;
			final int size = content.size() ;
			for( int i = 0; i < size; ++i )
			{
				final RenderData data = state.getCurrentPosition( i, position ) ;
				data.draw( position ) ;
			}
		}

		protected RenderData getCurrentPosition( final int _index, final Vector2  _position )
		{
			final Vector3 old = oldState.get( _index ) ;
			final RenderData data = content.get( _index ) ;
			final Vector3 current = data.getPosition() ;

			switch( data.getInterpolation() )
			{
				case NONE   : _position.setXY( current.x, current.y ) ; break ;
				case LINEAR :
				default     :
				{
					final int renderDiff =  ( int )( updateDT / drawDT ) ;
					final float xDiff = ( current.x - old.x ) / renderDiff ;
					final float yDiff = ( current.y - old.y ) / renderDiff ;

					_position.x = old.x + ( xDiff * renderIter ) ;
					_position.y = old.y + ( yDiff * renderIter ) ;
					break ;
				}
			}

			return data ;
		}

		@Override
		public void sort()
		{
			content = QuickSort.quicksort( content ) ;
		}

		@Override
		public ArrayList<RenderData> getContent()
		{
			return content ;
		}

		@Override
		public RenderData getData( Integer _id )
		{
			return hashedContent.get( _id ) ;
		}
	}

	public static abstract class RenderData implements SortInterface, Cacheable
	{
		public Settings data = null ;					// Data to be drawn
		public DrawInterface call = null ;				// Draw technique to use
		public DrawRequestType type = null ;			// Texture, Geometry, Text
		public DrawRequestType updateType = null ;		// Continuous, On-demand

		public RenderData() {}

		public void set( final Settings _data, final DrawInterface _call, final DrawRequestType _type, final DrawRequestType _updateType )
		{
			data = _data ;
			call = _call ;
			type = _type ;
			updateType = _updateType ;
		}

		public void draw( final Vector2 _position )
		{
			call.draw( this, _position ) ;
		}

		//public abstract void updateData() ;

		public abstract int getID() ;							// Unique identifier
		public abstract Vector3 getPosition() ;					// Position in 3D space
		public abstract int getLayer() ;						// Order to be rendered
		public abstract Interpolation getInterpolation() ;		// How the renderer should handle updating its data

		public abstract boolean isUI() ;
		
		public abstract void copy( final RenderData _data ) ;
		public abstract void removeResources() ;
		public abstract int sortValue() ;

		public DrawRequestType getUpdateType()
		{
			return updateType ;
		}

		@Override
		public void reset()
		{
			data = null ;
			call = null ;
			type = null ;
			updateType = null ;
		}
	}

	public interface DrawInterface<T extends RenderData>
	{
		public void draw( final T _data, final Vector2 _position ) ;
	}
}