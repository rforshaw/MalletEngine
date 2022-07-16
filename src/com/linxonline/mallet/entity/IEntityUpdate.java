package com.linxonline.mallet.entity ;

import java.util.List ;

/**
	See DefaultSTUpdate and DefaultMTUpdate as examples.
*/
public interface IEntityUpdate
{
	/**
		Loop over the _update entities in whichever
		way you see fit. Add any entity's flagged as destroyed
		to the _cleanup list.
	*/
	public void update( final float _dt, final List<Entity> _update, final List<Entity> _cleanup ) ;
}
