package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

public abstract class BasicRenderer implements RenderInterface
{
	protected static final MalletColour WHITE = MalletColour.white() ;
	protected static final MalletColour BLACK = MalletColour.black() ;

	protected static final MalletColour RED   = MalletColour.red() ;
	protected static final MalletColour GREEN = MalletColour.green() ;
	protected static final MalletColour BLUE  = MalletColour.blue() ;

	protected final WorldState worlds ;

	protected final EventController controller = new EventController() ;
	protected final RenderInfo info = new RenderInfo( new Vector2( 800, 600 ),
													  new Vector2( 800, 600 ) ) ;

	protected float drawDT   = 0.0f ;
	protected float updateDT = 0.0f ;
	protected int renderIter = 0 ;

	public BasicRenderer( final WorldState _worlds )
	{
		worlds = _worlds ;
	}

	@Override
	public void start()
	{
		controller.addEventProcessor( new EventProcessor<DrawDelegateCallback>( "DRAW_DELEGATE", "DRAW_DELEGATE" )
		{
			public void processEvent( final Event<DrawDelegateCallback> _event )
			{
				final DrawDelegateCallback callback = _event.getVariable() ;
				callback.callback( constructDrawDelegate() ) ;
			}
		} ) ;

		controller.addEventProcessor( new EventProcessor<DrawDelegateCallback>( "DRAW_CLEAN", "DRAW_CLEAN" )
		{
			public void processEvent( final Event<DrawDelegateCallback> _event )
			{
				clean() ;
			}
		} ) ;
	}

	/**
		Inform the renderer to start the clean-up process.
		Shutdown any active devices and ensure we leave it 
		in the same state as we got it in.
	*/
	@Override
	public abstract void shutdown() ;

	/**
		Construct any Assistors that are required.
		TextureAssist, FontAssist, DrawAssist, CameraAssist.
	*/
	@Override
	public abstract void initAssist() ;

	public abstract DrawData.UploadInterface getBasicUpload() ;
	public abstract DrawData.UploadInterface getTextUpload() ;

	public abstract Camera.DrawInterface getCameraDraw() ;

	/**
		Allows implementations to clean-up other systems using 
		or is used by DrawData.
	*/
	public abstract DrawState.RemoveDelegate constructRemoveDelegate() ;

	protected DrawDelegate constructDrawDelegate()
	{
		return new DrawDelegate()
		{
			private final ArrayList<Draw> data = new ArrayList<Draw>() ;
			private final ArrayList<Camera> cameras = new ArrayList<Camera>() ;

			@Override
			public void addTextDraw( final Draw _draw )
			{
				addTextDraw( _draw, null ) ;
			}

			@Override
			public void addBasicDraw( final Draw _draw )
			{
				addBasicDraw( _draw, null ) ;
			}

			@Override
			public void addCamera( final Camera _camera )
			{
				addCamera( _camera, null ) ;
			}

			@Override
			public void removeCamera( final Camera _camera )
			{
				removeCamera( _camera, null ) ;
			}

			@Override
			public void addTextDraw( final Draw _draw, final World _world )
			{
				if( _draw != null && _draw instanceof DrawData )
				{
					if( data.contains( _draw ) == false )
					{
						_draw.setUploadInterface( getTextUpload() ) ;
						data.add( _draw ) ;

						worlds.addDraw( ( DrawData )_draw, ( BasicWorld )_world ) ;
					}
				}
			}

			@Override
			public void addBasicDraw( final Draw _draw, final World _world )
			{
				if( _draw != null && _draw instanceof DrawData )
				{
					if( data.contains( _draw ) == false )
					{
						_draw.setUploadInterface( getBasicUpload() ) ;
						data.add( _draw ) ;

						worlds.addDraw( ( DrawData )_draw, ( BasicWorld )_world ) ;
					}
				}
			}

			@Override
			public void removeDraw( final Draw _draw )
			{
				if( _draw != null && _draw instanceof DrawData )
				{
					data.remove( _draw ) ;
					worlds.removeDraw( ( DrawData )_draw ) ;
				}
			}

			@Override
			public void addCamera( final Camera _camera, final World _world )
			{
				if( _camera != null && _camera instanceof CameraData )
				{
					_camera.setDrawInterface( getCameraDraw() ) ;
					cameras.add( _camera ) ;

					worlds.addCamera( ( CameraData )_camera, ( BasicWorld )_world ) ;
				}
			}

			@Override
			public void removeCamera( final Camera _camera, final World _world )
			{
				if( _camera != null && _camera instanceof CameraData )
				{
					cameras.remove( _camera ) ;
					worlds.removeCamera( ( CameraData )_camera ) ;
				}
			}

			
			@Override
			public void addWorld( final World _world )
			{
				if( _world != null && _world instanceof BasicWorld )
				{
					worlds.add( ( BasicWorld )_world ) ;
				}
			}

			@Override
			public void removeWorld( final World _world )
			{
				if( _world != null && _world instanceof BasicWorld )
				{
					worlds.remove( ( BasicWorld )_world ) ;
				}
			}

			@Override
			public Camera getCamera( final String _id, final World _world )
			{
				return worlds.getCamera( _id, ( BasicWorld )_world ) ;
			}

			@Override
			public World getWorld( final String _id )
			{
				return worlds.getWorld( _id ) ;
			}

			@Override
			public void shutdown()
			{
				for( final Draw draw : data  )
				{
					worlds.removeDraw( ( DrawData )draw ) ;
				}
				data.clear() ;

				for( final Camera camera : cameras  )
				{
					worlds.removeCamera( ( CameraData )camera ) ;
				}
				cameras.clear() ;
			}
		} ;
	}

	@Override
	public void setRenderDimensions( final int _width, final int _height )
	{
		info.setRenderDimensions( _width, _height ) ;
	}

	@Override
	public void setDisplayDimensions( final int _width, final int _height )
	{
		info.setDisplayDimensions( _width, _height ) ;
	}

	@Override
	public void setCameraPosition( final Vector3 _position )
	{
		CameraAssist.amendPosition( CameraAssist.getDefaultCamera(), _position.x, _position.y, _position.z ) ;
	}

	@Override
	public RenderInfo getRenderInfo()
	{
		return info ;
	}

	@Override
	public EventController getEventController()
	{
		return controller ;
	}

	@Override
	public void updateState( final float _dt )
	{
		controller.update() ;
		updateDT = _dt ;
		renderIter = 0 ;
	}

	@Override
	public void draw( final float _dt )
	{
		++renderIter ;
		drawDT = _dt ;
	}

	@Override
	public void sort()
	{
		worlds.sort() ;
	}

	@Override
	public void clear()
	{
		worlds.clear() ;
	}

	protected void calculateInterpolatedPosition( final Vector3 _old, final Vector3 _current, final Vector3 _position )
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
}