package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.maths.* ;

/**
	Event requests for generating text within 
	a renderer. Make sure the renderer used 
	supports these events.
*/
public final class TextFactory
{
	private TextFactory() {}

	public static Event<Settings> createText( final String _text,
											  final Vector3 _pos, 
											  final Vector2 _offset, 		// Not needed
											  final MalletFont _font,
											  final MalletColour _colour,
											  final Vector2 _clip,			// Not needed
											  final Vector2 _clipOffset,	// Not needed
											  final int _layer,
											  final int _alignment,
											  final IDInterface _callback )
	{
		final Settings settings = new Settings() ;

		settings.addObject( "REQUEST_TYPE", DrawRequestType.CREATE_DRAW ) ;
		settings.addObject( "TYPE", DrawRequestType.TEXT ) ;

		if( _text != null ) { settings.addString( "TEXT", _text ) ; }
		if( _font != null ) { settings.addObject( "FONT", _font ) ; }
		if( _colour != null ) { settings.addObject( "COLOUR", _colour ) ; }
		if( _callback != null ) { settings.addObject( "CALLBACK", _callback ) ; }

		DrawFactory.setPosition( settings, _pos, _offset ) ;
		DrawFactory.setClip( settings, _clip, _clipOffset ) ;

		settings.addInteger( "LAYER", _layer ) ;
		settings.addInteger( "ALIGNMENT", _alignment ) ;

		return new Event<Settings>( "DRAW", settings ) ;
	}
}
