package com.linxonline.mallet.animation ;

import java.util.List ;

import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.util.MalletList ;

/**
	The Animation-system is updated at the specified framerate 
	of the game-state.
	This system allows the developer to update state at the 
	intended framerate, or close enough, it is not intended 
	for anything else other than animations that can have leeway 
	in what is displayed to the screen, for example this should 
	not be used to update the position of an entity.
*/
public class AnimationSystem
{
	private final BufferedList<Runnable> executions = new BufferedList<Runnable>() ;

	private final List<IAnimation> animations = MalletList.<IAnimation>newList() ;

	public AnimationSystem() {}

	public void update( final float _dt )
	{
		updateExecutions() ;
		final int size = animations.size() ;
		for( int i = 0; i < size; ++i )
		{
			final IAnimation animation = animations.get( i ) ;
			animation.update( _dt ) ;
		}
	}

	/**
		Clear all animations from the system.
		Call removed() to inform users they've been 
		removed, most likely called at shutdown.
	*/
	public void clear()
	{
		for( final IAnimation animation : animations )
		{
			animation.removed() ;
		}
		animations.clear() ;
	}

	public AnimationAssist.Assist createAnimationAssist()
	{
		return new AnimationAssist.Assist()
		{
			private final List<IAnimation> animations = AnimationSystem.this.animations ;

			/**
				Add the IAnimation to the buffered-list and during 
				the animation-systems next update add it to the 
				animations list.
			*/
			public IAnimation add( final IAnimation _animation )
			{
				AnimationSystem.this.invokeLater( () ->
				{
					if( _animation != null )
					{
						animations.add( _animation ) ;
						_animation.added() ;
					}
				} ) ;
				return _animation ;
			}

			/**
				Remove the IAnimation on the next update.
				Removing the IAnimation will trigger a called to 
				removed(), which is expected to remove any resources 
				held by the IAnimation.
			*/
			public IAnimation remove( final IAnimation _animation )
			{
				AnimationSystem.this.invokeLater( () ->
				{
					if( _animation != null )
					{
						animations.remove( _animation ) ;
						_animation.removed() ;
					}
				} ) ;
				return _animation ;
			}
		} ;
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

	/**
		Allows the developer to hook into the animation-system,
		this system allows the developer to update content at 
		the intended framerate. 
	*/
	public interface IAnimation
	{
		/**
			Called during the animation systems 
			update cycle.
		*/
		public void update( final float _dt ) ;

		/**
			The animation has been added to the 
			animation-system.
		*/
		public void added() ;

		/**
			The animation has been removed from the 
			animation-system and all resources are expected 
			to be removed.
		*/
		public void removed() ;
	}
}
