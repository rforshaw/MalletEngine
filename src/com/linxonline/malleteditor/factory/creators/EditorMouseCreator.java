package com.linxonline.malleteditor.factory.creators ;

import com.linxonline.mallet.renderer.CameraFactory ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.system.GlobalConfig ;
import com.linxonline.mallet.util.factory.* ;

import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.entity.components.* ;
import com.linxonline.mallet.maths.* ;

import com.linxonline.malleteditor.entity.EditorMouseComponent ;

public class EditorMouseCreator extends Creator<Entity>
{
	public EditorMouseCreator()
	{
		setType( "EDITOR_MOUSE" ) ;
	}

	@Override
	public Entity create( final Settings _mouse )
	{
		final int width = GlobalConfig.getInteger( "RENDERWIDTH", 0 ) / 2 ;
		final int height = GlobalConfig.getInteger( "RENDERHEIGHT", 0 ) / 2 ;

		final Entity entity = new Entity( "MOUSE" ) ;
		entity.position = new Vector3( width, height, 0 ) ;

		final EventComponent event = new EventComponent() ;
		final RenderComponent render = new RenderComponent() ;
		final EditorMouseComponent mouse = new EditorMouseComponent( render ) ;

		entity.addComponent( mouse ) ;
		entity.addComponent( event ) ;
		entity.addComponent( render ) ;

		return entity ;
	}
}