package com.linxonline.mallet.ui ;

import java.util.UUID ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.maths.* ;

public class FrameBuffer
{
	private final World world ;
	private final Camera camera ;
	private final Draw frame ;

	private DrawDelegate<World, Draw> delegate = null ;

	public FrameBuffer( final float _posX,  final float _posY, final float _posZ,
						final float _offX,  final float _offY, final float _offZ,
						final float _width, final float _height, final int _layer )
	{
		this( _posX, _posY, _posZ,
			  _offX, _offY, _offZ,
			  ( int )_width, ( int )_height, _layer ) ;
	}

	public FrameBuffer( final float _posX, final float _posY, final float _posZ,
						final float _offX, final float _offY, final float _offZ,
						final int _width,  final int _height, final int _layer )
	{
		final UUID uid = UUID.randomUUID() ;
		world = WorldAssist.constructWorld( uid.toString(), 0 ) ;
		camera = CameraAssist.createCamera( "CAMERA", new Vector3(),
													  new Vector3(),
													  new Vector3( 1, 1, 1 ) ) ;

		CameraAssist.addCamera( camera, world ) ;
		frame = FrameBuffer.createFrame( world, _posX, _posY, _posZ,
												_offX, _offY, _offZ,
												_width, _height, _layer ) ;
	}

	public void setDrawDelegate( final DrawDelegate<World, Draw> _delegate )
	{
		if( delegate != null )
		{
			// Shutdown the previous delegate and remove any rendering 
			// resources that may have been assigned.
			delegate.shutdown() ;
		}

		delegate = _delegate ;
	}

	public void setPosition( final float _x, final float _y, final float _z )
	{
		DrawAssist.amendPosition( frame, _x, _y, _z ) ;
	}

	public void setOffset( final float _x, final float _y, final float _z )
	{
		DrawAssist.amendOffset( frame, _x, _y, _z ) ;
	}

	public void setLength( final float _x, final float _y, final float _z )
	{
		final int width = ( int )_x ;
		final int height = ( int )_y ;

		CameraAssist.amendScreenResolution( camera, width, height ) ;
		CameraAssist.amendDisplayResolution( camera, width, height ) ;
		CameraAssist.amendOrthographic( camera, 0.0f, height, 0.0f, width, -1000.0f, 1000.0f ) ;

		WorldAssist.setRenderDimensions( world, 0, 0, width, height ) ;
		WorldAssist.setDisplayDimensions( world, 0, 0, width, height ) ;

		Shape.updatePlaneGeometry( DrawAssist.getDrawShape( frame ), _x, _y, _z ) ;
		DrawAssist.forceUpdate( frame ) ;
	}

	public void setLayer( final int _layer )
	{
		DrawAssist.amendOrder( frame, _layer ) ;
	}
	
	public DrawDelegate<World, Draw> getDrawDelegate()
	{
		return delegate ;
	}

	public World getWorld()
	{
		return world ;
	}

	public Camera getCamera()
	{
		return camera ;
	}

	public Draw getFrame()
	{
		return frame ;
	}

	public void shutdown()
	{
		if( world != WorldAssist.getDefaultWorld() )
		{
			WorldAssist.destroyWorld( world ) ;
		}
	}

	public static FrameBuffer connect( final UIElement _element, final FrameBuffer _buffer )
	{
		UIElement.connect( _element, _element.positionChanged(), new Connect.Slot<UIElement>()
		{
			@Override
			public void slot( final UIElement _this )
			{
				final Vector3 position = _this.getPosition() ;
				_buffer.setPosition( position.x, position.y, position.z ) ;
			}
		} ) ;

		UIElement.connect( _element, _element.offsetChanged(), new Connect.Slot<UIElement>()
		{
			@Override
			public void slot( final UIElement _this )
			{
				final Vector3 offset = _this.getOffset() ;
				_buffer.setOffset( offset.x, offset.y, offset.z ) ;
			}
		} ) ;

		UIElement.connect( _element, _element.lengthChanged(), new Connect.Slot<UIElement>()
		{
			@Override
			public void slot( final UIElement _this )
			{
				final Vector3 length = _this.getLength() ;
				_buffer.setLength( length.x, length.y, length.z ) ;
			}
		} ) ;

		UIElement.connect( _element, _element.layerChanged(), new Connect.Slot<UIElement>()
		{
			@Override
			public void slot( final UIElement _this )
			{
				final int layer = _this.getLayer() + 1 ;
				_buffer.setLayer( layer ) ;
			}
		} ) ;

		UIElement.connect( _element, _element.elementShutdown(), new Connect.Slot<UIElement>()
		{
			@Override
			public void slot( final UIElement _this )
			{
				_buffer.shutdown() ;
			}
		} ) ;
		
		return _buffer ;
	}
	
	private static Draw createFrame( final World _world, final float _posX, final float _posY, final float _posZ,
														 final float _offX, final float _offY, final float _offZ,
														 final int _width, final int _height,  final int _layer )
	{
		final Draw pane = DrawAssist.createDraw( new Vector3( _posX, _posY, _posZ ),
												 new Vector3( _offX, _offY, _offZ ),
												 new Vector3(),
												 new Vector3( 1, 1, 1 ), _layer ) ;

		DrawAssist.amendUI( pane, true ) ;
		DrawAssist.amendShape( pane, Shape.constructPlane( new Vector3( _width, _height, 0 ),
														   new Vector2( 0, 1 ),
														   new Vector2( 1, 0 ) ) ) ;

		final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
		ProgramAssist.map( program, "inTex0", new MalletTexture( _world ) ) ;
		DrawAssist.attachProgram( pane, program ) ;

		return pane ;
	}
}
