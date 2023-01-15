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

	public List<JSEntity> getEntitiesByAbsoluteComponentName( final String _name )
	{
		final List<JSEntity> fill = MalletList.<JSEntity>newList( entities.size() ) ;

		entities.clear() ;
		system.getEntities( entities ) ;
		for( final Entity entity : entities )
		{
			final JSEntity jsEntity = new JSEntity( entity ) ;
			if( jsEntity.hasComponentByAbsoluteName( _name ) )
			{
				fill.add( jsEntity ) ;
			}
		}

		return fill ;
	}

	public List<JSEntity> getEntitiesBySimpleComponentName( final String _name )
	{
		final List<JSEntity> fill = MalletList.<JSEntity>newList( entities.size() ) ;

		entities.clear() ;
		system.getEntities( entities ) ;
		for( final Entity entity : entities )
		{
			final JSEntity jsEntity = new JSEntity( entity ) ;
			if( jsEntity.hasComponentBySimpleName( _name ) )
			{
				fill.add( jsEntity ) ;
			}
		}

		return fill ;
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
