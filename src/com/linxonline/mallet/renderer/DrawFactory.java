package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.resources.ResourceManager ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.maths.* ;

public class DrawFactory
{
	private final static String REQUEST_TYPE = "REQUEST_TYPE" ;
	private final static String TYPE = "TYPE" ;
	private final static String DRAW = "DRAW" ;

	private final static String TEXTURE = "TEXTURE" ;
	private final static String TEXT = "TEXT" ;

	private final static String CLIPOFFSET = "CLIPOFFSET" ;
	private final static String ALIGNMENT = "ALIGNMENT" ;
	private final static String POSITION = "POSITION" ;
	private final static String OFFSET = "OFFSET" ;
	private final static String COLOUR = "COLOUR" ;
	private final static String LAYER = "LAYER" ;
	private final static String FONT = "FONT" ;
	private final static String FILL = "FILL" ;
	private final static String CLIP = "CLIP" ;
	private final static String DIM = "DIM" ;

	public DrawFactory() {}

	public static Event createTexture( 	final String _file,
										final Vector3 _pos, 
										final Vector2 _offset, 		// Not needed
										final Vector2 _dim,			// Not needed
										final Vector2 _fill,		// Not needed
										final Vector2 _clip,		// Not needed
										final Vector2 _clipOffset,	// Not needed
										final int _layer )
	{
		final ResourceManager resources = ResourceManager.getResourceManager() ;
		final Settings settings = new Settings() ;

		settings.addInteger( REQUEST_TYPE, DrawRequestType.CREATE_DRAW ) ;
		settings.addInteger( TYPE, DrawRequestType.TEXTURE ) ;

		//if( _file != null ) { settings.addObject( TEXTURE, resources.getTexture( _file ) ) ; }
		if( _file != null ) { settings.addString( "FILE", _file ) ; }
		if( _dim != null ) { settings.addObject( DIM, _dim ) ; }
		if( _fill != null ) { settings.addObject( FILL, _fill ) ; }

		setPosition( settings, _pos, _offset ) ;
		setClip( settings, _clip, _clipOffset ) ;
		settings.addInteger( LAYER, _layer ) ;

		return new Event( DRAW, settings ) ;
	}

	public static Event createShape( 	final String _type,
										final Line _line,
										final Vector3 _pos, 
										final Vector2 _offset, 		// Not needed
										final Vector2 _clip,		// Not needed
										final Vector2 _clipOffset,	// Not needed
										final int _layer )
	{
		final ResourceManager resources = ResourceManager.getResourceManager() ;
		final Settings settings = new Settings() ;

		settings.addInteger( REQUEST_TYPE, DrawRequestType.CREATE_DRAW ) ;
		settings.addInteger( TYPE, DrawRequestType.GEOMETRY ) ;
		if( _type == null || _line == null )
		{
			return null ;
		}

		setPosition( settings, _pos, _offset ) ;
		setClip( settings, _clip, _clipOffset ) ;

		settings.addObject( _type, _line ) ;
		settings.addInteger( LAYER, _layer ) ;

		return new Event( DRAW, settings ) ;
	}

	public static Event createShape( 	final String _type,
										final Shape _shape,
										final Vector3 _pos, 
										final Vector2 _offset, 		// Not needed
										final Vector2 _clip,		// Not needed
										final Vector2 _clipOffset,	// Not needed
										final int _layer )
	{
		final ResourceManager resources = ResourceManager.getResourceManager() ;
		final Settings settings = new Settings() ;

		settings.addInteger( REQUEST_TYPE, DrawRequestType.CREATE_DRAW ) ;
		settings.addInteger( TYPE, DrawRequestType.GEOMETRY ) ;

		setPosition( settings, _pos, _offset ) ;
		setClip( settings, _clip, _clipOffset ) ;

		if( _type != null ) { settings.addObject( _type, _shape ) ; }
		settings.addInteger( LAYER, _layer ) ;

		return new Event( DRAW, settings ) ;
	}
	
	public static Event createText( 	final String _text,
										final Vector3 _pos, 
										final Vector2 _offset, 		// Not needed
										final MalletFont _font,
										final MalletColour _colour,
										final Vector2 _clip,		// Not needed
										final Vector2 _clipOffset,	// Not needed
										final int _layer,
										final int _alignment )
	{
		final ResourceManager resources = ResourceManager.getResourceManager() ;
		final Settings settings = new Settings() ;

		settings.addInteger( REQUEST_TYPE, DrawRequestType.CREATE_DRAW ) ;
		settings.addInteger( TYPE, DrawRequestType.TEXT ) ;
		if( _text != null ) { settings.addString( TEXT, _text ) ; }
		if( _font != null ) { settings.addObject( FONT, _font ) ; }
		if( _colour != null ) { settings.addObject( COLOUR, _colour ) ; }

		setPosition( settings, _pos, _offset ) ;
		setClip( settings, _clip, _clipOffset ) ;

		settings.addInteger( LAYER, _layer ) ;
		settings.addInteger( ALIGNMENT, _alignment ) ;

		return new Event( DRAW, settings ) ;
	}
	
	private static Settings setPosition( final Settings _settings, final Vector3 _pos, final Vector2 _offset )
	{
		if( _pos != null ) { _settings.addObject( POSITION, _pos ) ; }
		if( _offset != null ) { _settings.addObject( OFFSET, _offset ) ; }
		return _settings ;
	}

	private static Settings setClip( final Settings _settings, final Vector2 _clip, final Vector2 _clipOffset )
	{
		if( _clip != null ) { _settings.addObject( CLIP, _clip ) ; }
		if( _clipOffset != null ) { _settings.addObject( CLIPOFFSET, _clipOffset ) ; }
		return _settings ;
	}
}