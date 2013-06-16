package com.linxonline.mallet.resources.gl ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.resources.gl.* ;
import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.maths.* ;

public class GLModelGenerator
{
	private final static GLModelManager models = new GLModelManager() ;

	public static Model genPlaneModel( final String _name, final Vector2 _dim )
	{
		return genPlaneModel( _name, _dim, new Vector2( 1.0f, 1.0f ) ) ;
	}

	public static Model genPlaneModel( final String _name, final Vector2 _dim, final Vector2 _uv )
	{
		Model model = ( Model )models.get( _name ) ;
		if( model != null )
		{
			return model ;
		}

		GLGeometry geometry = new GLGeometry( 6, 4 ) ;
		geometry.addVertex( new Vector3( 0, 0, 0 ),
							new Vector3( 0, 0, 1 ),
							new Vector2( 0, 0 ) ) ;
		geometry.addVertex( new Vector3( _dim.x, 0, 0 ),
							new Vector3( 0, 0, 1 ),
							new Vector2( _uv.x, 0 ) ) ;
		geometry.addVertex( new Vector3( 0, _dim.y, 0 ),
							new Vector3( 0, 0, 1 ),
							new Vector2( 0, _uv.y ) ) ;
		geometry.addVertex( new Vector3( _dim.x, _dim.y, 0 ),
							new Vector3( 0, 0, 1 ),
							new Vector2( _uv.x, _uv.y ) ) ;

		geometry.addIndices( 0 ) ;
		geometry.addIndices( 1 ) ;
		geometry.addIndices( 2 ) ;
		geometry.addIndices( 2 ) ;
		geometry.addIndices( 1 ) ;
		geometry.addIndices( 3 ) ;
		
		model = new Model( geometry ) ;
		models.bind( geometry ) ;
		models.add( _name, model ) ;

		model.register() ;
		return model ;
	}
}