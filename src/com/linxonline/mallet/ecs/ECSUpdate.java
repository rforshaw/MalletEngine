package com.linxonline.mallet.ecs ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.util.Parallel ;

/**
	A generalised update component-system, sometimes building your
	own component-system is too much for a component that is not
	very complex, or doesn't have unusual requirements.

	If the above is the case for you, then use ECSUpdate. Define the
	component you want ECSUpdate to create by implementing ECSUpdate.ICreate.

	Make sure your component implements ECSUpdate.Component.

	T - represents the component that ECSUpdate will manage.
	u - represents the data passed to the created component.
*/
public final class ECSUpdate<T extends ECSUpdate.Component, U> implements IECS<T>
{
	private final BufferedList<Runnable> executions = new BufferedList<Runnable>() ;

	private final List<T> components = MalletList.<T>newList() ;
	private final ICreate<T, U> create ;

	private final ComponentUpdater updater = new ComponentUpdater() ;

	public ECSUpdate()
	{
		this( new ICreate<T, U>()
		{
			@Override
			public T create( final ECSEntity _parent, final U _data )
			{
				throw new RuntimeException( "Requires valid ICreate to use this path." ) ;
			}
		} ) ;
	}

	public ECSUpdate( final ICreate<T, U> _create )
	{
		create = _create ;
	}

	@Override
	public T create( final ECSEntity _parent )
	{
		return create( _parent, create, null ) ;
	}

	public T create( final ECSEntity _parent, final U _data )
	{
		return create( _parent, create, _data ) ;
	}

	public T create( final ECSEntity _parent, final ICreate<T, U> _create )
	{
		return create( _parent, _create, null ) ;
	}

	public T create( final ECSEntity _parent, final ICreate<T, U> _create, final U _data )
	{
		final T component = _create.create( _parent, _data ) ;
		invokeLater( () ->
		{
			components.add( component ) ;
		} ) ;
		return component ;
	}

	@Override
	public void remove( final T _component )
	{
		invokeLater( () ->
		{
			if( components.remove( _component ) )
			{
				_component.shutdown() ;
			}
		} ) ;
	}

	@Override
	public void update( final double _dt )
	{
		updateExecutions() ;

		updater.setDeltaTime( ( float )_dt ) ;
		Parallel.forEach( components, updater ) ;
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

	public interface ICreate<T, U>
	{
		public T create( final ECSEntity _parent, final U _data ) ;
	}

	public static abstract class Component extends ECSEntity.Component
	{
		public Component( final ECSEntity _parent )
		{
			_parent.super() ;
		}

		public abstract void update( final float _dt ) ;
		public abstract void shutdown() ;
	}

	private final class ComponentUpdater implements Parallel.IRangeRun<T>
	{
		private float delta = 0.0f ;

		public ComponentUpdater() {}

		public void setDeltaTime( final float _dt )
		{
			delta = _dt ;
		}

		@Override
		public void run( final int _index, final T _component )
		{
			_component.update( delta ) ;
		}
	}
}
