package com.linxonline.mallet.core.web ;

import java.util.List ;

import org.teavm.jso.browser.Window ;
import org.teavm.jso.browser.AnimationFrameCallback ;

import com.linxonline.mallet.core.* ;
import com.linxonline.mallet.core.statemachine.* ;
import com.linxonline.mallet.util.time.* ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Debounce ;

public class WebGameSystem implements IGameSystem
{
	private final StateUpdater state = new StateUpdater() ;
	private final List<IUpdate> updates = MalletList.<IUpdate>newList() ;

	private boolean running = false ;
	private ISystem system ;

	public WebGameSystem()
	{
		addUpdate( state ) ;
		// For debounce to work it must be tied into the
		// main-loop, this allows it to track when the 
		// runnable can be triggered.
		addUpdate( new Debounce() ) ;
	}

	public void setMainSystem( final ISystem _system )
	{
		system = _system ;
	}

	@Override
	public void addUpdate( final IUpdate _update )
	{
		updates.add( _update ) ;
	}

	@Override
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
				for( final IUpdate update : updates )
				{
					update.update( dt ) ;
				}

				if( running == false )
				{
					state.pause() ;
					return ;
				}

				Window.requestAnimationFrame( this ) ;
			}
		} ;

		state.resume() ;
		Window.requestAnimationFrame( callback ) ;
	}

	@Override
	public void stopSystem()
	{
		running = false ;
	}

	@Override
	public final void addGameState( final GameState _state )
	{
		assert _state != null ;
		_state.setSystem( system ) ;
		state.addState( _state ) ;
	}

	/**
		Specify the Game State that will be used if another, 
		State is not available when called.
	**/
	@Override
	public final void setDefaultGameState( final String _name )
	{
		state.setDefaultState( _name ) ;
	}

	public final static class StateUpdater implements IUpdate
	{
		private final StateMachine machine = new StateMachine() ;

		public void resume()
		{
			machine.resume() ;
		}

		public void pause()
		{
			machine.pause() ;
		}

		public void addState( final GameState _state )
		{
			machine.addState( _state ) ;
		}

		public void setDefaultState( final String _name )
		{
			machine.setDefaultState( _name ) ;
		}

		@Override
		public void update( final double _dt )
		{
			machine.update( _dt ) ;		// Update Game State
		}
	}
}
