package com.linxonline.mallet.ecs ;

import java.util.List ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.util.BufferedList ;

public class ECSInput implements IECS<ECSInput.Component>
{
	private final static IProcess EMPTY_PROCESS = ( final InputEvent _event ) ->
	{
		return InputEvent.Action.PROPAGATE ;
	} ;
	private final BufferedList<Runnable> executions = new BufferedList<Runnable>() ;

	private final InputState state ;

	public ECSInput( final InputState _input )
	{
		state = _input ;
	}

	@Override
	public Component create( final ECSEntity _parent )
	{
		return create( _parent, EMPTY_PROCESS ) ;
	}

	public Component create( final ECSEntity _parent, final IProcess _process )
	{
		final Component component = new Component( _parent, _process ) ;
		invokeLater( () ->
		{
			state.addInputHandler( component ) ;
		} ) ;
		return component ;
	}

	@Override
	public void remove( final Component _component )
	{
		invokeLater( () ->
		{
			state.removeInputHandler( _component ) ;
		} ) ;
	}

	@Override
	public void update( final double _dt )
	{
		updateExecutions() ;
	}

	private void invokeLater( final Runnable _run )
	{
		if( _run != null )
		{
			executions.add( _run ) ;
		}
	}

	private void updateExecutions()
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

	public interface IProcess
	{
		public InputEvent.Action process( final InputEvent _event ) ;
	}

	public final class Component extends ECSEntity.Component implements IInputHandler
	{
		private final IProcess process ;
		private boolean enable = true ;

		public Component( final ECSEntity _parent, final IProcess _process )
		{
			_parent.super() ;
			process = _process ;
		}

		public void enable()
		{
			enable = true ;
		}

		public void disable()
		{
			enable = false ;
		}

		@Override
		public InputEvent.Action passInputEvent( final InputEvent _event )
		{
			if( enable == false )
			{
				return InputEvent.Action.PROPAGATE ;
			}

			return process.process( _event ) ;
		}
	}
}
