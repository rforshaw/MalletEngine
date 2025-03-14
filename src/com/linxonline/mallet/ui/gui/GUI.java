package com.linxonline.mallet.ui.gui ;

import java.util.Map ;

import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public final class GUI
{
	private static final Map<Font, Program> fontPrograms = MalletMap.<Font, Program>newMap() ;
	private static final Map<Texture, Program> texturePrograms = MalletMap.<Texture, Program>newMap() ;

	private static final Program geometryProgram = ProgramAssist.add( new Program( "SIMPLE_GEOMETRY", IShape.Style.LINE_STRIP, new Attribute[]
	{
		new Attribute( "inVertex", IShape.Attribute.VEC3 ),
		new Attribute( "inColour", IShape.Attribute.FLOAT )
	}  ) );

	private static final DrawUpdaterPool drawPool = new DrawUpdaterPool() ;
	private static final TextUpdaterPool textPool = new TextUpdaterPool() ;

	private GUI() {}

	public static DrawUpdaterPool getDrawUpdaterPool()
	{
		return drawPool ;
	}

	public static DrawUpdater getDrawUpdater( final World _world, final Shape _shape, final int _layer )
	{
		final DrawUpdater updater = drawPool.getOrCreate( _world, geometryProgram, _shape, true, _layer ) ;
		updater.setInterpolation( Interpolation.NONE ) ;

		return updater ;
	}

	public static DrawUpdater getDrawUpdater( final World _world, final Texture _texture, final Shape _shape, final int _layer )
	{
		final Program program = getProgram( _texture ) ;
		final DrawUpdater updater = drawPool.getOrCreate( _world, program, _shape, true, _layer ) ;
		updater.setInterpolation( Interpolation.NONE ) ;

		return updater ;
	}

	private static Program getProgram( final Texture _texture )
	{
		Program program = texturePrograms.get( _texture ) ;
		if( program == null )
		{
			program = ProgramAssist.add( new Program( "SIMPLE_TEXTURE" ) ) ;
			program.mapUniform( "inTex0", _texture ) ;

			texturePrograms.put( _texture, program ) ;
		}

		return program ;
	}

	public static TextUpdaterPool getTextUpdaterPool()
	{
		return textPool ;
	}

	public static TextUpdater getTextUpdater( final World _world, final Font _font, final int _layer )
	{
		final Program program = getProgram( _font ) ;
		final TextUpdater updater = textPool.getOrCreate( _world, program, true, _layer ) ;
		updater.setInterpolation( Interpolation.NONE ) ;

		return updater ;
	}

	private static Program getProgram( final Font _font )
	{
		Program program = fontPrograms.get( _font ) ;
		if( program == null )
		{
			program = ProgramAssist.add( new Program( "SIMPLE_FONT" ) ) ;
			program.mapUniform( "inTex0", _font ) ;

			fontPrograms.put( _font, program ) ;
		}

		return program ;
	}

	public static Shape updateColour( final Shape _shape, final Colour _colour )
	{
		final int size = _shape.getVerticesSize() ;
		for( int i = 0; i < size; i++ )
		{
			_shape.setColour( i, 1, _colour ) ;
		}
		return _shape ;
	}

	public static Shape constructEdge( final Vector3 _length, final float _edge )
	{
		Shape.Attribute[] swivel = Shape.Attribute.constructAttribute( Shape.Attribute.VEC3,
															  Shape.Attribute.FLOAT,
															  Shape.Attribute.VEC2 ) ;

		final Vector3 length = new Vector3( _length ) ;
		length.subtract( _edge * 2, _edge * 2, _edge * 2 ) ;
		final Colour white = Colour.white() ;

		// 9 represents the amount of faces - Top Left Corner, Top Edge, etc..
		// 4 is the amount of vertices needed for each face
		// and 6 is the amount of indexes needed to construct that face
		final int faces = 9 ;
		final Shape shape = new Shape( Shape.Style.FILL, swivel, faces * 6, faces * 4 ) ;

		// Top Left Corner
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( 0.0f,  0.0f,  0.0f ), white, new Vector2( 0, 0 ) ) ) ;	
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge, 0.0f,  0.0f ), white, new Vector2( 1, 0 ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( 0.0f,  _edge, 0.0f ), white, new Vector2( 0, 0.3f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge, _edge, 0.0f ), white, new Vector2( 1, 0.3f ) ) ) ;

		int offset = 0 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		// Top Edge
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge, 0.0f,  0.0f ),            white, new Vector2( 0, 0.4f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x, 0.0f,  0.0f ), white, new Vector2( 0, 0.6f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge, _edge, 0.0f ),            white, new Vector2( 1, 0.4f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x, _edge, 0.0f ), white, new Vector2( 1, 0.6f ) ) ) ;

		offset += 4 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		// Top Right Corner
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x, 0.0f,  0.0f ),         white, new Vector2( 1, 0  ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x + _edge, 0.0f,  0.0f ), white, new Vector2( 0, 0 ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x, _edge, 0.0f ),         white, new Vector2( 1, 0.3f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x + _edge, _edge, 0.0f ), white, new Vector2( 0, 0.3f ) ) ) ;

		offset += 4 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		// Left Edge
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( 0.0f,  _edge,  0.0f ),           white, new Vector2( 0, 0.4f ) ) ) ;	
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge, _edge,  0.0f ),           white, new Vector2( 1, 0.4f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( 0.0f,  _edge + length.y, 0.0f ), white, new Vector2( 0, 0.6f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge, _edge + length.y, 0.0f ), white, new Vector2( 1, 0.6f ) ) ) ;

		offset += 4 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		// Bottom Left Corner
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( 0.0f,  _edge + length.y,  0.0f ),        white, new Vector2( 0, 0.3f ) ) ) ;	
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge, _edge + length.y,  0.0f ),        white, new Vector2( 1, 0.3f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( 0.0f,  _edge + length.y + _edge, 0.0f ), white, new Vector2( 0, 0 ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge, _edge + length.y + _edge, 0.0f ), white, new Vector2( 1, 0 ) ) ) ;

		offset += 4 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		// Right Edge
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x,  _edge,  0.0f ),                  white, new Vector2( 1, 0.4f ) ) ) ;	
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x + _edge, _edge,  0.0f ),           white, new Vector2( 0, 0.4f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x,  _edge + length.y, 0.0f ),        white, new Vector2( 1, 0.6f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x + _edge, _edge + length.y, 0.0f ), white, new Vector2( 0, 0.6f ) ) ) ;

		offset += 4 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		// Bottom Right Corner
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x,  _edge + length.y,  0.0f ),               white, new Vector2( 1, 0.3f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x + _edge, _edge + length.y,  0.0f ),        white, new Vector2( 0, 0.3f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x,  _edge + length.y + _edge, 0.0f ),        white, new Vector2( 1, 0 ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x + _edge, _edge + length.y + _edge, 0.0f ), white, new Vector2( 0, 0 ) ) ) ;

		offset += 4 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		// Bottom Edge
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge, _edge + length.y,  0.0f ),                   white, new Vector2( 1, 0.4f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x, _edge + length.y,  0.0f ),        white, new Vector2( 1, 0.5f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge, _edge + length.y + _edge, 0.0f ),            white, new Vector2( 0, 0.4f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x, _edge + length.y + _edge, 0.0f ), white, new Vector2( 0, 0.5f ) ) ) ;

		offset += 4 ;
		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 2 ) ;
		shape.addIndex( offset + 3 ) ;

		shape.addIndex( offset + 0 ) ;
		shape.addIndex( offset + 3 ) ;
		shape.addIndex( offset + 1 ) ;

		// Middle
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge, _edge,  0.0f ),                      white, new Vector2( 0.1f, 0.7f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x, _edge,  0.0f ),           white, new Vector2( 0.9f, 0.7f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge, _edge + length.y, 0.0f ),            white, new Vector2( 0.1f, 0.9f ) ) ) ;
		shape.copyVertex( Shape.Attribute.createVert( new Vector3( _edge + length.x, _edge + length.y, 0.0f ), white, new Vector2( 0.9f, 0.9f ) ) ) ;

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
		final float lengthX = _length.x - ( _edge * 2 ) ;
		final float lengthY = _length.y - ( _edge * 2 ) ;
		final float lengthZ = _length.z - ( _edge * 2 ) ;

		// Top Left Corner
		_shape.setVector3( 0, 0, 0.0f, 0.0f,  0.0f ) ;
		_shape.setVector3( 1, 0, _edge, 0.0f,  0.0f ) ;
		_shape.setVector3( 2, 0, 0.0f,  _edge, 0.0f ) ;
		_shape.setVector3( 3, 0, _edge, _edge, 0.0f ) ;

		// Top Edge
		_shape.setVector3( 4, 0, _edge, 0.0f,  0.0f ) ;
		_shape.setVector3( 5, 0, _edge + lengthX, 0.0f,  0.0f ) ;
		_shape.setVector3( 6, 0, _edge, _edge, 0.0f ) ;
		_shape.setVector3( 7, 0, _edge + lengthX, _edge, 0.0f ) ;

		// Top Right Corner
		_shape.setVector3( 8, 0, _edge + lengthX, 0.0f,  0.0f ) ;
		_shape.setVector3( 9, 0, _edge + lengthX + _edge, 0.0f,  0.0f ) ;
		_shape.setVector3( 10, 0, _edge + lengthX, _edge, 0.0f ) ;
		_shape.setVector3( 11, 0, _edge + lengthX + _edge, _edge, 0.0f ) ;

		// Left Edge
		_shape.setVector3( 12, 0, 0.0f,  _edge,  0.0f ) ;
		_shape.setVector3( 13, 0, _edge, _edge,  0.0f ) ;
		_shape.setVector3( 14, 0, 0.0f,  _edge + lengthY, 0.0f ) ;
		_shape.setVector3( 15, 0, _edge, _edge + lengthY, 0.0f ) ;

		// Bottom Left Corner
		_shape.setVector3( 16, 0, 0.0f,  _edge + lengthY,  0.0f ) ;
		_shape.setVector3( 17, 0, _edge, _edge + lengthY,  0.0f ) ;
		_shape.setVector3( 18, 0, 0.0f,  _edge + lengthY + _edge, 0.0f ) ;
		_shape.setVector3( 19, 0, _edge, _edge + lengthY + _edge, 0.0f ) ;

		// Right Edge
		_shape.setVector3( 20, 0, _edge + lengthX,  _edge,  0.0f ) ;	
		_shape.setVector3( 21, 0, _edge + lengthX + _edge, _edge,  0.0f ) ;
		_shape.setVector3( 22, 0, _edge + lengthX,  _edge + lengthY, 0.0f ) ;
		_shape.setVector3( 23, 0, _edge + lengthX + _edge, _edge + lengthY, 0.0f ) ;

		// Bottom Right Corner
		_shape.setVector3( 24, 0, _edge + lengthX,  _edge + lengthY,  0.0f ) ;
		_shape.setVector3( 25, 0, _edge + lengthX + _edge, _edge + lengthY,  0.0f ) ;
		_shape.setVector3( 26, 0, _edge + lengthX,  _edge + lengthY + _edge, 0.0f ) ;
		_shape.setVector3( 27, 0, _edge + lengthX + _edge, _edge + lengthY + _edge, 0.0f ) ;

		// Bottom Edge
		_shape.setVector3( 28, 0, _edge, _edge + lengthY,  0.0f ) ;
		_shape.setVector3( 29, 0, _edge + lengthX, _edge + lengthY,  0.0f ) ;
		_shape.setVector3( 30, 0, _edge, _edge + lengthY + _edge, 0.0f ) ;
		_shape.setVector3( 31, 0,  _edge + lengthX, _edge + lengthY + _edge, 0.0f ) ;

		// Middle
		_shape.setVector3( 32, 0, _edge, _edge,  0.0f ) ;
		_shape.setVector3( 33, 0, _edge + lengthX, _edge,  0.0f ) ;
		_shape.setVector3( 34, 0, _edge, _edge + lengthY, 0.0f ) ;
		_shape.setVector3( 35, 0, _edge + lengthX, _edge + lengthY, 0.0f ) ;

		return _shape ;
	}
}
