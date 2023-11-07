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

	/**
		Return all entities that have a component
		that represents the passed in absolute name.
	*/
	public List<JSEntity> getEntitiesByAbsoluteComponentName( final String _name )
	{
		final List<JSEntity> fill = MalletList.<JSEntity>newList( entities.size() ) ;

		entities.clear() ;
		system.getEntities( entities ) ;

		final int size = entities.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Entity entity = entities.get( i ) ;
			final JSEntity jsEntity = new JSEntity( entity ) ;
			if( jsEntity.hasComponentByAbsoluteName( _name ) )
			{
				fill.add( jsEntity ) ;
			}
		}

		return fill ;
	}

	/**
		Return all entities that have a component
		that represents the passed in simple-name.
		It's possible for a simple-name to be used by
		more than one component, if you want to grab only
		components of a specific type then use the absolute-name.
	*/
	public List<JSEntity> getEntitiesBySimpleComponentName( final String _name )
	{
		final List<JSEntity> fill = MalletList.<JSEntity>newList( entities.size() ) ;

		entities.clear() ;
		system.getEntities( entities ) ;

		final int size = entities.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Entity entity = entities.get( i ) ;
			final JSEntity jsEntity = new JSEntity( entity ) ;
			if( jsEntity.hasComponentBySimpleName( _name ) )
			{
				fill.add( jsEntity ) ;
			}
		}

		return fill ;
	}

	/**
		Return a list of all currently available entities
		within the game-state.
	*/
	public List<JSEntity> getEntities()
	{
		final List<JSEntity> fill = MalletList.<JSEntity>newList( entities.size() ) ;

		entities.clear() ;
		system.getEntities( entities ) ;

		final int size = entities.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Entity entity = entities.get( i ) ;
			fill.add( new JSEntity( entity ) ) ;
		}

		return fill ;
	}
}
