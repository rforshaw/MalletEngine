package com.linxonline.mallet.animation ;

import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.DrawFactory ;
import com.linxonline.mallet.renderer.Interpolation ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.maths.* ;

/**
	Provides an effective way to generate Events
	for the Animation System.
*/
public final class AnimationFactory
{
	private AnimationFactory() {}

	public static Event<Settings> createAnimation( final String _file,
													final Vector3 _pos, 
													final Vector2 _offset, 			// Not needed
													final Vector2 _dim,				// Not needed
													final Vector2 _fill,			// Not needed
													final Vector3 _clipPosition,	// Not needed
													final Vector3 _clipOffset,		// Not needed
													final int _layer,
													final SourceCallback _callback )	// Not needed, but is important
	{
		final Settings settings = new Settings() ;
		settings.addObject( "REQUEST_TYPE", AnimRequestType.CREATE_ANIMATION ) ;

		if( _file != null )
		{
			settings.addString( "ANIM_FILE", _file ) ;
		}

		final Shape plane = Shape.constructPlane( new Vector3(), new Vector3( _dim.x, _dim.y, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ;
		settings.addObject( "RENDER_EVENT", DrawFactory.amendClip( DrawFactory.createTexture( ( String )null,
																		plane,
																	   _pos,
																	   _offset,
																	   _layer,
																	   null ), Shape.constructPlane( new Vector3(), new Vector3( 400, 400, 0 ), new Vector2(), new Vector2( 1, 1 ) ), new Vector3(), new Vector3( -200, -200, 0 ) ) ) ;

		if( _callback != null )
		{
			settings.addObject( "CALLBACK", _callback ) ;
		}

		return new Event<Settings>( "ANIMATION", settings ) ;
	}

	public static Event<Settings> removeAnimation( final int _id )
	{
		final Settings anim = new Settings() ;
		anim.addObject( "REQUEST_TYPE", AnimRequestType.REMOVE_ANIMATION ) ;
		anim.addInteger( "ID", _id ) ;
		return new Event<Settings>( "ANIMATION", anim ) ;
	}

	public static Event createGarbageCollect()
	{
		final Settings anim = new Settings() ;
		anim.addObject( "REQUEST_TYPE", AnimRequestType.GARBAGE_COLLECT_ANIMATION ) ;
		return new Event( "ANIMATION", anim ) ;
	}

	public static Event<Settings> modifyAnimation( final int _id, final int _modifyType )
	{
		final Settings anim = new Settings() ;
		anim.addObject( "REQUEST_TYPE", AnimRequestType.MODIFY_EXISTING_ANIMATION ) ;
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

	public static Event<Settings> amendInterpolation( final Event<Settings> _event, final Interpolation _interpolation )
	{
		final Settings animSet = _event.getVariable() ;
		final Event<Settings> renderEvent = animSet.<Event<Settings>>getObject( "RENDER_EVENT", null ) ;
		DrawFactory.amendInterpolation( renderEvent, _interpolation ) ;
		return _event ;
	}

	public static Event<Settings> amendGUI( final Event<Settings> _event, final boolean _gui )
	{
		final Settings animSet = _event.getVariable() ;
		final Event<Settings> renderEvent = animSet.<Event<Settings>>getObject( "RENDER_EVENT", null ) ;
		DrawFactory.amendGUI( renderEvent, _gui ) ;
		return _event ;
	}

	public static Event<Settings> amendColour( final Event<Settings> _event, final MalletColour _colour )
	{
		final Settings animSet = _event.getVariable() ;
		final Event<Settings> renderEvent = animSet.<Event<Settings>>getObject( "RENDER_EVENT", null ) ;
		DrawFactory.amendColour( renderEvent, _colour ) ;
		return _event ;
	}

	public static Event<Settings> forceUpdate( final Event<Settings> _event )
	{
		final Settings animSet = _event.getVariable() ;
		final Event<Settings> renderEvent = animSet.<Event<Settings>>getObject( "RENDER_EVENT", null ) ;
		DrawFactory.forceUpdate( renderEvent ) ;
		return _event ;
	}
}