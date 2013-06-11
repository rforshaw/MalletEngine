package com.linxonline.mallet.resources.gl ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.resources.gl.* ;
import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.maths.* ;

public class GLModelGenerator
{
	public static Model genPlaneModel( final String _name, 
									   final int _width, 
									   final int _height )
	{
		/*ResourceManager resources = ResourceManager.getResourceManager() ;
		Model model = resources.getModel( _name ) ;
		if( model != null )
		{
			return model ;
		}

		GLGeometry geometry = new GLGeometry( 6, 4 ) ;
		geometry.addVertex( new Vector3( 0, 0, 0 ),
							new Vector3( 0, 0, 1 ),
							new Vector2( 0, 0 ) ) ;
		geometry.addVertex( new Vector3( _width, 0, 0 ),
							new Vector3( 0, 0, 1 ),
							new Vector2( 1, 0 ) ) ;
		geometry.addVertex( new Vector3( 0, _height, 0 ),
							new Vector3( 0, 0, 1 ),
							new Vector2( 0, 1 ) ) ;
		geometry.addVertex( new Vector3( _width, _height, 0 ),
							new Vector3( 0, 0, 1 ),
							new Vector2( 1, 1 ) ) ;

		geometry.addIndices( 0 ) ;
		geometry.addIndices( 1 ) ;
		geometry.addIndices( 2 ) ;
		geometry.addIndices( 2 ) ;
		geometry.addIndices( 1 ) ;
		geometry.addIndices( 3 ) ;
		
		model = new Model( geometry ) ;

		GLModelManager modelManager = ( GLModelManager )resources.modelManager ;
		if( modelManager != null )
		{
			modelManager.bind( geometry ) ;
			modelManager.add( _name, model ) ;
		}

		model.register() ;
		return model ;*/
		return null ;
	}
}