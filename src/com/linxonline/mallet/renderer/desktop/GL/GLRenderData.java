package com.linxonline.mallet.renderer.desktop.GL ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.util.settings.Settings ;

import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.resources.texture.* ;

public class GLRenderData extends RenderData
{
	public GLRenderData()
	{
		super() ;
	}

	public GLRenderData( final int _id, final int _type,
						 final Settings _draw, final Vector3 _position,
						 final int _layer )
	{
		super( _id, _type, _draw, _position, _layer ) ;
	}

	@Override
	public void unregisterResources()
	{
		final Texture texture = drawData.getObject( "TEXTURE", null ) ;
		if( texture != null )
		{
			texture.unregister() ;
		}

		final Model model = drawData.getObject( "MODEL", null ) ;
		if( model != null )
		{
			model.unregister() ;
			if( type == DrawRequestType.GEOMETRY )
			{
				// Geometry Requests are not stored.
				// So must be destroyed explicity.
				model.destroy() ;
			}
		}
	}
}