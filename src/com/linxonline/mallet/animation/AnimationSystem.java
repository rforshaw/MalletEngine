package com.linxonline.mallet.animation ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.EventUpdater ;
import com.linxonline.mallet.util.settings.Settings ;

public class AnimationSystem extends EventUpdater
{
	private static final String[] EVENT_TYPES = { "ANIMATION" } ;

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
		
	}

	@Override
	public final void passEvent( final Event _event ) {}

	@Override
	public String[] getWantedEventTypes()
	{
		return EVENT_TYPES ;
	}
}