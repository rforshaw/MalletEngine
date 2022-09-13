package com.linxonline.mallet.script.javascript ;

import java.util.List ;

import com.linxonline.mallet.entity.IEntitySystem ;
import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.util.MalletList ;

public final class JSEntitySystem
{
	private final IEntitySystem system ;
	private final List<Entity> entities = MalletList.<Entity>newList() ;

	public JSEntitySystem( final IEntitySystem _system )
	{
		system = _system ;
	}

	public List<JSEntity> getEntities()
	{
		final List<JSEntity> fill = MalletList.<JSEntity>newList( entities.size() ) ;

		entities.clear() ;
		system.getEntities( entities ) ;
		for( final Entity entity : entities )
		{
			fill.add( new JSEntity( entity ) ) ;
		}

		return fill ;
	}
}
