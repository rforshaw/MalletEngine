package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.util.* ;
import com.linxonline.mallet.maths.* ;

public abstract class BasicRenderer implements IRender
{
	private final BufferedList<Invoker> executions = new BufferedList<Invoker>() ;

	private final World world = new World( "DEFAULT" ) ;
	private final Camera camera = new Camera( "MAIN" ) ;

	private float drawDT   = 1.0f ;
	private float updateDT = 1.0f ;

	public BasicRenderer()
	{
		world.addCamera( camera ) ;
	}

	@Override
	public void start() {}

	/**
		Call the passed in runnable the next time the
		renderer has got focus.
	*/
	public void invokeLater( final Runnable _run )
	{
		if( _run == null )
		{
			return ;
		}

		executions.add( new Invoker( _run ) ) ;
	}

	/**
		Call the passed in runnable the next time the
		renderer has got focus.
		Use the passed in _anchor and _operation to remove
		previous invocations that are identical to the current
		call, this ensures that something that needs only be executed
		once is not processed multiple times in one draw call.
	*/
	public void invokeLater( final Object _anchor, final int _operation, final Runnable _run )
	{
		if( _run == null )
		{
			return ;
		}

		final Invoker invoker = new Invoker( _anchor, _operation, _run ) ;
		// Remove invokers that match the signature of the
		// newly created invoker. 
		executions.removeAll( invoker ) ;
		executions.add( invoker ) ;
	}

	protected void updateExecutions()
	{
		executions.update() ;
		final List<Invoker> runnables = executions.getCurrentData() ;
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

	public float getFrameDeltaTime()
	{
		return drawDT ;
	}

	@Override
	public void updateState( final float _dt )
	{
		updateDT = _dt ;
	}

	@Override
	public void draw( final float _dt )
	{
		drawDT = _dt ;
	}

	private static final class Invoker
	{
		private final Object anchor ;
		private final int operation ;
		private final Runnable run ;

		public Invoker( final Runnable _run )
		{
			this( null, -1, _run ) ;
		}

		public Invoker( final Object _anchor, final int _operation, final Runnable _run )
		{
			anchor = _anchor ;
			operation = _operation ;
			run = _run ;
		}

		@Override
		public boolean equals( final Object _obj )
		{
			if( _obj == this )
			{
				return true ;
			}

			if( !( _obj instanceof Invoker ) )
			{
				return false ;
			}

			final Invoker invoker = ( Invoker )_obj ;
			if( invoker.anchor == null )
			{
				return false ;
			}

			if( invoker.operation <= -1 )
			{
				return false ;
			}

			return anchor == invoker.anchor && operation == invoker.operation ;
		}

		public void run()
		{
			run.run() ;
		}
	}
}
