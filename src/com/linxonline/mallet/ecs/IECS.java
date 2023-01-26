package com.linxonline.mallet.ecs ;

import com.linxonline.mallet.core.GameState ;

/**
	An implementation of the IECS is where the
	heavy-lifting takes place.
	An implementation should and must provide a create()
	function for creating a ECSEntity.Component that is hooked
	into that component-system.
*/
public interface IECS<T> extends GameState.IUpdate
{
	/**
		A component is associated with one entity and only
		one entity, this is enforced with Component being
		an inner class of the ECSEntity class.
		Note: A developer can provide more than one create()
		function, this create() can effectively be considered like
		a default constructor.
	*/
	public T create( final ECSEntity _parent ) ;

	/**
		Once a component is removed it cannot be reused
		for another purpose within the implementation.
		A developer can reuse the entity the component
		was constructed for, but once that entity is destroyed
		it is destroyed for good.
	*/
	public void remove( final T _component ) ;
}
