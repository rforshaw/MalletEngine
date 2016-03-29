package com.linxonline.mallet.animation ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;

public class AnimationAssist
{
	private static final Event ANIMATION_CLEAN = new Event( "ANIMATION_CLEAN", null ) ;

	/**
		Request an AnimDelegate from the active animation system.
		The AnimDelegate allows the user to add/remove Anim objects
		from being rendered.

		An AnimDelegate is not required for constructing an Anim object, 
		but is required for displaying it.
	*/
	public static Event<AnimationDelegateCallback> constructAnimationDelegate( final AnimationDelegateCallback _callback )
	{
		return new Event<AnimationDelegateCallback>( "ANIMATION_DELEGATE", _callback ) ;
	}

	/**
		Request the active animation system to clean-up any 
		unused resources it may still be referencing.
	*/
	public static Event constructAnimationClean()
	{
		return ANIMATION_CLEAN ;
	}

	public static Anim createAnimation( final String _file,
										final Vector3 _position,
										final Vector3 _offset,
										final Vector3 _rotation,
										final Vector3 _scale,
										final int _order )
	{
		final Draw draw = DrawAssist.createDraw( _position,
												 _offset,
												 _rotation,
												 _scale,
												 _order ) ;
		DrawAssist.attachProgram( draw, "SIMPLE_TEXTURE" ) ;

		return new AnimData( _file, draw ) ;
	}

	public static Draw getDraw( final Anim _anim )
	{
		return ( ( AnimData )_anim ).getDraw() ;
	}

	public static Anim play( final Anim _anim )
	{
		( ( AnimData )_anim ).play() ;
		return _anim ;
	}

	public static Anim stop( final Anim _anim )
	{
		( ( AnimData )_anim ).stop() ;
		return _anim ;
	}

	public static Anim pause( final Anim _anim )
	{
		( ( AnimData )_anim ).pause() ;
		return _anim ;
	}

	public static Anim addCallback( final Anim _anim, final SourceCallback _callback )
	{
		( ( AnimData )_anim ).addCallback( _callback ) ;
		return _anim ;
	}

	public static Anim removeCallback( final Anim _anim, final SourceCallback _callback )
	{
		( ( AnimData )_anim ).removeCallback( _callback ) ;
		return _anim ;
	}
}