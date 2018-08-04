package com.linxonline.mallet.core.web ;

import org.teavm.jso.browser.Window ;
import org.teavm.jso.browser.AnimationFrameCallback ;

import com.linxonline.mallet.core.* ;
import com.linxonline.mallet.core.statemachine.* ;
import com.linxonline.mallet.util.time.* ;

public class WebGameSystem implements IGameSystem
{
	private final StateMachine stateMachine = new StateMachine() ;
	private boolean running = false ;
	private ISystem system ;

	public WebGameSystem() {}

	public void setMainSystem( final ISystem _system )
	{
		system = _system ;
	}

	public void runSystem()
	{
		running = true ;

		final AnimationFrameCallback callback = new AnimationFrameCallback()
		{
			private double dt = ElapsedTimer.getElapsedTimeInNanoSeconds() ;

			@Override
			public void onAnimationFrame( final double _timestamp )
			{
				dt = ElapsedTimer.getElapsedTimeInNanoSeconds() ;
				stateMachine.update( dt ) ;						// Update Game State
				if( running == true )
				{
					System.out.println( "Render: " + dt ) ;
					Window.requestAnimationFrame( this ) ;
				}
				else
				{
					stateMachine.pause() ;
				}
			}
		} ;

		stateMachine.resume() ;
		Window.requestAnimationFrame( callback ) ;
	}

	public void stopSystem()
	{
		running = false ;
	}

	public final void addGameState( final GameState _state )
	{
		assert _state != null ;
		_state.setSystem( system ) ;
		stateMachine.addState( _state ) ;
	}

	/**
		Specify the Game State that will be used if another, 
		State is not available when called.
	**/
	public final void setDefaultGameState( final String _name )
	{
		stateMachine.setDefaultState( _name ) ;
	}
}
