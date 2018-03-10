package com.linxonline.mallet.entity ;

import java.util.List ;

public interface IEntityUpdate
{
	public void update( final float _dt, final List<Entity> _update, final List<Entity> _cleanup ) ;
}
