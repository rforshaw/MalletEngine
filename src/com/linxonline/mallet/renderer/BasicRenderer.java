package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.renderer.font.* ;

import com.linxonline.mallet.util.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

public abstract class BasicRenderer<D extends DrawData,
									C extends CameraData,
									W extends BasicWorld<D, C>,
									WS extends WorldState<D, C, W>> implements IRender
{
	private final WS worlds ;

	private final EventController controller = new EventController() ;
	private final RenderInfo info = new RenderInfo() ;

	protected float drawDT   = 0.0f ;
	protected float updateDT = 0.0f ;
	protected int renderIter = 0 ;

	public BasicRenderer( final WS _worlds )
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
	public void initAssist()
	{
		FontAssist.setAssist( getFontAssist() ) ;
		TextureAssist.setAssist( getTextureAssist() ) ;

		DrawAssist.setAssist( getDrawAssist() ) ;
		ProgramAssist.setAssist( getProgramAssist() ) ;

		WorldAssist.setAssist( getWorldAssist() ) ;
		CameraAssist.setAssist( getCameraAssist() ) ;
	}

	public abstract FontAssist.Assist getFontAssist() ;
	public abstract TextureAssist.Assist getTextureAssist() ;

	public abstract DrawAssist.Assist getDrawAssist() ;
	public abstract ProgramAssist.Assist getProgramAssist() ;

	public abstract WorldAssist.Assist getWorldAssist() ;
	public abstract CameraAssist.Assist getCameraAssist() ;

	protected DrawDelegate<World, Draw> constructDrawDelegate()
	{
		return new DrawDelegate<World, Draw>()
		{
			private final List<D> data = MalletList.<D>newList() ;

			@Override
			@SuppressWarnings( "unchecked" )
			public void addTextDraw( final Draw _draw )
			{
				addTextDraw( _draw, null ) ;
			}

			@Override
			@SuppressWarnings( "unchecked" )
			public void addBasicDraw( final Draw _draw )
			{
				addBasicDraw( _draw, null ) ;
			}

			@Override
			@SuppressWarnings( "unchecked" )
			public void addTextDraw( final Draw _draw, final World _world )
			{
				final W world = ( W )_world ;
				final D draw = ( D )_draw ;

				if( data.contains( draw ) == false )
				{
					data.add( draw ) ;
					worlds.addDraw( draw, world ) ;
				}
			}

			@Override
			@SuppressWarnings( "unchecked" )
			public void addBasicDraw( final Draw _draw, final World _world )
			{
				final W world = ( W )_world ;
				final D draw = ( D )_draw ;

				if( data.contains( draw ) == false )
				{
					data.add( draw ) ;
					worlds.addDraw( draw, world ) ;
				}
			}

			@Override
			@SuppressWarnings( "unchecked" )
			public void removeDraw( final Draw _draw )
			{
				final D draw = ( D )_draw ;
				if( draw != null )
				{
					data.remove( draw ) ;
					worlds.removeDraw( draw ) ;
				}
			}

			@Override
			@SuppressWarnings( "unchecked" )
			public Camera getCamera( final String _id, final World _world )
			{
				final W world = ( W )_world ;
				return worlds.getCamera( _id, world ) ;
			}

			@Override
			@SuppressWarnings( "unchecked" )
			public World getWorld( final String _id )
			{
				return ( W )worlds.getWorld( _id ) ;
			}

			@Override
			public void shutdown()
			{
				if( data.isEmpty() == false )
				{
					for( final D draw : data  )
					{
						worlds.removeDraw( draw ) ;
					}
					data.clear() ;
				}
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

	public WS getWorldState()
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
