package com.linxonline.mallet.util.factory ;

import java.util.List ;

import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.io.reader.RFReader ;
import com.linxonline.mallet.util.Utility ;

/**
	Assumes the Entities are defined in the RF Format.
	Assumes each Creator also parses the Settings object
	passed to it.
**/
public class EntityFactory extends Factory<Entity, Settings>
{
	/**public void  addCreator()**/
	/**public boolean  removeCreator()**/

	public List<Entity> create( final String _file )
	{
		return createEntities( RFReader.loadFile( _file ) ) ;
	}

	public List<Entity> createEntities( final List<Settings> _file )
	{
		final List<Entity> entities = Utility.<Entity>newArrayList() ;
		final int length = _file.size() ;

		for( int i = 0; i < length; ++i )
		{
			final Settings settings = _file.get( i ) ;
			final Object obj = create( settings.getString( "TYPE", "" ), settings ) ;
			if( obj instanceof List )
			{
				entities.addAll( ( List<Entity> )obj ) ;
			}
			else if ( obj instanceof Entity )
			{
				entities.add( ( Entity )obj ) ;
			}
		}

		return entities ;
	}

	public Entity createEntity( final Settings _entity )
	{
		final Entity entity = create( _entity.getString( "TYPE", "" ), _entity ) ;
		return entity != null ? entity : new Entity() ;
	}
}
