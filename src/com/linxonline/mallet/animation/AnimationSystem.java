package com.linxonline.mallet.animation ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.EventUpdater ;

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
	
	protected void useEvent( final Event _event ) {}

	@Override
	public final void passEvent( final Event _event ) {}

	@Override
	public String[] getWantedEventTypes()
	{
		return EVENT_TYPES ;
	}
}