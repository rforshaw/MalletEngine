package com.linxonline.mallet.ui.gui ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public class GUI
{
	public GUI() {}

	public static Shape updateColour( final Shape _shape, final MalletColour _colour )
	{
		final int size = _shape.getVertexSize() ;
		for( int i = 0; i < size; i++ )
		{
			_shape.setColour( i, 1, _colour ) ;
		}
		return _shape ;
	}

	public static Shape constructEdge( final Vector3 _length, final float _edge )
	{
		Shape.Swivel[] swivel = Shape.Swivel.constructSwivel( Shape.Swivel.POINT,
															  Shape.Swivel.COLOUR,
															  Shape.Swivel.UV ) ;

		final Vector3 length = new Vector3( _length ) ;
		length.subtract( _edge * 2, _edge * 2, _edge * 2 ) ;
		final MalletColour white = MalletColour.white() ;

		// 9 represents the amount of faces - Top Left Corner, Top Edge, etc..
		// 4 is the amount of vertices needed for each face
		// and 6 is the amount of indexes needed to construct that face
		final int faces = 9 ;
		final Shape shape = Shape.create( Shape.Style.FILL, swivel, faces * 6, faces * 4 ) ;

		// Top Left Corner
		shape.addVertex( Shape.Swivel.createVert( new Vector3( 0.0f,  0.0f,  0.0f ), white, new Vector2( 0, 0 ) ) ) ;	
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge, 0.0f,  0.0f ), white, new Vector2( 1, 0 ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( 0.0f,  _edge, 0.0f ), white, new Vector2( 0, 0.3f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge, _edge, 0.0f ), white, new Vector2( 1, 0.3f ) ) ) ;

		int offset = 0 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		// Top Edge
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge, 0.0f,  0.0f ),            white, new Vector2( 0, 0.4f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x, 0.0f,  0.0f ), white, new Vector2( 0, 0.6f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge, _edge, 0.0f ),            white, new Vector2( 1, 0.4f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x, _edge, 0.0f ), white, new Vector2( 1, 0.6f ) ) ) ;

		offset += 4 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		// Top Right Corner
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x, 0.0f,  0.0f ),         white, new Vector2( 1, 0  ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x + _edge, 0.0f,  0.0f ), white, new Vector2( 0, 0 ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x, _edge, 0.0f ),         white, new Vector2( 1, 0.3f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x + _edge, _edge, 0.0f ), white, new Vector2( 0, 0.3f ) ) ) ;

		offset += 4 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		// Left Edge
		shape.addVertex( Shape.Swivel.createVert( new Vector3( 0.0f,  _edge,  0.0f ),           white, new Vector2( 0, 0.4f ) ) ) ;	
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge, _edge,  0.0f ),           white, new Vector2( 1, 0.4f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( 0.0f,  _edge + length.y, 0.0f ), white, new Vector2( 0, 0.6f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge, _edge + length.y, 0.0f ), white, new Vector2( 1, 0.6f ) ) ) ;

		offset += 4 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		// Bottom Left Corner
		shape.addVertex( Shape.Swivel.createVert( new Vector3( 0.0f,  _edge + length.y,  0.0f ),        white, new Vector2( 0, 0.3f ) ) ) ;	
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge, _edge + length.y,  0.0f ),        white, new Vector2( 1, 0.3f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( 0.0f,  _edge + length.y + _edge, 0.0f ), white, new Vector2( 0, 0 ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge, _edge + length.y + _edge, 0.0f ), white, new Vector2( 1, 0 ) ) ) ;

		offset += 4 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		// Right Edge
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x,  _edge,  0.0f ),                  white, new Vector2( 1, 0.4f ) ) ) ;	
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x + _edge, _edge,  0.0f ),           white, new Vector2( 0, 0.4f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x,  _edge + length.y, 0.0f ),        white, new Vector2( 1, 0.6f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x + _edge, _edge + length.y, 0.0f ), white, new Vector2( 0, 0.6f ) ) ) ;

		offset += 4 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		// Bottom Right Corner
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x,  _edge + length.y,  0.0f ),               white, new Vector2( 1, 0.3f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x + _edge, _edge + length.y,  0.0f ),        white, new Vector2( 0, 0.3f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x,  _edge + length.y + _edge, 0.0f ),        white, new Vector2( 1, 0 ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x + _edge, _edge + length.y + _edge, 0.0f ), white, new Vector2( 0, 0 ) ) ) ;

		offset += 4 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		// Bottom Edge
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge, _edge + length.y,  0.0f ),                   white, new Vector2( 1, 0.4f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x, _edge + length.y,  0.0f ),        white, new Vector2( 1, 0.5f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge, _edge + length.y + _edge, 0.0f ),            white, new Vector2( 0, 0.4f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x, _edge + length.y + _edge, 0.0f ), white, new Vector2( 0, 0.5f ) ) ) ;

		offset += 4 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		// Middle
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge, _edge,  0.0f ),                      white, new Vector2( 0.1f, 0.7f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x, _edge,  0.0f ),           white, new Vector2( 0.9f, 0.7f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge, _edge + length.y, 0.0f ),            white, new Vector2( 0.1f, 0.9f ) ) ) ;
		shape.addVertex( Shape.Swivel.createVert( new Vector3( _edge + length.x, _edge + length.y, 0.0f ), white, new Vector2( 0.9f, 0.9f ) ) ) ;

		offset += 4 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		return shape ;
	}

	public static Shape updateEdge( final Shape _shape, final Vector3 _length, final float _edge )
	{
		final Vector3 length = new Vector3( _length ) ;
		length.subtract( _edge * 2, _edge * 2, _edge * 2 ) ;

		// Top Left Corner
		_shape.setVector3( 0, 0, new Vector3( 0.0f, 0.0f,  0.0f ) ) ;
		_shape.setVector3( 1, 0, new Vector3( _edge, 0.0f,  0.0f ) ) ;
		_shape.setVector3( 2, 0, new Vector3( 0.0f,  _edge, 0.0f ) ) ;
		_shape.setVector3( 3, 0, new Vector3( _edge, _edge, 0.0f ) ) ;

		// Top Edge
		_shape.setVector3( 4, 0, new Vector3( _edge, 0.0f,  0.0f ) ) ;
		_shape.setVector3( 5, 0, new Vector3( _edge + length.x, 0.0f,  0.0f ) ) ;
		_shape.setVector3( 6, 0, new Vector3( _edge, _edge, 0.0f ) ) ;
		_shape.setVector3( 7, 0, new Vector3( _edge + length.x, _edge, 0.0f ) ) ;

		// Top Right Corner
		_shape.setVector3( 8, 0, new Vector3( _edge + length.x, 0.0f,  0.0f ) ) ;
		_shape.setVector3( 9, 0, new Vector3( _edge + length.x + _edge, 0.0f,  0.0f ) ) ;
		_shape.setVector3( 10, 0, new Vector3( _edge + length.x, _edge, 0.0f ) ) ;
		_shape.setVector3( 11, 0, new Vector3( _edge + length.x + _edge, _edge, 0.0f ) ) ;

		// Left Edge
		_shape.setVector3( 12, 0, new Vector3( 0.0f,  _edge,  0.0f ) ) ;	
		_shape.setVector3( 13, 0, new Vector3( _edge, _edge,  0.0f ) ) ;
		_shape.setVector3( 14, 0, new Vector3( 0.0f,  _edge + length.y, 0.0f ) ) ;
		_shape.setVector3( 15, 0, new Vector3( _edge, _edge + length.y, 0.0f ) ) ;

		// Bottom Left Corner
		_shape.setVector3( 16, 0, new Vector3( 0.0f,  _edge + length.y,  0.0f ) ) ;	
		_shape.setVector3( 17, 0, new Vector3( _edge, _edge + length.y,  0.0f ) ) ;
		_shape.setVector3( 18, 0, new Vector3( 0.0f,  _edge + length.y + _edge, 0.0f ) ) ;
		_shape.setVector3( 19, 0, new Vector3( _edge, _edge + length.y + _edge, 0.0f ) ) ;

		// Right Edge
		_shape.setVector3( 20, 0, new Vector3( _edge + length.x,  _edge,  0.0f ) ) ;	
		_shape.setVector3( 21, 0, new Vector3( _edge + length.x + _edge, _edge,  0.0f ) ) ;
		_shape.setVector3( 22, 0, new Vector3( _edge + length.x,  _edge + length.y, 0.0f ) ) ;
		_shape.setVector3( 23, 0, new Vector3( _edge + length.x + _edge, _edge + length.y, 0.0f ) ) ;

		// Bottom Right Corner
		_shape.setVector3( 24, 0, new Vector3( _edge + length.x,  _edge + length.y,  0.0f ) ) ;	
		_shape.setVector3( 25, 0, new Vector3( _edge + length.x + _edge, _edge + length.y,  0.0f ) ) ;
		_shape.setVector3( 26, 0, new Vector3( _edge + length.x,  _edge + length.y + _edge, 0.0f ) ) ;
		_shape.setVector3( 27, 0, new Vector3( _edge + length.x + _edge, _edge + length.y + _edge, 0.0f ) ) ;

		// Bottom Edge
		_shape.setVector3( 28, 0, new Vector3( _edge, _edge + length.y,  0.0f ) ) ;
		_shape.setVector3( 29, 0, new Vector3( _edge + length.x, _edge + length.y,  0.0f ) ) ;
		_shape.setVector3( 30, 0, new Vector3( _edge, _edge + length.y + _edge, 0.0f ) ) ;
		_shape.setVector3( 31, 0, new Vector3( _edge + length.x, _edge + length.y + _edge, 0.0f ) ) ;

		// Middle
		_shape.setVector3( 32, 0, new Vector3( _edge, _edge,  0.0f ) ) ;
		_shape.setVector3( 33, 0, new Vector3( _edge + length.x, _edge,  0.0f ) ) ;
		_shape.setVector3( 34, 0, new Vector3( _edge, _edge + length.y, 0.0f ) ) ;
		_shape.setVector3( 35, 0, new Vector3( _edge + length.x, _edge + length.y, 0.0f ) ) ;

		return _shape ;
	}
}
