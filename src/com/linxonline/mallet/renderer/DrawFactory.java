package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.maths.* ;

/**
	Provides an effective way to generate Events
	for the Rendering System.
	Supports: G2DRenderer, GLRenderer...
*/
public final class DrawFactory
{
	private DrawFactory() {}

	public static Event removeDraw( final int _id )
	{
		final Settings draw = new Settings() ;
		draw.addObject( "REQUEST_TYPE", DrawRequestType.REMOVE_DRAW ) ;
		draw.addInteger( "ID", _id ) ;
		return new Event( "DRAW", draw ) ;
	}

	public static Event createGarbageCollect()
	{
		final Settings draw = new Settings() ;
		draw.addObject( "REQUEST_TYPE", DrawRequestType.GARBAGE_COLLECT_DRAW ) ;
		return new Event( "DRAW", draw ) ;
	}

	public static Event<Settings> createTexture( final MalletTexture _texture,
												 final Vector3 _pos, 
												 final Vector2 _offset, 		// Not needed
												 final Vector2 _dim,			// Not needed
												 final Vector2 _fill,			// Not needed
												 final Vector2 _uv1,			// Not needed
												 final Vector2 _uv2,			// Not needed
												 final int _layer,
												 final IDInterface _callback )
	{
		return createTexture( _texture.getPath(), _pos, _offset, _dim, _fill, _uv1, _uv2, _layer, _callback ) ;
	}

	public static Event<Settings> createTexture( final String _file,
												 final Vector3 _pos, 
												 final Vector2 _offset, 		// Not needed
												 final Vector2 _dim,			// Not needed
												 final Vector2 _fill,			// Not needed
												 final Vector2 _uv1,			// Not needed
												 final Vector2 _uv2,			// Not needed
												 final int _layer,
												 final IDInterface _callback )
	{
		final Settings settings = new Settings() ;

		settings.addObject( "REQUEST_TYPE", DrawRequestType.CREATE_DRAW ) ;
		settings.addObject( "TYPE", DrawRequestType.TEXTURE ) ;

		if( _file != null ) { settings.addString( "FILE", _file ) ; }
		if( _dim != null ) { settings.addObject( "DIM", _dim ) ; }
		if( _fill != null ) { settings.addObject( "FILL", _fill ) ; }

		if( _uv1 != null ) { settings.addObject( "UV1", _uv1 ) ; }
		if( _uv2 != null ) { settings.addObject( "UV2", _uv2 ) ; }

		if( _callback != null ) { settings.addObject( "CALLBACK", _callback ) ; }

		setPosition( settings, _pos, _offset ) ;
		//setClip( settings, _clip, _clipOffset ) ;
		settings.addInteger( "LAYER", _layer ) ;

		return new Event<Settings>( "DRAW", settings ) ;
	}

	public static Event<Settings> amendRotate( final Event<Settings> _event, final float _rotate )
	{
		final Settings set = _event.getVariable() ;
		set.addFloat( "ROTATE", _rotate ) ;
		return _event ;
	}

	/**
		Inform the renderer to handle the draw request as a GUI element.
		Amend the draw request to prevent camera position and scale from being 
		applied. 
	*/
	public static Event<Settings> amendGUI( final Event<Settings> _event, final boolean _set )
	{
		final Settings set = _event.getVariable() ;
		set.addBoolean( "GUI", _set ) ;
		return _event ;
	}

	public static Event<Settings> amendInterpolation( final Event<Settings> _event, final Interpolation _set )
	{
		final Settings set = _event.getVariable() ;
		set.addObject( "INTERPOLATION", _set ) ;
		return _event ;
	}

	public static Event<Settings> amendColour( final Event<Settings> _event, final MalletColour _colour )
	{
		final Settings set = _event.getVariable() ;
		set.addObject( "COLOUR", _colour ) ;
		return _event ;
	}

	/**
		Inform the renderer that the render event is to be updated.
		Geometry or colour changes require a forceUpdate. Texture/UV 
		changes are automatically updated per frame currently.
		Translation, rotation, or scale do not require the renderer to 
		update its state.
	*/
	public static Event<Settings> forceUpdate( final Event<Settings> _event )
	{
		final Settings set = _event.getVariable() ;
		set.addBoolean( "UPDATE", true ) ;
		return _event ;
	}
	
	/**
		Inform the renderer to return the id of the draw request.
		Must be applied before being passed to the renderer.
	*/
	public static void insertIDCallback( final Event<Settings> _event, final IDInterface _callback )
	{
		final Settings sets = _event.getVariable() ;
		sets.addObject( "CALLBACK", _callback ) ;
	}

	public static Settings setPosition( final Settings _settings, final Vector3 _pos, final Vector2 _offset )
	{
		if( _pos != null ) { _settings.addObject( "POSITION", _pos ) ; }
		if( _offset != null ) { _settings.addObject( "OFFSET", _offset ) ; }
		return _settings ;
	}

	public static Settings setClip( final Settings _settings, final Vector2 _clip, final Vector2 _clipOffset )
	{
		if( _clip != null ) { _settings.addObject( "CLIP", _clip ) ; }
		if( _clipOffset != null ) { _settings.addObject( "CLIPOFFSET", _clipOffset ) ; }
		return _settings ;
	}
}