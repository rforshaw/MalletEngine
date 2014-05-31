package com.linxonline.mallet.renderer.android ;

import android.content.res.Resources ;
import android.content.Context ;
import android.view.SurfaceHolder ;
import android.graphics.Paint.Style ;
import android.graphics.* ;

import com.linxonline.mallet.resources.android.* ;

import com.linxonline.mallet.renderer.Basic2DRender ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.resources.texture.* ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.system.GlobalConfig ;

public class Canvas2DRenderer extends Basic2DRender
{
	private int numID = 0 ;

	private final AndroidTextureManager textures ;
	private final Paint fontPaint = new Paint() ;
	private final Paint paint = new Paint() ;

	private final Matrix scaleMatrix = new Matrix() ;
	private final SurfaceHolder holder ;
	private Canvas canvas = null ;

	protected DrawInterface drawShape = null ;
	protected DrawInterface drawTexture = null ;
	protected DrawInterface drawText = null ;

	private final Vector2 pos = new Vector2() ;
	private final Vector3 oldCameraPosition = new Vector3() ;

	private Vector2 renderScaleSize = null ;
	private Vector2 renderRatio = null ;
	private Vector2 screenOffset = null ;
	private Vector3 cameraPosition = null ;
	
	public Canvas2DRenderer( final SurfaceHolder _holder, final Resources _resources )
	{
		textures = new AndroidTextureManager( _resources ) ;
		holder = _holder ;
	}

	@Override
	public void start()
	{
		initDrawCalls() ;

		paint.setFilterBitmap( true ) ;
		paint.setStyle( Paint.Style.FILL ) ;
		fontPaint.setAntiAlias( true ) ;
	}

	@Override
	public void shutdown() {}
	
	private void initDrawCalls()
	{
		drawShape = new DrawInterface()
		{
			public void draw( final Settings _settings, final Vector2 _position )
			{
				setGraphicsColour( _settings ) ;
				Android2DDraw.drawLine( canvas, _settings, _position, paint ) ;
				Android2DDraw.drawLines( canvas, _settings, _position, paint ) ;
				Android2DDraw.drawPolygon( canvas, _settings, _position, paint ) ;
				Android2DDraw.drawPoints( canvas, _settings, _position, paint ) ;
			}
		} ;

		drawTexture = new DrawInterface()
		{
			public void draw( final Settings _settings, final Vector2 _position )
			{
				setGraphicsColour( _settings ) ;
				Texture<AndroidImage> texture = _settings.getObject( "TEXTURE", null ) ;
				if( texture == null )
				{
					texture = loadTexture( _settings ) ;
					if( texture == null ) { return ; }
				}

				scaleMatrix.reset() ;
				final Vector2 scale = _settings.getObject( "SCALE", null ) ;
				if( scale != null )
				{
					scaleMatrix.setScale( scale.x, scale.y ) ;
				}

				final Vector2 offset = _settings.getObject( "OFFSET", DEFAULT_OFFSET ) ;
				scaleMatrix.setTranslate( _position.x + offset.x, _position.y + offset.y ) ;

				final AndroidImage image = texture.getImage() ;
				canvas.drawBitmap( image.bitmap, scaleMatrix, paint ) ;
			}
		} ;

		drawText = new DrawInterface()
		{
			public void draw( final Settings _settings, final Vector2 _position )
			{
				final String text = _settings.getString( "TEXT", null ) ;
				if( text == null )
				{
					return ;
				}

				setGraphicsColour( _settings ) ;
				final MalletFont font = _settings.getObject( "FONT", null ) ;
				if( font != null )
				{
					fontPaint.setTextSize( font.size ) ;
				}

				final float textWidth = fontPaint.measureText( text ) ;
				_settings.addInteger( "TEXTWIDTH", ( int )textWidth ) ;

				final Vector2 offset = _settings.getObject( "OFFSET", DEFAULT_OFFSET ) ;
				final Vector2 position = new Vector2( _position.x + offset.x, _position.y + offset.y ) ;
				final Vector2 currentPos = new Vector2( position.x, position.y ) ;

				final int lineWidth = _settings.getInteger( "LINEWIDTH", ( int )renderScaleSize.x ) ;
				final float lineHeight = fontPaint.getTextSize();

				final String[] words = text.split( " " ) ;
				final int size = words.length ;
				String word = null ;

				for( int i = 0; i < size; ++i )
				{
					word = words[i] ;
					final float wordWidth = fontPaint.measureText( word + " " ) ;
					if( currentPos.x + wordWidth >= lineWidth )
					{
						// Jump to next line if beyond renderWidth
						currentPos.y += lineHeight ;
						currentPos.x = position.x ;
					}

					canvas.drawText( word, currentPos.x, currentPos.y, fontPaint ) ;
					currentPos.x += wordWidth ;
				}
			}
		} ;
	}

	private void setGraphicsColour( final Settings _settings )
	{
		final MalletColour colour = _settings.getObject( "COLOUR", null ) ;
		if( colour != null )
		{
			fontPaint.setARGB( 255, colour.getRed(), colour.getGreen(), colour.getBlue() ) ;
			paint.setARGB( 255, colour.getRed(), colour.getGreen(), colour.getBlue() ) ;
		}
		else
		{
			fontPaint.setColor( Color.WHITE ) ;
			paint.setColor( Color.WHITE ) ;
		}
	}

	@Override
	public void updateState( final float _dt )
	{
		super.updateState( _dt ) ;
		cameraPosition = renderInfo.getCameraPosition() ;
		oldCameraPosition.setXYZ( cameraPosition ) ;
	}

	@Override
	public void draw( final float _dt )
	{
		try
		{
			canvas = holder.lockCanvas() ;
			synchronized( holder )
			{
				if( canvas != null )
				{
					if( cameraPosition == null )
					{
						System.out.println( "Camera Not Set" ) ;
						return ;
					}

					++renderIter ;
					drawDT = _dt ;

					updateEvents() ;		// Process the latest batch of Events
					render( _dt ) ;
				}
			}
		}
		finally
		{
			if( canvas != null )
			{
				//System.out.println( "POST ON CANVAS" ) ;
				holder.unlockCanvasAndPost( canvas ) ;
			}
		}
	}

	private void render( final float _dt )
	{
		scaleMatrix.reset() ;

		cameraPosition  = renderInfo.getCameraPosition() ;
		renderScaleSize = renderInfo.getScaledRenderDimensions() ;
		renderRatio     = renderInfo.getScaleRenderToDisplay() ;
		screenOffset    = renderInfo.getScreenOffset() ;

		canvas.translate( screenOffset.x, screenOffset.y ) ;
		canvas.clipRect( 0, 0, ( int )renderScaleSize.x, ( int )renderScaleSize.y ) ;
		canvas.scale( renderRatio.x, renderRatio.y ) ;
		
		calculateInterpolatedPosition( oldCameraPosition, cameraPosition, pos ) ;
		canvas.translate( pos.x, pos.y ) ;

		if( state.isStateStable() == true )
		{
			state.draw() ;
		}
	}

	private Texture loadTexture( final Settings _draw )
	{
		return textures.get( _draw.getString( "FILE", null ) ) ;
	}

	@Override
	protected void createTexture( final Settings _draw )
	{
		final Vector3 position = _draw.getObject( "POSITION", null ) ;
		final int layer = _draw.getInteger( "LAYER", -1 ) ;

		if( position != null )
		{
			final RenderData data = new RenderData( numID++, DrawRequestType.TEXTURE, _draw, position, layer ) ;
			passIDToCallback( data.id, _draw.<IDInterface>getObject( "CALLBACK", null ) ) ;
			data.drawCall = drawTexture ;
			insert( data ) ;
		}
	}

	@Override
	protected void createGeometry( final Settings _draw ) {}

	@Override
	protected void createText( final Settings _draw )
	{
		final Vector3 position = _draw.getObject( "POSITION", null ) ;
		final int layer = _draw.getInteger( "LAYER", -1 ) ;

		if( position != null )
		{
			final RenderData data = new RenderData( numID++, DrawRequestType.TEXT, _draw, position, layer ) ;
			passIDToCallback( data.id, _draw.<IDInterface>getObject( "CALLBACK", null ) ) ;
			data.drawCall = drawText ;
			insert( data ) ;
		}
	}
}