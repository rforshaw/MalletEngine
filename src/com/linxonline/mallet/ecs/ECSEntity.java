package com.linxonline.mallet.ecs ;

/**
	An ECSEntity represents a continaer of components that
	have a mutual association with each other.
	The entity itself does nothing other than clearing up
	the components once destroy() has been called on the entity.

	The container can also be used to save the state of all components
	allowing for the entity to be recreated.
*/
public final class ECSEntity
{
	private final static ISave SAVE_FALLBACK = new ISave()
	{
		@Override
		public void save( final ECSEntity _toSave ) {}
	} ;

	private final Component[] components ;
	private final IDestroy destroy ;
	private final ISave save ;

	private boolean dead = false ;

	public <T> ECSEntity( final ICreate<? super T> _create, final IDestroy _destroy )
	{
		this( _create, _destroy, null, null ) ;
	}

	public <T> ECSEntity( final ICreate<? super T> _create, final IDestroy _destroy, final T _data )
	{
		this( _create, _destroy, null, _data ) ;
	}

	public <T> ECSEntity( final ICreate<? super T> _create, final IDestroy _destroy, final ISave _save, final T _data )
	{
		components = _create.create( this, _data ) ;
		destroy = _destroy ;
		save = (_save != null) ? _save : SAVE_FALLBACK ;
	}

	/**
		Call save to run the save operation associated with this entity.
	*/
	public void save()
	{
		if( dead == true )
		{
			// Don't trigger the save operation if the entity
			// is flagged for death.
			return ;
		}

		save.save( this ) ;
	}

	public void destroy()
	{
		if( dead == true )
		{
			// We don't want to run through the destruction
			// process multiple times.
			return ;
		}

		dead = true ;
		destroy.destroy( components ) ;
	}

	/**
		Return true if the entity has been destroyed.
		Return false if destroy() has yet to be called.
	*/
	public boolean isDead()
	{
		return dead ;
	}

	public final <T extends Component> T getComponentByType( final Class<T> _clazz )
	{
		final int size = components.length ;
		for( int i = 0; i < size; ++i )
		{
			final Component component = components[i] ;
			if( _clazz.isInstance( component ) == true )
			{
				return _clazz.cast( component ) ;
			}
		}

		return null ;
	}

	/**
		The implementation is left mostly to the developer.

		I would recommend populating whatever data structure
		was used to create the object, then you can decide
		how you wish to write that data-structure out in whatever
		format is most appropriate for your circumstances.

		You can implement one save implementation for all
		your entities, or you can implement a save implementation
		on each type of entity you create.

		Overall I'd suggest using a MemoryPool to grab the data-structure,
		populate it with the required data, and then stick it in a queue so
		it can start to be written out, once written the data-structure
		can be returned to be used for another entity.
	*/
	public interface ISave
	{
		public void save( final ECSEntity _toSave ) ;
	}

	/**
		Create the components that will define an ECSEntity.
		Components should be provided by an implementation
		of IComponentSystem.
		If a component requires data from another component
		then how that is done should be decided here.
	*/
	public interface ICreate<T>
	{
		public Component[] create( final ECSEntity _parent, final T _data ) ;
	}

	/**
		Destroy the components that define an entity.
		A Component should be returned to the ComponentSystem
		that created it.
	*/
	public interface IDestroy
	{
		public void destroy( final Component[] _components ) ;
	}

	/**
		Extend the Component to store data that is used by
		an implementation of the IComponentSystem.
	*/
	public abstract class Component
	{
		public final ECSEntity getParent()
		{
			return ECSEntity.this ; 
		}
	}
}
