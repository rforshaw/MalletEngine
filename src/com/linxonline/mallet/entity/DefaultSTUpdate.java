package com.linxonline.mallet.entity ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

/**
	Update the entities one at a time on the calling thread.
*/
public class DefaultSTUpdate implements IEntityUpdate
{
	public DefaultSTUpdate() {}

	@Override
	public void update( final float _dt, final List<Entity> _update, final List<Entity> _cleanup )
	{
		Entity entity = null ;
		final int entitySize = _update.size() ;
		for( int i = 0; i < entitySize; ++i )
		{
			entity = _update.get( i ) ;
			entity.update( _dt ) ;

			if( entity.isDead() == true )
			{
				_cleanup.add( entity ) ;
			}
		}
	}
}
