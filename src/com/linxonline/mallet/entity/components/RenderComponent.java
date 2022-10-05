package com.linxonline.mallet.entity.components ;

import com.linxonline.mallet.entity.Entity ;

public class RenderComponent extends Component
{
	public RenderComponent( final Entity _parent )
	{
		this( _parent, Entity.AllowEvents.YES ) ;
	}

	public RenderComponent( final Entity _parent, final Entity.AllowEvents _allow )
	{
		super( _parent, _allow ) ;
		init() ;
	}

	/**
		Override when you wish to construct a set of 
		draw objects and add them directly to the 
		component without revealing them to others.
	*/
	public void init() {}

	public void shutdown() {}

	@Override
	public void readyToDestroy( final Entity.ReadyCallback _callback )
	{
		shutdown() ;
		super.readyToDestroy( _callback ) ;
	}
}
