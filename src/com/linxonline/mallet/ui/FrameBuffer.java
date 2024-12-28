package com.linxonline.mallet.ui ;

import java.util.UUID ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.ui.gui.GUI ;

public final class FrameBuffer
{
	private final World world ;
	private final Program program ;
	private final Camera camera ;
	private final Draw frame ;

	public FrameBuffer( final float _posX,  final float _posY, final float _posZ,
						final float _offX,  final float _offY, final float _offZ,
						final float _width, final float _height )
	{
		this( _posX, _posY, _posZ,
			  _offX, _offY, _offZ,
			  ( int )_width, ( int )_height) ;
	}

	public FrameBuffer( final float _posX, final float _posY, final float _posZ,
						final float _offX, final float _offY, final float _offZ,
						final int _width,  final int _height )
	{
		final String uid = UUID.randomUUID().toString() ;

		camera = CameraAssist.add( new Camera( uid ) ) ;
		world = WorldAssist.add( new World( uid ) ) ;
		world.addCameras( camera ) ;

		frame = new Draw( _posX, _posY, _posZ,
						  _offX, _offY, _offZ ) ;
		frame.setShape( Shape.constructPlane( new Vector3( _width, _height, 0 ),
											  new Vector2( 0, 1 ),
											  new Vector2( 1, 0 ) ) ) ;

		program = ProgramAssist.add( new Program( "SIMPLE_TEXTURE" ) ) ;
		program.mapUniform( "inTex0", new Texture( world ) ) ;
	}

	public void setPosition( final float _x, final float _y, final float _z )
	{
		frame.setPosition( _x, _y, _z ) ;
	}

	public void setOffset( final float _x, final float _y, final float _z )
	{
		frame.setOffset( _x, _y, _z ) ;
	}

	public void setLength( final float _x, final float _y, final float _z )
	{
		final int width = ( int )_x ;
		final int height = ( int )_y ;

		camera.setScreenResolution( width, height ) ;
		camera.setDisplayResolution( width, height ) ;
		camera.setOrthographic( 0.0f, height, 0.0f, width, -1000.0f, 1000.0f ) ;

		world.setRenderDimensions( 0, 0, width, height ) ;

		CameraAssist.update( camera ) ;
		WorldAssist.update( world ) ;

		Shape.updatePlaneGeometry( ( Shape )frame.getShape(), _x, _y, _z ) ;;
	}

	public World getWorld()
	{
		return world ;
	}

	public Camera getCamera()
	{
		return camera ;
	}

	public Program getProgram()
	{
		return program ;
	}

	public Draw getFrame()
	{
		return frame ;
	}

	public void shutdown()
	{
		GUI.getDrawUpdaterPool().clean( world ) ;
		GUI.getTextUpdaterPool().clean( world ) ;

		WorldAssist.remove( world ) ;
		CameraAssist.remove( camera ) ;
		ProgramAssist.remove( program ) ;
	}
}
