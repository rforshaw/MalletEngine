package com.linxonline.mallet.util.factory.creators ;

import com.linxonline.mallet.resources.ResourceManager ;
import com.linxonline.mallet.renderer.DrawFactory ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.factory.* ;
import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.maths.* ;

public class AnimMouseCreator extends Creator
{
	private static final String ANIMMOUSE = "ANIMMOUSE" ;
	private static final String MOUSE = "MOUSE" ;
	private static final String DEFAULT = "DEFAULT" ;
	private static final String LAYER = "LAYER" ;
	private static final String OFFSET = "OFFSET" ;
	private static final String FILL = "FILL" ;
	private static final String DIM = "DIM" ;
	
	public AnimMouseCreator()
	{
		setType( ANIMMOUSE ) ;
	}

	@Override
	public Object create( final Settings _mouse )
	{
		final ResourceManager resources = ResourceManager.getResourceManager() ;
		final Settings config = resources.getConfig() ;
		final int width = config.getInteger( "RENDERWIDTH", 0 ) / 2 ;
		final int height = config.getInteger( "RENDERHEIGHT", 0 ) / 2 ;

		final Entity entity = new Entity( "MOUSE" ) ;
		entity.position = new Vector3( width, height, 0 ) ;

		SpriteComponent sprite = new SpriteComponent() ;
		EventComponent event = new EventComponent() ;
		MouseComponent mouse = new MouseComponent() ;

		sprite.add( DrawFactory.createTexture( null,
												entity.position,
											   _mouse.getObject( OFFSET, Vector2.class, null ),
											   _mouse.getObject( DIM, Vector2.class, null ),
											   _mouse.getObject( FILL, Vector2.class, null ), 
											    null,		// Clip View 
											    null,		// Clip Offset
											   _mouse.getInteger( LAYER, 100 ) ) ) ;

		sprite.addSprite( resources.getSprite( _mouse.getString( MOUSE, null ) ), DEFAULT ) ;
		sprite.setSpriteByName( DEFAULT ) ;

		entity.addComponent( sprite ) ;
		entity.addComponent( mouse ) ;
		entity.addComponent( event ) ;

		return entity ;
	}
}