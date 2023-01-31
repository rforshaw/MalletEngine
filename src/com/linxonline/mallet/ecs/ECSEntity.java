package com.linxonline.mallet.ecs ;

/**
	An ECSEntity represents a continaer of components that
	have a mutual association with each other.
	The entity itself does nothing other than clearing up
	the components once destroy() has been called on the entity.
*/
public final class ECSEntity
{
	private final Component[] components ;
	private final IDestroy destroy ;

	private boolean dead = false ;

	public <T> ECSEntity( final ICreate _create, final IDestroy _destroy )
	{
		this( _create, _destroy, null ) ;
	}

	public <T> ECSEntity( final ICreate<T> _create, final IDestroy _destroy, final T _data )
	{
		components = _create.create( this, _data ) ;
		destroy = _destroy ;
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
		for( final Component component : components )
		{
			if( _clazz.isInstance( component ) == true )
			{
				return _clazz.cast( component ) ;
			}
		}

		return null ;
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
