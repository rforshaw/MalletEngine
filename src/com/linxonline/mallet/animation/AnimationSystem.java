package com.linxonline.mallet.animation ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.AddEventInterface ;
import com.linxonline.mallet.util.SystemRoot ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.caches.ObjectCache ;
import com.linxonline.mallet.resources.texture.SpriteManager ;
import com.linxonline.mallet.resources.texture.Sprite ;
import com.linxonline.mallet.renderer.DrawFactory ;

public class AnimationSystem extends SystemRoot<Animation>
{
	private final static String[] EVENT_TYPES = { "ANIMATION" } ;
	private final static ObjectCache<Animation> animationCache = new ObjectCache<Animation>( Animation.class ) ; 
	private final static SpriteManager spriteManager = new SpriteManager() ;

	protected int numID = 0 ;

	public AnimationSystem( final AddEventInterface _eventSystem )
	{
		assert _eventSystem != null ;
		eventSystem = _eventSystem ;
	}

	@Override
	protected void updateSource( final Animation _source, final float _dt )
	{
		_source.update( _dt ) ;
	}

	/**
		Unregister any resources this Animation may have used.
	*/
	@Override
	protected void destroySource( final Animation _source )
	{
		_source.destroy() ;
		animationCache.reclaim( _source ) ;						// Return the animation object back to the cache
	}

	@Override
	protected void useEvent( final Event<?> _event )
	{
		final Settings anim = ( Settings )_event.getVariable() ;
		final int type = anim.getInteger( "REQUEST_TYPE", -1 ) ;

		switch( type )
		{
			case AnimRequestType.CREATE_ANIMATION :
			{
				createAnimation( anim ) ;
				break ;
			}
			case AnimRequestType.MODIFY_EXISTING_ANIMATION :
			{
				final Animation animation = getSource( anim.getInteger( "ID", -1 ) ) ;
				if( animation != null )
				{
					modifyAnimation( anim, animation ) ;
				}
				break ;
			}
			case AnimRequestType.REMOVE_ANIMATION :
			{
				final int id = anim.getInteger( "ID", -1 ) ;
				//Logger.println( "AnimationSystem - Remove Anim Request: " + id, Logger.Verbosity.MINOR ) ;
				final Animation animation = getSource( id ) ;
				if( animation != null )
				{
					//Logger.println( "AnimationSystem - Remove Anim: " + id, Logger.Verbosity.MINOR ) ;
					passEvent( DrawFactory.removeDraw( animation.renderID ) ) ;
					removeSources.add( new RemoveSource( id, animation ) ) ;
				}
				break ;
			}
			case AnimRequestType.GARBAGE_COLLECT_ANIMATION : spriteManager.clean() ; break ;
		}
	}

	protected void createAnimation( final Settings _anim )
	{
		final String file = _anim.getString( "ANIM_FILE", null ) ;
		if( file != null )
		{
			final Event event = _anim.getObject( "RENDER_EVENT", null ) ;
			final Animation anim = animationCache.get() ;			// Get an Animation object from the cache
			if( anim != null )
			{
				//Logger.println( "AnimationSystem - Create Anim: " + anim.id, Logger.Verbosity.MINOR ) ;
				anim.setAnimation( numID++, event, ( Sprite )spriteManager.get( file ) ) ;
				DrawFactory.insertIDCallback( event, anim ) ;
				passEvent( event ) ;
				addCallbackToAnimation( anim, _anim ) ;
				storeSource( anim, anim.id ) ;
				anim.play() ;						// Assumed that animation will want to be played immediately.
			}
		}
	}

	protected void modifyAnimation( final Settings _settings, final Animation _animation )
	{
		final int type = _settings.getInteger( "MODIFY_ANIMATION", -1 ) ;
		switch( type )
		{
			case ModifyAnimation.PLAY  : _animation.play() ;  break ;
			case ModifyAnimation.STOP  : _animation.stop() ;  break ;
			case ModifyAnimation.PAUSE : _animation.pause() ; break ;
		}
	}

	/**
		Pass the ActiveSound ID to the IDInterface provided.
		Currently called when ActiveSound is created
	**/
	protected void addCallbackToAnimation( final Animation _animation, final Settings _anim )
	{
		final SourceCallback callback = _anim.getObject( "CALLBACK", null ) ;
		if( callback != null )
		{
			_animation.addCallback( callback ) ;
		}
	}

	@Override
	public String getName()
	{
		return "Animation System" ;
	}

	@Override
	public String[] getWantedEventTypes()
	{
		return EVENT_TYPES ;
	}
}