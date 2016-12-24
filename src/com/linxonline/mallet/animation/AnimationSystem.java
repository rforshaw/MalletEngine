package com.linxonline.mallet.animation ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.EventType ;
import com.linxonline.mallet.event.EventProcessor ;
import com.linxonline.mallet.event.EventController ;

import com.linxonline.mallet.event.AddEventInterface ;

import com.linxonline.mallet.util.SystemRoot ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.caches.ObjectCache ;

import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.World ;
import com.linxonline.mallet.renderer.Draw ;

public class AnimationSystem
{
	private final List<AnimData> toAddAnim    = MalletList.<AnimData>newList() ;
	private final List<AnimData> toRemoveAnim = MalletList.<AnimData>newList() ;
	private final List<AnimData> animations   = MalletList.<AnimData>newList() ;

	private final SpriteManager spriteManager = new SpriteManager() ;

	private final EventController controller = new EventController() ;
	protected DrawDelegate drawDelegate = null ;

	public AnimationSystem()
	{
		controller.addEventProcessor( new EventProcessor<AnimationDelegateCallback>( "ANIMATION_DELEGATE", "ANIMATION_DELEGATE" )
		{
			public void processEvent( final Event<AnimationDelegateCallback> _event )
			{
				final AnimationDelegateCallback callback = _event.getVariable() ;
				callback.callback( constructAnimationDelegate() ) ;
			}
		} ) ;

		controller.addEventProcessor( new EventProcessor( "ANIMATION_CLEAN", "ANIMATION_CLEAN" )
		{
			public void processEvent( final Event _event )
			{
				spriteManager.clean() ;
			}
		} ) ;

		/**
			Animation System requires a handle to the Rendering System.
			It determines when an Animation is displayed or not.
		*/
		controller.passEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
		{
			public void callback( final DrawDelegate _delegate )
			{
				drawDelegate = _delegate ;
			}
		} ) ) ;
	}

	public void update( final float _dt )
	{
		controller.update() ;
		if( drawDelegate != null )
		{
			if( toAddAnim.isEmpty() == false )
			{
				for( final AnimData anim : toAddAnim )
				{
					final Sprite sprite = ( Sprite )spriteManager.get( anim.getFile() ) ;
					if( sprite != null )
					{
						anim.setSprite( sprite ) ;
						animations.add( anim ) ;
						drawDelegate.addBasicDraw( anim.getDraw(), anim.getWorld() ) ;
						anim.play() ;
					}
				}
				toAddAnim.clear() ;
			}

			if( toRemoveAnim.isEmpty() == false )
			{
				for( final AnimData anim : toRemoveAnim )
				{
					anim.stop() ;
					animations.remove( anim ) ;
					drawDelegate.removeDraw( anim.getDraw() ) ;
					anim.unregister() ;
				}
				toRemoveAnim.clear() ;
			}
		}

		final int size = animations.size() ;
		for( int i = 0; i < size; i++ )
		{
			animations.get( i ).update( _dt ) ;
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

		for( final AnimData anim : animations )
		{
			drawDelegate.removeDraw( anim.getDraw() ) ;
			anim.unregister() ;
		}
		animations.clear() ;
	}

	public String getName()
	{
		return "Animation System" ;
	}

	protected void removeAnimData( final List<AnimData> _data )
	{
		for( final AnimData anim : _data )
		{
			animations.remove( anim ) ;
			drawDelegate.removeDraw( anim.getDraw() ) ;
			anim.unregister() ;
		}
		_data.clear() ;
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
					if( data.contains( _animation ) == false )
					{
						( ( AnimData )_animation ).setWorld( _world ) ;
						data.add( ( AnimData )_animation ) ;
						toAddAnim.add( ( AnimData )_animation ) ;
					}
				}
			}

			@Override
			public void removeAnimation( final Anim _animation )
			{
				if( _animation != null && _animation instanceof AnimData )
				{
					data.remove( ( AnimData )_animation ) ;
					toRemoveAnim.add( ( AnimData )_animation ) ;
				}
			}

			@Override
			public void start() {}

			@Override
			public void shutdown()
			{
				for( final AnimData anim : data  )
				{
					toRemoveAnim.add( anim ) ;
				}
				data.clear() ;
			}
		} ;
	}
}
