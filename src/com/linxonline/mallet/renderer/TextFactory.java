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
											  final Vector2 _offset,
											  final MalletFont _font,
											  final MalletColour _colour,
											  final int _layer,
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

		settings.addInteger( "LAYER", _layer ) ;
		settings.addBoolean( "UPDATE", true ) ;

		return new Event<Settings>( "DRAW", settings ) ;
	}

	public static Event<Settings> amendColour( final Event<Settings> _event, final MalletColour _colour )
	{
		final Settings set = _event.getVariable() ;
		set.addObject( "COLOUR", _colour ) ;
		return _event ;
	}
}
