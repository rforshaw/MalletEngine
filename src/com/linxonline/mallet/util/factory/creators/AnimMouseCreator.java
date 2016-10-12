package com.linxonline.mallet.util.factory.creators ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.factory.* ;

import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.animation.AnimationAssist ;
import com.linxonline.mallet.animation.Anim ;

import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Interpolation ;
import com.linxonline.mallet.renderer.UpdateType ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.entity.components.* ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.event.* ;

public class AnimMouseCreator extends Creator<Entity, Settings>
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

		final AnimComponent anim = new AnimComponent() ;
		final EventComponent event = new EventComponent() ;
		final MouseComponent mouse = new MouseComponent() ;

		final Anim animation = AnimationAssist.createAnimation( _mouse.getString( "ANIM", null ),
																entity.position,
																_mouse.<Vector3>getObject( "OFFSET", null ),
																new Vector3(),
																new Vector3( 1, 1, 1 ),
																_mouse.getInteger( "LAYER", 100 ) ) ;

		final Vector2 dim = _mouse.<Vector2>getObject( "DIM", null ) ;
		DrawAssist.amendShape( AnimationAssist.getDraw( animation ), Shape.constructPlane( new Vector3( dim.x, dim.y, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ) ;
		DrawAssist.amendInterpolation( AnimationAssist.getDraw( animation ), Interpolation.LINEAR ) ;
		DrawAssist.amendUpdateType( AnimationAssist.getDraw( animation ), UpdateType.ON_DEMAND ) ;

		anim.addAnimation( "DEFAULT", animation ) ;
		anim.setDefaultAnim( "DEFAULT" ) ;

		entity.addComponent( anim ) ;
		entity.addComponent( mouse ) ;
		entity.addComponent( event ) ;

		return entity ;
	}
}
