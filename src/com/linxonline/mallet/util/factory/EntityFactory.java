package com.linxonline.mallet.util.factory ;

import java.util.ArrayList ;

//import com.linxonline.mallet.factory.Factory ;
import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.io.reader.RFReader ;

/**
	Assumes the Entities are defined in the RF Format.
	Assumes each Creator also parses the Settings object
	passed to it.
**/
public class EntityFactory extends Factory<Entity>
{
	/**public void  addCreator()**/
	/**public boolean  removeCreator()**/

	public ArrayList<Entity> create( final String _file )
	{
		return createEntities( RFReader.loadFile( _file ) ) ;
	}

	public ArrayList<Entity> createEntities( final ArrayList<Settings> _file )
	{
		final ArrayList<Entity> entities = new ArrayList<Entity>() ;
		final int length = _file.size() ;

		for( int i = 0; i < length; ++i )
		{
			final Object obj = create( _file.get( i ) ) ;
			if( obj instanceof ArrayList )
			{
				entities.addAll( ( ArrayList<Entity> )obj ) ;
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
		final Entity entity = create( _entity ) ;
		return entity != null ? entity : new Entity() ;
	}
}
