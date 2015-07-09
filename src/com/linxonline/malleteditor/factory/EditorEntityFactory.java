package com.linxonline.malleteditor.factory ;

import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.util.factory.EntityFactory ;
import com.linxonline.mallet.util.settings.Settings ;

public class EditorEntityFactory extends EntityFactory
{
	private final static String DEFAULT_EDITOR_CREATOR = "EDITOR" ;

	public EditorEntityFactory() {}

	@Override
	public Entity create( final String _type, final Settings _setting )
	{
		if( exists( _type ) == true )
		{
			return ( Entity )creators.get( _type ).create( _setting ) ;
		}
		else
		{
			return ( Entity )creators.get( DEFAULT_EDITOR_CREATOR ).create( _setting ) ;
		}
	}
}