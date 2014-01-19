package com.linxonline.malleteditor.system ;

import com.linxonline.mallet.entity.Entity ;

public class EntityCell
{
	private final Entity entity ;

	public EntityCell( final Entity _entity )
	{
		entity = _entity ;
	}

	public Entity getEntity()
	{
		return entity ;
	}

	public String toString()
	{
		return entity.getName() ;
	}
}