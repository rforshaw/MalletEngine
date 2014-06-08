package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.animation.AnimationFactory ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.event.Event ;

public class AnimComponent extends EventComponent implements SourceCallback
{
	private static final int ANIM_NOT_SET = -1 ;

	private final HashMap<String, Event<Settings>> animations = new HashMap<String, Event<Settings>>() ;
	private String defaultAnim = null ;
	private int animationID = -1 ;

	public AnimComponent()
	{
		super( "ANIM" ) ;
	}

	public void addAnimation( final String _name, final Event<Settings> _anim )
	{
		animations.put( _name, _anim ) ;
	}

	public void removeAnimation( final String _name )
	{
		animations.remove( _name ) ;
	}

	public Event<Settings> getAnimation( final String _name )
	{
		return animations.get( _name ) ;
	}

	public void setDefaultAnim( final String _name )
	{
		defaultAnim = _name ;
	}

	/**
		Begin playing specified animation
	**/
	public void playAnimation( final String _name )
	{
		stopAnimation() ; 								// Stop the previous animation
		final Event<Settings> event = animations.get( _name ) ;
		if( event != null )
		{
			passEvent( event ) ;
		}
	}

	/**
		Remove the current animation from the Animation system.
	**/
	public void stopAnimation()
	{
		if( animationID != ANIM_NOT_SET )
		{
			passEvent( AnimationFactory.removeAnimation( animationID ) ) ;
			animationID = ANIM_NOT_SET ;
		}
	}

	public void recieveID( final int _id ) { animationID = _id ; }

	public void callbackRemoved() {}

	public void start() {}
	public void pause() {}
	public void stop() {}

	public void update( final float _dt ) {}

	public void finished() {}

	@Override
	public void passInitialEvents( final ArrayList<Event> _events )
	{
		if( defaultAnim != null )
		{
			_events.add( AnimationFactory.removeAnimation( animationID ) ) ;
			_events.add( animations.get( defaultAnim ) ) ;
		}
	}

	@Override
	public void passFinalEvents( final ArrayList<Event> _events )
	{
		_events.add( AnimationFactory.removeAnimation( animationID ) ) ;
	}
}