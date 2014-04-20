package com.linxonline.mallet.util.factory.creators ;

import com.linxonline.mallet.animation.AnimationFactory ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.system.GlobalConfig ;
import com.linxonline.mallet.util.factory.* ;
import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.entity.components.* ;
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
																		_mouse.<Vector2>getObject( "OFFSET", null ),
																		_mouse.<Vector2>getObject( "DIM", null ),
																		_mouse.<Vector2>getObject( "FILL", null ), 
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