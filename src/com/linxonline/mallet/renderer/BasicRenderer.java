package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.core.GlobalConfig ;

import com.linxonline.mallet.renderer.font.* ;

import com.linxonline.mallet.util.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

public abstract class BasicRenderer implements IRender
{
	private final EventController controller = new EventController() ;
	private final List<Runnable> executions = MalletList.<Runnable>newList() ;

	private float drawDT   = 1.0f ;
	private float updateDT = 1.0f ;
	private int renderIter = 0 ;

	public BasicRenderer() {}

	@Override
	public void start()
	{
		controller.reset() ;
		controller.addProcessor( "DRAW_DELEGATE", ( final DrawDelegateCallback _delegate ) ->
		{
			_delegate.callback( constructDrawDelegate() ) ;
		} ) ;

		controller.addProcessor( "DRAW_CLEAN", ( final DrawDelegateCallback _delegate ) ->
		{
			clean() ;
		} ) ;
	}

	protected abstract DrawDelegate constructDrawDelegate() ;

	public void invokeLater( final Runnable _run )
	{
		if( _run != null )
		{
			executions.add( _run ) ;
		}
	}

	protected void updateExecutions()
	{
		if( executions.isEmpty() == false )
		{
			final int size = executions.size() ;
			for( int i = 0; i < size; i++ )
			{
				executions.get( i ).run() ;
			}
			executions.clear() ;
		}
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
