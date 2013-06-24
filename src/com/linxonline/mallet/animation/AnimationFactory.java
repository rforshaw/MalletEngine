package com.linxonline.mallet.animation ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.renderer.DrawFactory ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.maths.* ;

public class AnimationFactory
{
	public static Event createAnimation( 	final String _file,
										final Vector3 _pos, 
										final Vector2 _offset, 		// Not needed
										final Vector2 _dim,			// Not needed
										final Vector2 _fill,		// Not needed
										final Vector2 _clip,		// Not needed
										final Vector2 _clipOffset,	// Not needed
										final int _layer )
	{
		final Settings settings = new Settings() ;
		settings.addInteger( "REQUEST_TYPE", AnimRequestType.CREATE_ANIMATION ) ;

		if( _file != null ) { settings.addString( "ANIM_FILE", _file ) ; }
		settings.addObject( "RENDER_EVENT", DrawFactory.createTexture( null,
																	 _pos, _offset,
																	 _dim, _fill, 
																	 _clip, _clipOffset, _layer ) ) ;

		return new Event( "ANIMATION", settings ) ;
	}
}