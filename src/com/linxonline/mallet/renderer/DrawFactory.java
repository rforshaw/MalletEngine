package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.maths.* ;

/**
	Provides an effective way to generate Events
	for the Rendering System.
*/
public final class DrawFactory
{
	private DrawFactory() {}

	public static Event<Settings> removeDraw( final int _id )
	{
		final Settings draw = new Settings() ;
		draw.addObject( "REQUEST_TYPE", DrawRequestType.REMOVE_DRAW ) ;
		draw.addInteger( "ID", _id ) ;
		return new Event<Settings>( "DRAW", draw ) ;
	}

	/**
		Request the rendering system to clean-up resources 
		that it has accumulated. This should remove textures 
		or programs that are not currently being used.
	*/
	public static Event<Settings> createGarbageCollect()
	{
		final Settings draw = new Settings() ;
		draw.addObject( "REQUEST_TYPE", DrawRequestType.GARBAGE_COLLECT_DRAW ) ;
		return new Event<Settings>( "DRAW", draw ) ;
	}

	/**
		Upload a Program/Shader to the rendering system.
		Once loaded use the _key within a draw event to 
		use the Program/Shader. Use amendProgram().
	*/
	public static Event<Settings> createProgram( final String _key, final String _file )
	{
		final Settings program = new Settings() ;
		program.addObject( "REQUEST_TYPE", DrawRequestType.CREATE_SHADER_PROGRAM ) ;
		program.addString( "PROGRAM_KEY", _key ) ;
		program.addString( "PROGRAM_FILE", _file ) ;
		return new Event<Settings>( "PROGRAM", program ) ;
	}

	/**
		Assign one texture to piece of geometry.
		By default uses the SIMPLE_TEXTURE program.
	*/
	public static Event<Settings> createTexture( final MalletTexture _texture,
												 final Shape _shape,
												 final Vector3 _pos, 
												 final Vector2 _offset, 		// Not needed
												 final int _layer,
												 final IDInterface _callback )
	{
		return createTexture( _texture.getPath(), _shape, _pos, _offset, _layer, _callback ) ;
	}

	/**
		Assign one texture to piece of geometry.
		By default uses the SIMPLE_TEXTURE program.
	*/
	public static Event<Settings> createTexture( final String _file,
												 final Shape _shape,
												 final Vector3 _pos, 
												 final Vector2 _offset, 		// Not needed
												 final int _layer,
												 final IDInterface _callback )
	{
		final Settings settings = new Settings() ;

		settings.addObject( "REQUEST_TYPE", DrawRequestType.CREATE_DRAW ) ;
		settings.addObject( "TYPE", DrawRequestType.TEXTURE ) ;

		if( _file != null ) { settings.addString( "FILE", _file ) ; }
		if( _callback != null ) { settings.addObject( "CALLBACK", _callback ) ; }

		setPosition( settings, _pos, _offset ) ;
		settings.addInteger( "LAYER", _layer ) ;

		if( _shape != null ) { settings.addObject( "SHAPE", _shape ) ; }

		return new Event<Settings>( "DRAW", settings ) ;
	}

	/**
		Assign multiple textures to piece of geometry.
		When using multi-textures you must specify a 
		Program/Shader that determines how these textures 
		are blended together.
	*/
	public static Event<Settings> createMultiTexture( final String _programKey,
													  final MalletTexture[] _textures,
													  final Shape _shape,
													  final Vector3 _pos, 
													  final Vector2 _offset, 		// Not needed
													  final int _layer,
													  final IDInterface _callback )
	{
		final String[] files = new String[_textures.length] ;
		for( int i = 0; i < _textures.length; i++ )
		{
			files[i] = _textures[i].getPath() ;
		}

		return createMultiTexture( _programKey, files, _shape, _pos, _offset, _layer, _callback ) ;
	}

	/**
		Assign multiple textures to piece of geometry.
		When using multi-textures you must specify a 
		Program/Shader that determines how these textures 
		are blended together.
	*/
	public static Event<Settings> createMultiTexture( final String _programKey,
												 final String[] _files,
												 final Shape _shape,
												 final Vector3 _pos, 
												 final Vector2 _offset, 		// Not needed
												 final int _layer,
												 final IDInterface _callback )
	{
		final Settings settings = new Settings() ;

		settings.addObject( "REQUEST_TYPE", DrawRequestType.CREATE_DRAW ) ;
		settings.addObject( "TYPE", DrawRequestType.TEXTURE ) ;

		if( _programKey != null ) { settings.addObject( "PROGRAM", _programKey ) ; }
		if( _files != null ) { settings.addObject( "FILES", _files ) ; }
		if( _callback != null ) { settings.addObject( "CALLBACK", _callback ) ; }

		setPosition( settings, _pos, _offset ) ;
		settings.addInteger( "LAYER", _layer ) ;

		if( _shape != null ) { settings.addObject( "SHAPE", _shape ) ; }

		return new Event<Settings>( "DRAW", settings ) ;
	}

	/**
		Specify a program that should affect the draw event.
		This will only be usable with rendering systems 
		that support shaders.
		OpenGL GLSL for example.
	*/
	public static Event<Settings> amendProgram( final Event<Settings> _event, final String _programKey )
	{
		final Settings set = _event.getVariable() ;
		set.addObject( "PROGRAM", _programKey ) ;
		return _event ;
	}

	/**
		Used to inform the renderer whether it should continuously 
		update the rendering data or be told to update.
		Use DrawRequestType.CONTINUOUS to update indefinately or 
		DrawRequestType.ON_DEMAND to update on UPDATE request.
	*/
	public static Event<Settings> amendUpdateType( final Event<Settings> _event, final DrawRequestType _updateType )
	{
		final Settings set = _event.getVariable() ;
		set.addObject( "UPDATE_TYPE", _updateType ) ;
		return _event ;
	}

	/**
		Define a location that _event can be displayed in.
		That allows the _event to be very large but only 
		a subset of the content to be visible.
	*/
	public static Event<Settings> amendClip( final Event<Settings> _event,
											 final Shape _clip,
											 final Vector3 _clipPosition,
											 final Vector3 _clipOffset )
	{
		final Settings set = _event.getVariable() ;
		set.addObject( "CLIP_SHAPE", _clip ) ;
		set.addObject( "CLIP_POSITION", _clipPosition ) ;
		set.addObject( "CLIP_OFFSET", _clipOffset ) ;
		return _event ;
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

	/**
		Define what type of interpolation should be applied
		between frames.
	*/
	public static Event<Settings> amendInterpolation( final Event<Settings> _event, final Interpolation _set )
	{
		final Settings set = _event.getVariable() ;
		set.addObject( "INTERPOLATION", _set ) ;
		return _event ;
	}

	/**
		Inform the renderer that the render event is to be updated.
		Geometry or colour changes require a forceUpdate. Texture/UV 
		changes are automatically updated per frame currently.
		Translation, rotation, or scale do not require the renderer to 
		update its state.
		Used in conjunction with DrawRequestType.ON_DEMAND
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
}