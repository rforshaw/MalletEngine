package com.linxonline.mallet.util.factory.creators ;

import com.linxonline.mallet.animation.AnimationFactory ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.system.GlobalConfig ;
import com.linxonline.mallet.util.factory.* ;
import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.maths.* ;

public class AnimMouseCreator extends Creator<Entity>
{
	public AnimMouseCreator()
	{
		setType( "ANIMMOUSE" ) ;
	}

	@Override
	public Entity create( final Settings _mouse )
	{
		final int width = GlobalConfig.getInteger( "RENDERWIDTH", 0 ) / 2 ;
		final int height = GlobalConfig.getInteger( "RENDERHEIGHT", 0 ) / 2 ;

		final Entity entity = new Entity( "MOUSE" ) ;
		entity.position = new Vector3( width, height, 0 ) ;

		AnimComponent anim = new AnimComponent() ;
		EventComponent event = new EventComponent() ;
		MouseComponent mouse = new MouseComponent() ;

		anim.addAnimation( "DEFAULT", AnimationFactory.createAnimation( _mouse.getString( "ANIM", null ),
																		 entity.position,
																		_mouse.getObject( "OFFSET", Vector2.class, null ),
																		_mouse.getObject( "DIM", Vector2.class, null ),
																		_mouse.getObject( "FILL", Vector2.class, null ), 
																		 null,		// Clip View 
																		 null,		// Clip Offset
																		_mouse.getInteger( "LAYER", 100 ), anim ) ) ;
		anim.setDefaultAnim( "DEFAULT" ) ;

		entity.addComponent( anim ) ;
		entity.addComponent( mouse ) ;
		entity.addComponent( event ) ;

		return entity ;
	}
}