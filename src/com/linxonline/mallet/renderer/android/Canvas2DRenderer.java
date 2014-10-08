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
import com.linxonline.mallet.renderer.font.* ;
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
	private final Rect rect = new Rect() ;
	private final RectF rectF = new RectF() ;
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
	public void initFontAssist()
	{
		FontAssist.setFontWrapper( new FontInterface()
		{
			@Override
			public Font createFont( final String _font, final int _style, final int _size )
			{
				return new Font<Paint>( fontPaint )
				{
					@Override
					public int getHeight()
					{
						font.setTextSize( _size ) ;
						return ( int )font.getTextSize() ;
					}

					@Override
					public int stringWidth( final String _text )
					{
						font.setTextSize( _size ) ;
						return ( int )font.measureText( _text ) ;
					}
				} ;
			}
		} ) ;
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
	public void shutdown()
	{
		clear() ;
		clean() ;
	}
	
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

				final Vector2 offset = _settings.getObject( "OFFSET", DEFAULT_OFFSET ) ;
				float x = _position.x + offset.x ;
				float y = _position.y + offset.y ;

				float width = texture.getWidth() ;
				float height = texture.getHeight() ;
				rect.set( 0, 0, ( int )width, ( int )height ) ;

				Vector2 fillDim = _settings.getObject( "FILL", null ) ;
				Vector2 dimension = _settings.getObject( "DIM", null ) ;
				if( dimension != null )
				{
					width = dimension.x ;
					height = dimension.y ;
				}

				if( _settings.getBoolean( "GUI", false ) == true )
				{
					x -= pos.x ;
					y -= pos.y ;
				}

				rectF.set( x, y, x + width, y + height ) ;

				final AndroidImage image = texture.getImage() ;
				canvas.drawBitmap( image.bitmap, rect, rectF, paint ) ;
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
	public String getName()
	{
		return "CANVAS_2D_RENDERER" ;
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
			cameraPosition = renderInfo.getCameraPosition() ;
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

					render( _dt ) ;
				}
			}
		}
		finally
		{
			if( canvas != null )
			{
				holder.unlockCanvasAndPost( canvas ) ;
				canvas = null ;
			}
		}
	}

	private void render( final float _dt )
	{
		scaleMatrix.reset() ;

		renderScaleSize = renderInfo.getScaledRenderDimensions() ;
		renderRatio     = renderInfo.getScaleRenderToDisplay() ;
		screenOffset    = renderInfo.getScreenOffset() ;

		canvas.translate( screenOffset.x, screenOffset.y ) ;
		canvas.clipRect( 0, 0, ( int )renderScaleSize.x, ( int )renderScaleSize.y ) ;
		canvas.drawColor( Color.BLACK ) ;
		canvas.scale( renderRatio.x, renderRatio.y ) ;

		updateEvents() ;		// Process the latest batch of Events

		calculateInterpolatedPosition( oldCameraPosition, cameraPosition, pos ) ;
		canvas.translate( pos.x, pos.y ) ;

		state.removeRenderData() ;
		if( state.isStateStable() == true )
		{
			state.draw() ;
		}
	}

	@Override
	public void clean()
	{
		textures.clean() ;
	}

	private Texture loadTexture( final Settings _draw )
	{
		final String file = _draw.getString( "FILE", null ) ;
		final Texture texture = textures.get( file ) ;
		_draw.addObject( "TEXTURE", texture ) ;
		return texture ;
	}

	@Override
	protected void createTexture( final Settings _draw )
	{
		final Vector3 position = _draw.getObject( "POSITION", null ) ;
		final int layer = _draw.getInteger( "LAYER", -1 ) ;

		if( position != null )
		{
			final RenderData data = new RenderData( numID++, DrawRequestType.TEXTURE, _draw, position, layer )
			{
				@Override
				public void unregisterResources()
				{
					final Texture texture = drawData.getObject( "TEXTURE", null ) ;
					if( texture != null )
					{
						texture.unregister() ;
					}
				}
			} ;

			passIDToCallback( data.id, _draw.<IDInterface>getObject( "CALLBACK", null ) ) ;
			data.drawCall = drawTexture ;
			insert( data ) ;
		}
	}

	@Override
	protected void createGeometry( final Settings _draw )
	{
		final Vector3 position = _draw.getObject( "POSITION", null ) ;
		final int layer = _draw.getInteger( "LAYER", -1 ) ;

		if( position != null )
		{
			final RenderData data = new RenderData( numID++, DrawRequestType.GEOMETRY, _draw, position, layer ) ;
			passIDToCallback( data.id, _draw.<IDInterface>getObject( "CALLBACK", null ) ) ;
			data.drawCall = drawShape ;
			insert( data ) ;
		}
	}

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