package com.linxonline.malleteditor.factory ;

import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.util.factory.EntityFactory ;
import com.linxonline.mallet.util.settings.Settings ;

public class EditorEntityFactory extends EntityFactory
{
	private final static String DEFAULT_EDITOR_CREATOR = "EDITOR" ;

	public EditorEntityFactory() {}

	@Override
	public Entity create( final Settings _setting )
	{
		final String type = _setting.getString( TYPE, null ) ;
		if( type != null )
		{
			if( exists( type ) == true )
			{
				return ( Entity )creators.get( type ).create( _setting ) ;
			}
			else
			{
				return ( Entity )creators.get( DEFAULT_EDITOR_CREATOR ).create( _setting ) ;
			}
		}

		System.out.println( "Failed to create object of type: " + type ) ;
		return null ;
	}
}