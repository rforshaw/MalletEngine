package com.linxonline.mallet.animation ;

import java.util.List ;
import java.util.Set ;
import java.util.HashSet ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Tuple ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.EventController ;

import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.renderer.Interpolation ;
import com.linxonline.mallet.renderer.DrawUpdater ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.WorldAssist ;
import com.linxonline.mallet.renderer.World ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Shape ;

public class AnimationSystem
{
	private final List<AnimData> toAddAnim    = MalletList.<AnimData>newList() ;
	private final List<AnimData> toRemoveAnim = MalletList.<AnimData>newList() ;
	private final List<AnimData> animations   = MalletList.<AnimData>newList() ;

	private final SpriteManager spriteManager = new SpriteManager() ;

	private final EventController controller ;

	public AnimationSystem()
	{
		controller = new EventController( MalletList.toArray( 
			Tuple.<String, EventController.IProcessor<?>>build( "ANIMATION_DELEGATE", ( final AnimationDelegateCallback _callback ) ->
			{
				_callback.callback( constructAnimationDelegate() ) ;
			} ),
			Tuple.<String, EventController.IProcessor<?>>build( "ANIMATION_CLEAN", new EventController.IProcessor<Object>()
			{
				@Override
				public void process( final Object _null )
				{
					final Set<String> activeKeys = new HashSet<String>() ;
					getActiveKeys( activeKeys, toAddAnim ) ;
					getActiveKeys( activeKeys, animations ) ;

					spriteManager.clean( activeKeys ) ;
				}

				private void getActiveKeys( final Set<String> _keys, final List<AnimData> _animations )
				{
					final int size = _animations.size() ;
					for( int i = 0; i < size; i++ )
					{
						final AnimData anim = _animations.get( i ) ;
						_keys.add( anim.getFile() ) ;
					}
				}
			} ) )
		) ;
	}

	public void update( final float _dt )
	{
		controller.update() ;
		if( toRemoveAnim.isEmpty() == false )
		{
			final int size = toRemoveAnim.size() ;
			for( int i = 0; i < size; i++ )
			{
				final AnimData anim = toRemoveAnim.get( i ) ;
				anim.stop() ;
				animations.remove( anim ) ;

				final World world = anim.getWorld() ;
				final Program program = anim.getProgram() ;
				final Draw draw = anim.getDraw() ;
				final Shape shape = draw.getShape() ;
				final int order = anim.getOrder() ;

				DrawUpdater updater = DrawUpdater.get( world, program, shape, false, order ) ;
				if( updater != null )
				{
					updater.removeDynamics( draw ) ;
					anim.removeCallback() ;
				}
			}
			toRemoveAnim.clear() ;
		}

		if( toAddAnim.isEmpty() == false )
		{
			final int size = toAddAnim.size() ;
			for( int i = 0; i < size; i++ )
			{
				final AnimData anim = toAddAnim.get( i ) ;
				final Sprite sprite = ( Sprite )spriteManager.get( anim.getFile() ) ;
				if( sprite != null )
				{
					anim.setSprite( sprite ) ;
					animations.add( anim ) ;
					
					final World world = anim.getWorld() ;
					final Program program = anim.getProgram() ;
					final Draw draw = anim.getDraw() ;
					final Shape shape = draw.getShape() ;
					final int order = anim.getOrder() ;

					final DrawUpdater updater = DrawUpdater.getOrCreate( world, program, shape, false, order ) ;
					updater.addDynamics( draw ) ;

					anim.setUpdater( updater, updater.getDrawBuffer() ) ;
					anim.play() ;
				}
			}
			toAddAnim.clear() ;
		}

		final int size = animations.size() ;
		for( int i = 0; i < size; i++ )
		{
			final AnimData anim = animations.get( i ) ;
			if( anim.update( _dt ) == true )
			{
				final World world = anim.getWorld() ;
				final Program program = anim.getProgram() ;
				final Draw draw = anim.getDraw() ;
				final Shape shape = draw.getShape() ;
				final int order = anim.getOrder() ;

				final DrawUpdater updater = DrawUpdater.get( world, program, shape, false, order ) ;

				updater.makeDirty() ;
				anim.setUpdater( updater, updater.getDrawBuffer() ) ;
			}
		}
	}

	public EventController getEventController()
	{
		return controller ;
	}

	public void clear()
	{
		toAddAnim.clear() ;			// Never added not hooked in
		toRemoveAnim.clear() ;		// Will be removed from animations anyway

		final int size = animations.size() ;
		for( int i = 0; i < size; i++ )
		{
			final AnimData anim = animations.get( i ) ;
			final World world = anim.getWorld() ;
			final Program program = anim.getProgram() ;
			final Draw draw = anim.getDraw() ;
			final Shape shape = draw.getShape() ;
			final int order = anim.getOrder() ;

			final DrawUpdater updater = DrawUpdater.getOrCreate( world, program, shape, true, order ) ;
			updater.removeDynamics( draw ) ;

			anim.removeCallback() ;
		}
		animations.clear() ;
	}

	protected AnimationDelegate constructAnimationDelegate()
	{
		return new AnimationDelegate()
		{
			private final List<AnimData> data = MalletList.<AnimData>newList() ;

			@Override
			public void addAnimation( final Anim _animation, final World _world )
			{
				if( _animation != null && _animation instanceof AnimData )
				{
					final AnimData anim = ( AnimData )_animation ;
					if( data.contains( anim ) == false )
					{
						anim.setWorld( _world ) ;
						data.add( anim ) ;
						toAddAnim.add( anim ) ;

						final Draw draw = anim.getDraw() ;
						draw.update( Interpolation.NONE, 0, 0 ) ;
					}
				}
			}

			@Override
			public void removeAnimation( final Anim _animation )
			{
				if( _animation != null && _animation instanceof AnimData )
				{
					final AnimData anim = ( AnimData )_animation ;
					data.remove( anim ) ;
					toRemoveAnim.add( anim ) ;
				}
			}

			@Override
			public void start() {}

			@Override
			public void shutdown()
			{
				final int size = data.size() ;
				for( int i = 0; i < size; i++ )
				{
					final AnimData anim = data.get( i ) ;
					toRemoveAnim.add( anim ) ;
				}
				data.clear() ;
			}
		} ;
	}
}
