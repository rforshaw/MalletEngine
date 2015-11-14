package com.linxonline.mallet.renderer.web.gl ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

public class GLModelGenerator
{
	public static Model genShapeModel( Shape _shape )
	{
		final GLGeometryUploader.GLGeometry geometry = GLGeometryUploader.construct( _shape ) ;
		return new Model( geometry ) ;
	}
}