package com.linxonline.mallet.animation ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.EventUpdater ;
import com.linxonline.mallet.event.AddEventInterface ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.resources.SpriteManager ;
import com.linxonline.mallet.resources.texture.Sprite ;

public class AnimationSystem extends EventUpdater
{
	private final static String[] EVENT_TYPES = { "ANIMATION" } ;

	private final static SpriteManager spriteManager = new SpriteManager() ;

	private final AddEventInterface eventSystem ;
	private final HashMap<Integer, Animation> animations = new HashMap<Integer, Animation>() ;
	private final ArrayList<Animation> activeAnimations = new ArrayList<Animation>() ;
	private final ArrayList<Animation> removeAnimations = new ArrayList<Animation>() ;

	protected int numID = 0 ;

	public AnimationSystem( final AddEventInterface _eventSystem )
	{
		eventSystem = _eventSystem ;
	}

	public void update( final float _dt )
	{
		updateEvents() ;
		updateAnimations( _dt ) ;
		removeAnimations() ;
	}

	protected void updateAnimations( final float _dt )
	{
		final int size = activeAnimations.size() ;
		Animation anim = null ;

		for( int i = 0; i < size; ++i )
		{
			anim = activeAnimations.get( i ) ;
			anim.update( _dt ) ;
		}
	}

	protected void removeAnimations()
	{
		for( final Animation remove : removeAnimations )
		{
			remove.destroy() ;
			activeAnimations.remove( remove ) ;
		}

		removeAnimations.clear() ;
	}

	protected void useEvent( final Event _event )
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
				// modifyAnimation( anim ) ;
				break ;
			}
		}
	}

	protected void createAnimation( final Settings _anim )
	{
		final String file = _anim.getString( "ANIM_FILE", null ) ;
		if( file != null )
		{
			final Event event = _anim.getObject( "RENDER_EVENT", Event.class, null ) ;
			final Animation anim = new Animation( numID++, event, ( Sprite )spriteManager.get( file ) ) ;
			if( anim != null )
			{
				passEvent( event ) ;
				passIDToCallback( anim.id, _anim ) ;
				storeAnimation( anim ) ;
			}
		}
	}

	protected void storeAnimation( final Animation _anim )
	{
		activeAnimations.add( _anim ) ;
		animations.put( _anim.id, _anim ) ;
	}

	/**
		Pass the ActiveSound ID to the IDInterface provided.
		Currently called when ActiveSound is created
	**/
	protected void passIDToCallback( final int _id, final Settings _anim )
	{
		final IDInterface idInterface = _anim.getObject( "ID_REQUEST", IDInterface.class, null ) ;
		if( idInterface != null )
		{
			idInterface.recievedID( _id ) ;
		}
	}

	@Override
	public final void passEvent( final Event _event )
	{
		// Possibly null, but if it is we'll want to know.
		eventSystem.addEvent( _event ) ;
	}

	@Override
	public String[] getWantedEventTypes()
	{
		return EVENT_TYPES ;
	}

	protected class Animation
	{
		public final int id ;
		private final Sprite sprite ;
		private final Event event ;

		private float elapsedTime = 0.0f ;
		private int frame = 0 ;						// Current frame 
		private final float frameDelta ;				// Amount of time that needs to elapse before next frame
		private final int length ;					// How many frames

		public Animation( final int _id, final Event _event, final Sprite _sprite )
		{
			id = _id ;
			event = _event ;
			sprite = _sprite ;
			frameDelta = 1.0f / sprite.framerate ;
			length = sprite.size() ;
			
			changeTexture( event, sprite ) ;
		}

		private void changeTexture( final Event _event, final Sprite _sprite )
		{
			final Settings settings = ( Settings )_event.getVariable() ;
			final String file = sprite.getTexture( frame ) ;

			settings.addString( "FILE", file ) ;
			settings.addObject( "TEXTURE", null ) ;
		}

		public void update( final float _dt )
		{
			elapsedTime += _dt ;
			if( elapsedTime >= frameDelta )
			{
				changeTexture( event, sprite ) ;
				elapsedTime -= frameDelta ;
				frame = ++frame % length ; // Increment frame, reset to 0 if reaches length.
			}
		}

		public void destroy()
		{
			sprite.unregister() ;
		}
	}
}