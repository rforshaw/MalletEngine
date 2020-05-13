package com.linxonline.mallet.entity.components ;

import java.util.List ;
import java.util.Map ;

import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.animation.AnimationDelegateCallback ;
import com.linxonline.mallet.animation.AnimationDelegate ;
import com.linxonline.mallet.animation.AnimationAssist ;
import com.linxonline.mallet.animation.Anim ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.renderer.World ;

public class AnimComponent extends EventComponent implements SourceCallback
{
	private static final int ANIM_NOT_SET = -1 ;

	private final Map<String, Tuple<Anim, World>> animations = MalletMap.<String, Tuple<Anim, World>>newMap() ;

	private String defaultAnim = null ;					// Name of the default animation, used as a fallback if all else fails.
	private Tuple<Anim, World> currentAnim   = null ;					// Name of the current animation that is playing

	private AnimationDelegate delegate        = null ;
	private Entity.ReadyCallback toDestroy = null ;
	private SourceCallback callback           = null ;

	public AnimComponent( final Entity _parent )
	{
		this( _parent, Entity.AllowEvents.YES ) ;
	}

	public AnimComponent( final Entity _parent, Entity.AllowEvents _allow )
	{
		this( _parent, _allow, 0, 0 ) ;
	}

	public AnimComponent( final Entity _parent,
						  final Entity.AllowEvents _allow,
						  final int _stateCapacity,
						  final int _backendCapacity )
	{
		super( _parent, _allow, _stateCapacity, _backendCapacity ) ;
	}

	public void addAnimation( final String _name, final Anim _anim )
	{
		addAnimation( _name, _anim, null ) ;
	}

	public void addAnimation( final String _name, final Anim _anim, final World _world )
	{
		AnimationAssist.addCallback( _anim, this ) ;
		animations.put( _name, new Tuple<Anim, World>( _anim, _world ) ) ;
	}

	public void removeAnimation( final String _name )
	{
		animations.remove( _name ) ;
	}

	public Anim getAnimation( final String _name )
	{
		return animations.get( _name ).getLeft() ;
	}

	public void setDefaultAnim( final String _name )
	{
		defaultAnim = _name ;
	}

	@Override
	public void readyToDestroy( final Entity.ReadyCallback _callback )
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
		final Tuple<Anim, World> tuple = animations.get( _name ) ;
		if( delegate != null && tuple != null )
		{
			delegate.addAnimation( tuple.getLeft(), tuple.getRight() ) ;
			currentAnim = tuple ;
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
			delegate.removeAnimation( currentAnim.getLeft() ) ;
		}
	}

	public void callbackRemoved() {}

	public Anim getCurrentAnimation()
	{
		return ( currentAnim != null ) ? currentAnim.getLeft() : null ;
	}

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
	public void passInitialEvents( final List<Event<?>> _events )
	{
		_events.add( AnimationAssist.constructAnimationDelegate( ( final AnimationDelegate _delegate ) ->
		{
			delegate = _delegate ;
			if( defaultAnim != null )
			{
				playAnimation( defaultAnim ) ;
			}
		} ) ) ;
		super.passInitialEvents( _events ) ;
	}
}
