package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.maths.* ;

/**
	Event requests for generating geometry within 
	a renderer. Make sure the renderer used 
	supports these events.
*/
public final class GeometryFactory
{
	private GeometryFactory() {}

	public static Event<Settings> createShape( 	final String _type,
												final Line _line,
												final Vector3 _pos, 
												final Vector2 _offset, 		// Not needed
												final Vector2 _clip,		// Not needed
												final Vector2 _clipOffset,	// Not needed
												final int _layer,
												final IDInterface _callback )
	{
		final Settings settings = new Settings() ;

		settings.addInteger( "REQUEST_TYPE", DrawRequestType.CREATE_DRAW ) ;
		settings.addInteger( "TYPE", DrawRequestType.GEOMETRY ) ;
		if( _type == null || _line == null )
		{
			return null ;
		}

		if( _callback != null ) { settings.addObject( "CALLBACK", _callback ) ; }

		DrawFactory.setPosition( settings, _pos, _offset ) ;
		DrawFactory.setClip( settings, _clip, _clipOffset ) ;

		settings.addObject( _type, _line ) ;
		settings.addInteger( "LAYER", _layer ) ;

		return new Event<Settings>( "DRAW", settings ) ;
	}

	public static Event<Settings> createShape(  final String _type,
												final Shape _shape,
												final Vector3 _pos, 
												final Vector2 _offset, 		// Not needed
												final Vector2 _clip,		// Not needed
												final Vector2 _clipOffset,	// Not needed
												final int _layer,
												final IDInterface _callback )
	{
		final Settings settings = new Settings() ;

		settings.addInteger( "REQUEST_TYPE", DrawRequestType.CREATE_DRAW ) ;
		settings.addInteger( "TYPE", DrawRequestType.GEOMETRY ) ;

		DrawFactory.setPosition( settings, _pos, _offset ) ;
		DrawFactory.setClip( settings, _clip, _clipOffset ) ;

		if( _callback != null ) { settings.addObject( "CALLBACK", _callback ) ; }

		if( _type != null ) { settings.addObject( _type, _shape ) ; }
		settings.addInteger( "LAYER", _layer ) ;

		return new Event<Settings>( "DRAW", settings ) ;
	}
}