package com.linxonline.mallet.renderer.opengl ;

import java.util.Set ;

import com.linxonline.mallet.renderer.DrawData ;
import com.linxonline.mallet.renderer.CameraData ;
import com.linxonline.mallet.renderer.BasicWorld ;

public abstract class World<D extends DrawData,
							C extends CameraData> extends BasicWorld<D, C>
{
	public World( final String _id, final int _order )
	{
		super( _id, _order ) ;
	}

	public abstract void init() ;

	public abstract void clean( final Set<String> _activeKeys ) ;

	public abstract void shutdown() ;
}
