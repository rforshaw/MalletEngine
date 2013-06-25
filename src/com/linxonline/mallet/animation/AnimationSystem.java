package com.linxonline.mallet.animation ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.AddEventInterface ;
import com.linxonline.mallet.util.SystemRoot ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.resources.SpriteManager ;
import com.linxonline.mallet.resources.texture.Sprite ;

public class AnimationSystem extends SystemRoot<Animation>
{
	private final static String[] EVENT_TYPES = { "ANIMATION" } ;
	private final static SpriteManager spriteManager = new SpriteManager() ;

	protected int numID = 0 ;

	public AnimationSystem( final AddEventInterface _eventSystem )
	{
		eventSystem = _eventSystem ;
	}

	@Override
	protected void updateSource( final Animation _source, final float _dt )
	{
		_source.update( _dt ) ;
	}

	@Override
	protected void destroySource( final Animation _source )
	{
		_source.destroy() ;
	}

	@Override
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
				final Animation animation = getSource( anim.getInteger( "ID", -1 ) ) ;
				if( animation != null )
				{
					modifyAnimation( anim, animation ) ;
				}
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
				storeSource( anim, anim.id ) ;
			}
		}
	}

	protected void modifyAnimation(  final Settings _settings, final Animation _animation )
	{
		final int type = _settings.getInteger( "MODIFY_ANIMATION", -1 ) ;
		switch( type )
		{
			case ModifyAnimation.PLAY :
			{
				break ;
			}
			case ModifyAnimation.STOP :
			{
				break ;
			}
			case ModifyAnimation.PAUSE :
			{
				break ;
			}
		}
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
	public String[] getWantedEventTypes()
	{
		return EVENT_TYPES ;
	}
}