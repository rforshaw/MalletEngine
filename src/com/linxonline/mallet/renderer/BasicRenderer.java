package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

public abstract class BasicRenderer implements RenderInterface
{
	protected final DrawState state = new DrawState() ;
	protected final EventController controller = new EventController() ;
	protected final RenderInfo info = new RenderInfo( new Vector2( 800, 600 ),
													  new Vector2( 800, 600 ),
													  new Vector3( 400, 300, 0 ) ) ;

	protected final BasicCamera oldCamera = new BasicCamera() ;
	protected final BasicCamera camera = new BasicCamera() ;

	protected Vector3 oldCameraPosition = new Vector3() ;
	protected Vector3 cameraPosition = null ;
	protected final Vector3 cameraScale = new Vector3( 1, 1, 1 ) ;

	protected float drawDT   = 0.0f ;
	protected float updateDT = 0.0f ;
	protected int renderIter = 0 ;

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

		state.setRemoveDelegate( constructRemoveDelegate() ) ;
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

	public abstract DrawData.DrawInterface getBasicDraw() ;
	public abstract DrawData.DrawInterface getTextDraw() ;

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

			public void addTextDraw( final Draw _draw )
			{
				if( _draw != null && _draw instanceof DrawData )
				{
					if( data.contains( _draw ) == false )
					{
						_draw.setDrawInterface( getTextDraw() ) ;
						data.add( _draw ) ;
						synchronized( state )
						{
							state.add( ( DrawData )_draw ) ;
						}
					}
				}
			}

			public void addBasicDraw( final Draw _draw )
			{
				if( _draw != null && _draw instanceof DrawData )
				{
					if( data.contains( _draw ) == false )
					{
						_draw.setDrawInterface( getBasicDraw() ) ;
						data.add( _draw ) ;
						synchronized( state )
						{
							state.add( ( DrawData )_draw ) ;
						}
					}
				}
			}

			@Override
			public void removeDraw( final Draw _draw )
			{
				if( _draw != null && _draw instanceof DrawData )
				{
					data.remove( _draw ) ;
					synchronized( state )
					{
						state.remove( ( DrawData )_draw ) ;
					}
				}
			}

			@Override
			public void shutdown()
			{
				synchronized( state )
				{
					for( final Draw draw : data  )
					{
						state.remove( ( DrawData )draw ) ;
					}
				}
				data.clear() ;
			}
		} ;
	}

	@Override
	public void setRenderDimensions( final int _width, final int _height )
	{
		info.setRenderDimensions( new Vector2( _width, _height ) ) ;
	}

	@Override
	public void setDisplayDimensions( final int _width, final int _height )
	{
		info.setDisplayDimensions( new Vector2( _width, _height ) ) ;
	}

	@Override
	public void setCameraPosition( final Vector3 _position )
	{
		CameraAssist.amendPosition( CameraAssist.getCamera(), _position.x, _position.y, _position.z ) ;
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
		state.draw( ( int )( updateDT / drawDT ), renderIter ) ;
	}

	@Override
	public void sort()
	{
		state.sort() ;
	}

	@Override
	public void clear()
	{
		state.clear() ;
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

	protected class BasicCamera implements Camera
	{
		private final Matrix4 projection = new Matrix4() ;		// Combined Model View and Projection Matrix
		private final Vector3 position = new Vector3() ;
		private final Vector3 rotation = new Vector3() ;
		private final Vector3 scale = new Vector3( 1, 1, 1 ) ;

		public Matrix4 getProjection()
		{
			return projection ;
		}
		
		public Vector3 getPosition()
		{
			return position ;
		}

		public Vector3 getRotation()
		{
			return rotation ;
		}

		public Vector3 getScale()
		{
			return scale ;
		}

		public void setPosition( final float _x, final float _y, final float _z )
		{
			position.setXYZ( _x, _y, _z ) ;
		}
		
		public void setRotation( final float _x, final float _y, final float _z )
		{
			rotation.setXYZ( _x, _y, _z ) ;
		}
		
		public void setScale( final float _x, final float _y, final float _z )
		{
			scale.setXYZ( _x, _y, _z ) ;
		}
	}
}