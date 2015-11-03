package com.linxonline.mallet.renderer.android.GL ;

import android.opengl.GLES11 ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.util.tools.ConvertBytes ;

public class GLModelGenerator
{
	private final static GLModelManager models = new GLModelManager() ;

	public static void clean()
	{
		models.clean() ;
	}

	public static void shutdown()
	{
		models.shutdown() ;
	}
	
	public static Model genPlaneModel( final String _name, final Vector2 _dim )
	{
		return genPlaneModel( _name, _dim, new Vector2( 0.0f, 0.0f ), new Vector2( 1.0f, 1.0f ) ) ;
	}

	/**
		Create a Quad made from 2 triangles.
		Added to ModelManager and registered.
	*/
	public static Model genPlaneModel( final String _name, final Vector2 _dim, final Vector2 _uv1, final Vector2 _uv2 )
	{
		// See if the model already exists
		final Model m = ( Model )models.get( _name ) ;
		if( m != null ) 
		{
			return m ;
		}

		// Generate the plane, & register it.
		final Model model = GLModelGenerator.genPlaneModel( _dim, _uv1, _uv2 ) ;
		models.add( _name, model ) ;
		model.register() ;
		return model ;
	}

	/**
		Create a Quad made from 2 triangles.
		NOT added to ModelManager or registered.
	*/
	public static Model genPlaneModel( final Vector2 _dim, final Vector2 _uv1, final Vector2 _uv2 )
	{
		//final GLGeometry geometry = new GLGeometry( 6, 4 ) ;
		/*geometry.addVertex( new Vector3( 0, 0, 0 ),
							new Vector2( _uv1.x, _uv1.y ) ) ;
		geometry.addVertex( new Vector3( _dim.x, 0, 0 ),
							new Vector2( _uv2.x, _uv1.y ) ) ;
		geometry.addVertex( new Vector3( 0, _dim.y, 0 ),
							new Vector2( _uv1.x, _uv2.y ) ) ;
		geometry.addVertex( new Vector3( _dim.x, _dim.y, 0 ),
							new Vector2( _uv2.x, _uv2.y ) ) ;

		geometry.addIndices( 0 ) ;
		geometry.addIndices( 2 ) ;
		geometry.addIndices( 3 ) ;
		geometry.addIndices( 0 ) ;
		geometry.addIndices( 3 ) ;
		geometry.addIndices( 1 ) ;*/

		//final Model model = new Model( geometry ) ;
		//models.bind( geometry ) ;
		return null ;//model ;
	}

	/**
		Create a Geometric Line
	*/
	public static Model genShapeModel( final String _name, final Shape _line )
	{
		// See if the model already exists
		final Model m = ( Model )models.get( _name ) ;
		if( m != null ) { return m ; }

		// Generate the line, & register it.
		final Model model = GLModelGenerator.genShapeModel( _line ) ;
		models.add( _name, model ) ;
		model.register() ;
		return model ;
	}

	public static Model genShapeModel( Shape _shape )
	{
		final GLGeometry geometry = GLGeometry.construct( _shape ) ;
		final Model model = new Model( geometry ) ;

		models.bind( geometry ) ;
		return model ;
	}

	public static float getABGR( final MalletColour _colour )
	{
		final byte[] colour = new byte[4] ;
		colour[0] = _colour.colours[MalletColour.ALPHA] ;
		colour[1] = _colour.colours[MalletColour.BLUE] ;
		colour[2] = _colour.colours[MalletColour.GREEN] ;
		colour[3] = _colour.colours[MalletColour.RED] ;

		return ConvertBytes.toFloat( colour, 0, 4 ) ;
	}
}