package com.linxonline.mallet.entity.components ;

import com.linxonline.mallet.entity.Entity ;

public class Component extends Entity.Component
{
	public Component( final Entity _parent )
	{
		this( _parent, null, null ) ;
	}

	public Component( final Entity _parent, final String _name, final String _group )
	{
		_parent.super( _name, _group ) ;
	}
}
