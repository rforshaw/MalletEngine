package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.animation.AnimationDelegateCallback ;
import com.linxonline.mallet.animation.AnimationDelegate ;
import com.linxonline.mallet.animation.AnimationAssist ;
import com.linxonline.mallet.animation.Anim ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.event.Event ;

public class AnimComponent extends EventComponent implements SourceCallback
{
	private static final int ANIM_NOT_SET = -1 ;

	private final HashMap<String, Anim> animations = new HashMap<String, Anim>() ;

	private String defaultAnim = null ;					// Name of the default animation, used as a fallback if all else fails.
	private Anim currentAnim   = null ;					// Name of the current animation that is playing

	private AnimationDelegate delegate        = null ;
	private Component.ReadyCallback toDestroy = null ;
	private SourceCallback callback           = null ;

	public AnimComponent()
	{
		this( "ANIM" ) ;
	}

	public AnimComponent( final String _name )
	{
		super( _name ) ;
	}

	public void addAnimation( final String _name, final Anim _anim )
	{
		AnimationAssist.addCallback( _anim, this ) ;
		animations.put( _name, _anim ) ;
	}

	public void removeAnimation( final String _name )
	{
		animations.remove( _name ) ;
	}

	public Anim getAnimation( final String _name )
	{
		return animations.get( _name ) ;
	}

	public void setDefaultAnim( final String _name )
	{
		defaultAnim = _name ;
	}

	@Override
	public void readyToDestroy( final Component.ReadyCallback _callback )
	{
		if( delegate != null )
		{
			delegate.shutdown() ;
			delegate = null ;
		}

		toDestroy = _callback ;
		super.readyToDestroy( _callback ) ;
	}

	@Override
	public void tick( final float _dt )
	{
		if( callback != null )
		{
			callback.tick( _dt ) ;
		}
	}

	public void playAnimation( final String _name, final SourceCallback _callback )
	{
		playAnimation( _name ) ;
		callback = _callback ;
	}

	/**
		Begin playing specified animation as soon as possible.
		If called very quickly, repeatedly, some animations 
		may never get rendered.
	**/
	public void playAnimation( final String _name )
	{
		if( toDestroy != null )
		{
			return ;
		}

		stopAnimation() ;
		final Anim anim = animations.get( _name ) ;
		if( delegate != null && anim != null )
		{
			delegate.addAnimation( anim ) ;
			currentAnim = anim ;
		}
	}

	/**
		Remove the current animation from the Animation system.
	**/
	public void stopAnimation()
	{
		if( toDestroy != null )
		{
			return ;
		}

		if( delegate != null && currentAnim != null )
		{
			delegate.removeAnimation( currentAnim ) ;
		}
	}

	public void callbackRemoved() {}

	@Override
	public void start()
	{
		if( callback != null )
		{
			callback.start() ;
		}
	}

	@Override
	public void pause()
	{
		if( callback != null )
		{
			callback.pause() ;
		}
	}

	@Override
	public void stop()
	{
		if( callback != null )
		{
			callback.stop() ;
		}
	}

	@Override
	public void finished()
	{
		if( callback != null )
		{
			callback.finished() ;
		}
	}

	@Override
	public void passInitialEvents( final ArrayList<Event<?>> _events )
	{
		_events.add( AnimationAssist.constructAnimationDelegate( new AnimationDelegateCallback()
		{
			public void callback( AnimationDelegate _delegate )
			{
				delegate = _delegate ;
				if( defaultAnim != null )
				{
					playAnimation( defaultAnim ) ;
				}
			}
		} ) ) ;
		super.passInitialEvents( _events ) ;
	}
}