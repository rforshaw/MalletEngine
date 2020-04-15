package com.linxonline.mallet.entity.components ;

import com.linxonline.mallet.entity.Entity ;

public class Component extends Entity.Component
{
	public Component( final Entity _parent )
	{
		_parent.super() ;
	}

	public Component( final Entity _parent, Entity.AllowEvents _allow )
	{
		_parent.super( _allow ) ;
	}
}
