package com.linxonline.mallet.renderer.desktop.GL ;

import javax.media.opengl.* ;

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
		final GLGeometry geometry = new GLGeometry( 6, 4 ) ;
		geometry.addVertex( new Vector3( 0, 0, 0 ),
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
		geometry.addIndices( 1 ) ;
		
		final Model model = new Model( geometry ) ;
		models.bind( geometry ) ;
		return model ;
	}

	public static Model updatePlaneModelUV( final Model _model, final Vector2 _uv1, final Vector2 _uv2 )
	{
		final GLGeometry geometry = _model.getGeometry( GLGeometry.class ) ;
		geometry.updateTexCoord( 0, _uv1.x, _uv1.y ) ;
		geometry.updateTexCoord( 1, _uv2.x, _uv1.y ) ;
		geometry.updateTexCoord( 2, _uv1.x, _uv2.y ) ;
		geometry.updateTexCoord( 3, _uv2.x, _uv2.y ) ;

		return _model ;
	}

	public static Model updateModelColour( final Model _model, final float _colour )
	{
		final GLGeometry geometry = _model.getGeometry( GLGeometry.class ) ;
		final int size = geometry.getVertexSize() ;
		for( int i = 0; i < size; i++ )
		{
			geometry.updateColour( i, _colour ) ;
		}

		return _model ;
	}

	public static Model updatePlaneModelColour( final Model _model, final float _colour )
	{
		final GLGeometry geometry = _model.getGeometry( GLGeometry.class ) ;
		updatePlaneColour( geometry, _colour ) ;

		return _model ;
	}

	public static void updatePlaneColour( final GLGeometry _geometry, final float _colour )
	{
		_geometry.updateColour( 0, _colour ) ;
		_geometry.updateColour( 1, _colour ) ;
		_geometry.updateColour( 2, _colour ) ;
		_geometry.updateColour( 3, _colour ) ;
	}

	public static Model updateShapeModel( final Model _model, final Shape _shape )
	{
		final int indexSize = _shape.indicies.length ;
		final int pointSize = _shape.points.length ;

		final GLGeometry geometry = _model.getGeometry( GLGeometry.class ) ;
		if( indexSize >= geometry.getIndexSize() ||
			pointSize >= geometry.getVertexSize() )
		{
			return _model ;
		}

		if( _shape.colours != null )
		{
			for( int i = 0; i < pointSize; ++i )
			{
				geometry.updatePosition( i, _shape.points[i] ) ;
				geometry.updateColour( i, getABGR( _shape.colours[i] ) ) ;
			}
		}
		else
		{
			for( int i = 0; i < pointSize; ++i )
			{
				geometry.updatePosition( i, _shape.points[i] ) ;
			}
		}

		for( int i = 0; i < indexSize; ++i )
		{
			geometry.updateIndex( i, _shape.indicies[i] ) ;
		}

		return _model ;
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

	public static Model genShapeModel( final Shape _shape )
	{
		final int indexSize = _shape.indicies.length ;
		final int pointSize = _shape.points.length ;

		final GLGeometry geometry = new GLGeometry( indexSize, pointSize ) ;
		if( _shape.colours != null )
		{
			for( int i = 0; i < pointSize; ++i )
			{
				geometry.addVertex( _shape.points[i], getABGR( _shape.colours[i] ) ) ;
			}
		}
		else
		{
			for( int i = 0; i < pointSize; ++i )
			{
				geometry.addVertex( _shape.points[i] ) ;
			}
		}

		for( int i = 0; i < indexSize; ++i )
		{
			geometry.addIndices( _shape.indicies[i] ) ;
		}

		switch( _shape.style )
		{
			case LINES      : geometry.setStyle( GL2.GL_LINES ) ;      break ;
			case LINE_STRIP : geometry.setStyle( GL2.GL_LINE_STRIP ) ; break ;
			case FILL       : geometry.setStyle( GL2.GL_TRIANGLES ) ;  break ;
		}

		final Model model = new Model( geometry ) ;
		models.bind( geometry ) ;
		return model ;
	}

	public static Model genCubeModel( final String _name, final Vector2 _dim )
	{
		return genCubeModel( _name, _dim, new Vector2( 0.0f, 0.0f ), new Vector2( 1.0f, 1.0f ) ) ;
	}

	public static Model genCubeModel( final String _name,
									  final Vector2 _dim,
									  final Vector2 _uv1,
									  final Vector2 _uv2 )
	{
		// See if the model already exists
		final Model m = ( Model )models.get( _name ) ;
		if( m != null ) { return m ; }

		// Generate the plane, & register it.
		final Model model = GLModelGenerator.genCubeModel( _dim, _uv1, _uv2 ) ;
		models.add( _name, model ) ;
		model.register() ;
		return model ;
	}

	public static Model genCubeModel( final Vector2 _dim,
									  final Vector2 _uv1,
									  final Vector2 _uv2 )
	{
		final GLGeometry geometry = new GLGeometry( 36, 24 ) ;
		geometry.addVertex( new Vector3( 0, 0, 0 ),					// 0 - 1st Face - Front
							new Vector2( _uv1.x, _uv1.y ) ) ;
		geometry.addVertex( new Vector3( _dim.x, 0, 0 ),			// 1
							new Vector2( _uv2.x, _uv1.y ) ) ;
		geometry.addVertex( new Vector3( 0, _dim.y, 0 ),			// 2
							new Vector2( _uv1.x, _uv2.y ) ) ;
		geometry.addVertex( new Vector3( _dim.x, _dim.y, 0 ),		// 3
							new Vector2( _uv2.x, _uv2.y ) ) ;
		final float z = _dim.x ;
		geometry.addVertex( new Vector3( 0, 0, z ),					// 4 - 2nd Face - Back
							new Vector2( _uv1.x, _uv1.y ) ) ;
		geometry.addVertex( new Vector3( _dim.x, 0, z ),			// 5
							new Vector2( _uv2.x, _uv1.y ) ) ;
		geometry.addVertex( new Vector3( 0, _dim.y, z ),			// 6
							new Vector2( _uv1.x, _uv2.y ) ) ;
		geometry.addVertex( new Vector3( _dim.x, _dim.y, z ),		// 7
							new Vector2( _uv2.x, _uv2.y ) ) ;

		geometry.addVertex( new Vector3( 0, 0, 0 ),					// 8 - 3rd Face - Top
							new Vector2( _uv1.x, _uv1.y ) ) ;
		geometry.addVertex( new Vector3( _dim.x, 0, 0 ),			// 9
							new Vector2( _uv2.x, _uv1.y ) ) ;
		geometry.addVertex( new Vector3( 0, 0, z ),					// 10
							new Vector2( _uv1.x, _uv2.y ) ) ;
		geometry.addVertex( new Vector3( _dim.x, 0, z ),			// 11
							new Vector2( _uv2.x, _uv2.y ) ) ;

		geometry.addVertex( new Vector3( 0, _dim.y, 0 ),			// 12 - 4th Face - Bottom
							new Vector2( _uv1.x, _uv1.y ) ) ;
		geometry.addVertex( new Vector3( _dim.x, _dim.y, 0 ),		// 13
							new Vector2( _uv2.x, _uv1.y ) ) ;
		geometry.addVertex( new Vector3( 0, _dim.y, z ),			// 14
							new Vector2( _uv1.x, _uv2.y ) ) ;
		geometry.addVertex( new Vector3( _dim.x, _dim.y, z ),		// 15
							new Vector2( _uv2.x, _uv2.y ) ) ;

		geometry.addVertex( new Vector3( 0, 0, z ),					// 16 - 5th Face - Left
							new Vector2( _uv1.x, _uv1.y ) ) ;
		geometry.addVertex( new Vector3( 0, 0, 0 ),					// 17
							new Vector2( _uv2.x, _uv1.y ) ) ;
		geometry.addVertex( new Vector3( 0, _dim.y, z ),			// 18
							new Vector2( _uv1.x, _uv2.y ) ) ;
		geometry.addVertex( new Vector3( 0, _dim.y, 0 ),			// 19
							new Vector2( _uv2.x, _uv2.y ) ) ;

		geometry.addVertex( new Vector3( _dim.x, 0, 0 ),			// 20 - 6th Face - Left
							new Vector2( _uv1.x, _uv1.y ) ) ;
		geometry.addVertex( new Vector3( _dim.x, 0, z ),			// 21
							new Vector2( _uv2.x, _uv1.y ) ) ;
		geometry.addVertex( new Vector3( _dim.x, _dim.y, 0 ),		// 22
							new Vector2( _uv1.x, _uv2.y ) ) ;
		geometry.addVertex( new Vector3( _dim.x, _dim.y, z ),		// 23
							new Vector2( _uv2.x, _uv2.y ) ) ;

		geometry.addIndices( 0 ) ;	// 1st Face
		geometry.addIndices( 1 ) ;
		geometry.addIndices( 2 ) ;
		geometry.addIndices( 2 ) ;
		geometry.addIndices( 1 ) ;
		geometry.addIndices( 3 ) ;

		geometry.addIndices( 4 ) ;	// 2nd Face
		geometry.addIndices( 5 ) ;
		geometry.addIndices( 6 ) ;
		geometry.addIndices( 6 ) ;
		geometry.addIndices( 5 ) ;
		geometry.addIndices( 7 ) ;

		geometry.addIndices( 10 ) ;	// 3rd Face
		geometry.addIndices( 11 ) ;
		geometry.addIndices( 8 ) ;
		geometry.addIndices( 8 ) ;
		geometry.addIndices( 11 ) ;
		geometry.addIndices( 9 ) ;
		
		geometry.addIndices( 14 ) ;	// 4th Face
		geometry.addIndices( 15 ) ;
		geometry.addIndices( 12 ) ;
		geometry.addIndices( 12 ) ;
		geometry.addIndices( 15 ) ;
		geometry.addIndices( 13 ) ;
		
		geometry.addIndices( 18 ) ;	// 5th Face
		geometry.addIndices( 19 ) ;
		geometry.addIndices( 16 ) ;
		geometry.addIndices( 16 ) ;
		geometry.addIndices( 19 ) ;
		geometry.addIndices( 17 ) ;
		
		geometry.addIndices( 22 ) ;	// 6th Face
		geometry.addIndices( 23 ) ;
		geometry.addIndices( 20 ) ;
		geometry.addIndices( 20 ) ;
		geometry.addIndices( 23 ) ;
		geometry.addIndices( 21 ) ;
		
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