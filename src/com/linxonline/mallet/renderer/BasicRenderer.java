package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

public abstract class BasicRenderer<T extends WorldState> implements RenderInterface<T>
{
	private final T worlds ;

	private final EventController controller = new EventController() ;
	private final RenderInfo info = new RenderInfo( new Vector2( 800, 600 ),
													new Vector2( 800, 600 ) ) ;

	protected float drawDT   = 0.0f ;
	protected float updateDT = 0.0f ;
	protected int renderIter = 0 ;

	public BasicRenderer( final T _worlds )
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
	public T getWorldState()
	{
		return worlds ;
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
}