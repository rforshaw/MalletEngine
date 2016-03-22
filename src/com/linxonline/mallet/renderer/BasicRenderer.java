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
	}

	@Override
	public abstract void shutdown() ;

	@Override
	public abstract void initAssist() ;

	public abstract DrawData.DrawInterface getBasicDraw() ;
	public abstract DrawData.DrawInterface getTextDraw() ;

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
		info.setCameraPosition( _position ) ;
	}

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

	public void passEvent( final Event _event ) {}

	public void processEvent( final Event _event ) {}

	@Override
	public void reset() {}

	@Override
	public ArrayList<EventType> getWantedEventTypes()
	{
		final ArrayList<EventType> types = new ArrayList<EventType>() ;
		return types ;
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