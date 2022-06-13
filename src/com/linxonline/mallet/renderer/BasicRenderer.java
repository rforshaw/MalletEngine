package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.core.GlobalConfig ;

import com.linxonline.mallet.util.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

public abstract class BasicRenderer implements IRender
{
	private final EventController controller = new EventController() ;
	private final BufferedList<Runnable> executions = new BufferedList<Runnable>() ;

	private final World world = new World( "DEFAULT" ) ;
	private final Camera camera = new Camera( "MAIN" ) ;

	private float drawDT   = 1.0f ;
	private float updateDT = 1.0f ;
	private int renderIter = 0 ;

	public BasicRenderer()
	{
		world.addCameras( camera ) ;
		controller.addProcessor( "DRAW_CLEAN", ( final Object _obj ) ->
		{
			clean() ;
		} ) ;
	}

	@Override
	public void start() {}

	public void invokeLater( final Runnable _run )
	{
		if( _run != null )
		{
			executions.add( _run ) ;
		}
	}

	protected void updateExecutions()
	{
		executions.update() ;
		final List<Runnable> runnables = executions.getCurrentData() ;
		if( runnables.isEmpty() )
		{
			return ;
		}

		final int size = runnables.size() ;
		for( int i = 0; i < size; i++ )
		{
			runnables.get( i ).run() ;
		}
		runnables.clear() ;
	}

	public World getDefaultWorld()
	{
		return world ;
	}

	public Camera getDefaultCamera()
	{
		return camera ;
	}

	public float getUpdateDeltaTime()
	{
		return updateDT ;
	}
	
	public int getFrameIteration()
	{
		return renderIter ;
	}

	public float getFrameDeltaTime()
	{
		return drawDT ;
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
}
