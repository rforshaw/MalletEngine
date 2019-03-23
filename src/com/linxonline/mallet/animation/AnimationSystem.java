package com.linxonline.mallet.animation ;

import java.util.List ;
import java.util.Set ;
import java.util.HashSet ;

import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.EventType ;
import com.linxonline.mallet.event.EventController ;
import com.linxonline.mallet.event.IAddEvent ;

import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.Logger ;
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
		controller.addProcessor( "ANIMATION_DELEGATE", ( final AnimationDelegateCallback _callback ) ->
		{
			_callback.callback( constructAnimationDelegate() ) ;
		} ) ;

		controller.addProcessor( "ANIMATION_CLEAN", new EventController.IProcessor<Object>()
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
		} ) ;

		/**
			Animation System requires a handle to the Rendering System.
			It determines when an Animation is displayed or not.
		*/
		controller.passEvent( DrawAssist.constructDrawDelegate( ( final DrawDelegate _delegate ) -> 
		{
			drawDelegate = _delegate ;
		} ) ) ;
	}

	public void update( final float _dt )
	{
		controller.update() ;
		if( drawDelegate != null )
		{
			if( toRemoveAnim.isEmpty() == false )
			{
				final int size = toRemoveAnim.size() ;
				for( int i = 0; i < size; i++ )
				{
					final AnimData anim = toRemoveAnim.get( i ) ;
					anim.stop() ;
					animations.remove( anim ) ;
					drawDelegate.removeDraw( anim.getDraw() ) ;
					anim.removeCallback() ;
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
						drawDelegate.addBasicDraw( anim.getDraw(), anim.getWorld() ) ;
						anim.play() ;
					}
				}
				toAddAnim.clear() ;
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

		final int size = animations.size() ;
		for( int i = 0; i < size; i++ )
		{
			final AnimData anim = animations.get( i ) ;
			drawDelegate.removeDraw( anim.getDraw() ) ;
			anim.removeCallback() ;
		}
		animations.clear() ;
	}

	public String getName()
	{
		return "Animation System" ;
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
