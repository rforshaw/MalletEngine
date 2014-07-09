package com.linxonline.mallet.animation ;

import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.renderer.DrawFactory ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.maths.* ;

/**
	Provides an effective way to generate Events
	for the Animation System.
*/
public class AnimationFactory
{
	public static Event<Settings> createAnimation( final String _file,
										 final Vector3 _pos, 
										 final Vector2 _offset, 			// Not needed
										 final Vector2 _dim,				// Not needed
										 final Vector2 _fill,				// Not needed
										 final Vector2 _clip,				// Not needed
										 final Vector2 _clipOffset,			// Not needed
										 final int _layer,
										 final SourceCallback _callback )	// Not needed, but is important
	{
		final Settings settings = new Settings() ;
		settings.addInteger( "REQUEST_TYPE", AnimRequestType.CREATE_ANIMATION ) ;

		if( _file != null )
		{
			settings.addString( "ANIM_FILE", _file ) ;
		}

		settings.addObject( "RENDER_EVENT", DrawFactory.createTexture( null,
																	 _pos, _offset,
																	 _dim, _fill, 
																	 _clip, _clipOffset, _layer ) ) ;

		if( _callback != null )
		{
			settings.addObject( "CALLBACK", _callback ) ;
		}

		return new Event<Settings>( "ANIMATION", settings ) ;
	}

	public static Event<Settings> removeAnimation( final int _id )
	{
		final Settings anim = new Settings() ;
		anim.addInteger( "REQUEST_TYPE", AnimRequestType.REMOVE_ANIMATION ) ;
		anim.addInteger( "ID", _id ) ;
		return new Event<Settings>( "ANIMATION", anim ) ;
	}

	public static Event createGarbageCollect()
	{
		final Settings anim = new Settings() ;
		anim.addInteger( "REQUEST_TYPE", AnimRequestType.GARBAGE_COLLECT_ANIMATION ) ;
		return new Event( "ANIMATION", anim ) ;
	}

	public static Event<Settings> modifyAnimation( final int _id, final int _modifyType )
	{
		final Settings anim = new Settings() ;
		anim.addInteger( "REQUEST_TYPE", AnimRequestType.MODIFY_EXISTING_ANIMATION ) ;
		anim.addInteger( "ID", _id ) ;
		anim.addInteger( "MODIFY_ANIMATION", _modifyType ) ;
		return new Event<Settings>( "ANIMATION", anim ) ;
	}

	public static Event<Settings> amendRotate( final Event<Settings> _event, final float _rotate )
	{
		final Settings animSet = _event.getVariable() ;
		final Event<Settings> renderEvent = animSet.<Event<Settings>>getObject( "RENDER_EVENT", null ) ;
		DrawFactory.amendRotate( renderEvent, _rotate ) ;
		return _event ;
	}
}