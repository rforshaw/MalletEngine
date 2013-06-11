package com.linxonline.mallet.entity ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.settings.* ;

import com.linxonline.mallet.resources.* ;
//import com.linxonline.mallet.resources.gl.* ;
import com.linxonline.mallet.resources.texture.* ;

public final class EntityFactory
{
	public EntityFactory() {}

	/*public static Entity createEntity( final String _name, 
									   final String _texture, 
									   final Vector2 _position, 
									   final int _layer, 
									   final Vector2 _offset )
	{
		ResourceManager resources = ResourceManager.getResourceManager() ;
		Entity entity = new Entity( _name ) ;
		entity.position = new Vector3( _position ) ;

		RenderComponent render = new RenderComponent() ;
		EventComponent event = new EventComponent() ;

		render.container.enableType( RenderContainer.MODEL_TYPE ) ;
		render.container.settings.addObject( "OFFSET", _offset ) ;
		render.container.settings.addInteger( "LAYER", _layer ) ;
		render.container.settings.addObject( "TEXTURE", resources.getTexture( _texture ) ) ;
		render.container.position = entity.position ;

		entity.addComponent( render ) ;
		entity.addComponent( event ) ;

		return entity ;
	}

	public static Entity createAnimatedEntity( final String _name, 
											  final String _sprite, 
											  final Vector2 _position, 
											  final int _layer, 
											  final Vector2 _offset )
	{
		ResourceManager resources = ResourceManager.getResourceManager() ;
		Entity entity = new Entity( _name ) ;
		entity.position = new Vector3( _position ) ;

		SpriteComponent sprite = new SpriteComponent() ;
		EventComponent event = new EventComponent() ;
		MouseComponent mouse = new MouseComponent() ;

		sprite.container.enableType( RenderContainer.MODEL_TYPE ) ;
		sprite.container.settings.addInteger( "LAYER", 100 ) ;
		sprite.container.position = entity.position ;
		sprite.container.settings.addObject( "OFFSET", _offset ) ;
		sprite.addSprite( resources.getSprite( _sprite ), "DEFAULT" ) ;
		sprite.setSpriteByName( "DEFAULT" ) ;

		entity.addComponent( sprite ) ;
		entity.addComponent( mouse ) ;
		entity.addComponent( event ) ;

		return entity ;
	}
	
	public static Entity createCamera( final Vector2 _position )
	{
		Entity entity = new Entity( "CAMERA" ) ;
		entity.position = new Vector3( _position ) ;

		EventComponent event = new EventComponent() ;
		CameraInputComponent cameraInput = new CameraInputComponent() ;

		entity.addComponent( cameraInput ) ;
		entity.addComponent( event ) ;

		return entity ;
	}

	public static Entity createMouse( final String _texture, final Entity _camera )
	{
		ResourceManager resources = ResourceManager.getResourceManager() ;
		Entity entity = new Entity( "MOUSE" ) ;
		entity.position = new Vector3( 0, 0, 0 ) ;

		RenderComponent render = new RenderComponent() ;
		EventComponent event = new EventComponent() ;
		MouseComponent mouse = new MouseComponent() ;

		render.container.enableType( RenderContainer.MODEL_TYPE ) ;
		render.container.settings.addInteger( "LAYER", 100 ) ;
		//render.container.settings.addObject( "MODEL", GLModelGenerator.genPlaneModel( "MOUSE_POINTER", 16, 16 ) ) ;
		render.container.settings.addObject( "TEXTURE", resources.getTexture( _texture ) ) ;
		render.container.position = entity.position ;

		entity.addComponent( render ) ;
		entity.addComponent( mouse ) ;
		entity.addComponent( event ) ;

		return entity ;
	}
	
	public static Entity createAnimatedMouse( final String _sprite, final Entity _camera )
	{
		ResourceManager resources = ResourceManager.getResourceManager() ;
		Entity entity = new Entity( "MOUSE" ) ;
		entity.position = new Vector3( 400, 300, 0 ) ;

		try
		{
			Settings config = resources.getConfig() ;
			int widthHalf = config.getInteger( "RENDERWIDTH" ) / 2 ;
			int heightHalf = config.getInteger( "RENDERHEIGHT" ) / 2 ;
			entity.position = new Vector3( widthHalf, heightHalf, 0 ) ;
		}
		catch( Exception _ex ) {}

		SpriteComponent sprite = new SpriteComponent() ;
		EventComponent event = new EventComponent() ;
		MouseComponent mouse = new MouseComponent() ;

		sprite.container.enableType( RenderContainer.MODEL_TYPE ) ;
		sprite.container.settings.addInteger( "LAYER", 100 ) ;
		sprite.container.position = entity.position ;
		sprite.addSprite( resources.getSprite( _sprite ), "DEFAULT" ) ;
		sprite.setSpriteByName( "DEFAULT" ) ;

		entity.addComponent( sprite ) ;
		entity.addComponent( mouse ) ;
		entity.addComponent( event ) ;

		return entity ;
	}

	public static Entity createAnimatedEntity( final String _name, 
											   final String _sprite, 
											   final Vector2 _position, 
											   final int _layer, 
											   final int _alignment )
	{
		ResourceManager resources = ResourceManager.getResourceManager() ;
		Entity entity = new Entity( _name ) ;
		entity.position = new Vector3( _position ) ;

		SpriteComponent sprite = new SpriteComponent() ;
		EventComponent event = new EventComponent() ;

		sprite.container.enableType( RenderContainer.MODEL_TYPE ) ;
		sprite.container.settings.addInteger( "ALIGNMENT", _alignment ) ;
		sprite.container.settings.addInteger( "LAYER", _layer ) ;
		sprite.container.position = entity.position ;
		sprite.addSprite( resources.getSprite( _sprite ), "DEFAULT" ) ;
		sprite.setSpriteByName( "DEFAULT" ) ;

		entity.addComponent( sprite ) ;
		entity.addComponent( event ) ;

		return entity ;
	}*/

	/*public static Entity createTextAreaEntity( final String _name, 
											   final String _texture, 
											   final Vector2 _position,
											   final Vector2 _offset,
											   final Vector2 _textOffset,
											   final MalletColour _colour,
											   final int _layer, 
											   final boolean _isProtected )
	{*/
		/*ResourceManager resources = ResourceManager.getResourceManager() ;
		Entity entity = new Entity( _name ) ;
		entity.position = new Vector3( _position ) ;
		
		Texture texture = resources.getTexture( _texture ) ;
		int width = texture.width ;
		int height = texture.height ;

		RenderComponent renderBack = new RenderComponent() ;
		renderBack.container.enableType( RenderContainer.MODEL_TYPE ) ;
		renderBack.container.settings.addInteger( "LAYER", _layer ) ;
		renderBack.container.settings.addObject( "TEXTURE", texture ) ;
		renderBack.container.settings.addObject( "OFFSET", _offset ) ;
		renderBack.container.position = entity.position ;

		RenderComponent renderText = new RenderComponent() ;
		renderText.container.enableType( RenderContainer.TEXT_TYPE ) ;
		renderText.container.enableType( RenderContainer.GEOMETRIC_TYPE ) ;
		renderText.container.settings.addInteger( "LAYER", _layer + 1 ) ;
		renderText.container.settings.addString( "TEXT", "Type Here..." ) ;
		renderText.container.settings.addObject( "OFFSET", _textOffset ) ;
		renderText.container.settings.addObject( "COLOUR", _colour ) ;
		renderText.container.position = entity.position ;

		EventComponent event = new EventComponent() ;
		
		TextAreaComponent text = new TextAreaComponent() ;
		text.setProtected( _isProtected ) ;
		text.dimensions = new Vector2( width, height ) ;
		text.setRenderComponent( renderText ) ;
		text.setEventComponent( event ) ;

		entity.addComponent( event ) ;
		entity.addComponent( renderBack ) ;
		entity.addComponent( renderText ) ;
		entity.addComponent( text ) ;

		return entity ;*/
	//	return null ;
	//}
}
