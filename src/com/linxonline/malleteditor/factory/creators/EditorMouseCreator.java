package com.linxonline.malleteditor.factory.creators ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.system.GlobalConfig ;
import com.linxonline.mallet.util.factory.* ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.maths.* ;


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
		final MouseComponent mouse = new MouseComponent() ;

		entity.addComponent( mouse ) ;
		entity.addComponent( event ) ;

		return entity ;
	}
}