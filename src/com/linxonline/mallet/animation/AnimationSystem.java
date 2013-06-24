package com.linxonline.mallet.animation ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.EventUpdater ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.resources.SpriteManager ;
import com.linxonline.mallet.resources.texture.Sprite ;

public class AnimationSystem extends EventUpdater
{
	private final static String[] EVENT_TYPES = { "ANIMATION" } ;

	private final static SpriteManager spriteManager = new SpriteManager() ;

	private final HashMap<Integer, Animation> animations = new HashMap<Integer, Animation>() ;
	private final ArrayList<Animation> activeAnimations = new ArrayList<Animation>() ;
	private final ArrayList<Animation> removeAnimations = new ArrayList<Animation>() ;

	protected int numID = 0 ;
	
	public AnimationSystem() {}

	public void update( final float _dt )
	{
		updateEvents() ;
		updateAnimations( _dt ) ;
		removeAnimations() ;
	}

	protected void updateAnimations( final float _dt ) {}

	protected void removeAnimations() {}

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
				
				break ;
			}
		}
	}

	protected void createAnimation( final Settings _anim )
	{
		final String file = _anim.getString( "ANIM_FILE", null ) ;
		if( file != null )
		{
			final Animation anim = new Animation( numID++, ( Sprite )spriteManager.get( file ) ) ;
			if( anim != null )
			{
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
	public final void passEvent( final Event _event ) {}

	@Override
	public String[] getWantedEventTypes()
	{
		return EVENT_TYPES ;
	}

	protected class Animation
	{
		public final int id ;
		private final Sprite sprite ;
		private float elapsedTime = 0.0f ;
		private int frame = 0 ;						// Current frame 
		private final float frameDelta ;				// Amount of time that needs to elapse before next frame
		private final int length ;					// How many frames

		public Animation( final int _id, final Sprite _sprite )
		{
			id = _id ;
			sprite = _sprite ;
			frameDelta = 1.0f / sprite.framerate ;
			length = sprite.size() ;
		}

		public void update( final float _dt )
		{
			elapsedTime += _dt ;
			if( elapsedTime >= frameDelta )
			{
				//Make render calls
				elapsedTime -= frameDelta ;
				frame = ++frame % length ; // Increment frame, reset to 0 if reaches length.
			}
		}
	}
}