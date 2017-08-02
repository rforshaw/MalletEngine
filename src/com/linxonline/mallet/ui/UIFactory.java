package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.core.GlobalConfig ;
import com.linxonline.mallet.util.notification.Notification ;

import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.UpdateType ;

import com.linxonline.mallet.renderer.CameraAssist ;
import com.linxonline.mallet.renderer.Camera ;

import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.World ;

import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.Program ;

import com.linxonline.mallet.maths.Vector3 ;

/**
	Contains helper functions for the construction of 
	commonly used UI elements and their supporting classes.
*/
public final class UIFactory
{
	private UIFactory() {}

	/**
		Construct a UILayout that uses the RenderWidth and RenderHeight
		of the window as the width and height of the UILayout.
		Will use the default camera.
	*/
	public static UILayout constructWindowLayout( final UILayout.Type _type )
	{
		return constructWindowLayout( _type, CameraAssist.getDefaultCamera() ) ;
	}

	/**
		Construct a UILayout that uses the RenderWidth and RenderHeight
		of the window as the width and height of the UILayout.
	*/
	public static UILayout constructWindowLayout( final UILayout.Type _type, final Camera _camera )
	{
		final int width = GlobalConfig.getInteger( "RENDERWIDTH", 640 ) ;
		final int height = GlobalConfig.getInteger( "RENDERHEIGHT", 480 ) ;

		final Vector3 dimension = new Vector3( width, height, 0.0f ) ;
		final UILayout layout = new UILayout( _type, new Vector3(), new Vector3(), dimension ) ;

		layout.addListener( new BaseListener<UILayout>()
		{
			private final Notification.Notify<String> widthNotify = new Notification.Notify<String>()
			{
				public void inform( final String _data )
				{
					final UIRatio ratio = layout.getRatio() ;
					dimension.x = GlobalConfig.getInteger( "RENDERWIDTH", 640 ) ;

					layout.setLength( ratio.toUnitX( dimension.x ),
									  ratio.toUnitY( dimension.y ),
									  ratio.toUnitZ( dimension.z ) ) ;
					layout.makeDirty() ;

					CameraAssist.amendOrthographic( _camera, 0.0f, dimension.y, 0.0f, dimension.x, -1000.0f, 1000.0f ) ;
					CameraAssist.amendScreenResolution( _camera, ( int )dimension.x, ( int )dimension.y ) ;
				}
			} ;

			private final Notification.Notify<String> heightNotify = new Notification.Notify<String>()
			{
				public void inform( final String _data )
				{
					final UIRatio ratio = layout.getRatio() ;
					dimension.y = GlobalConfig.getInteger( "RENDERHEIGHT", 640 ) ;

					layout.setLength( ratio.toUnitX( dimension.x ),
									  ratio.toUnitY( dimension.y ),
									  ratio.toUnitZ( dimension.z ) ) ;
					layout.makeDirty() ;

					CameraAssist.amendOrthographic( _camera, 0.0f, dimension.y, 0.0f, dimension.x, -1000.0f, 1000.0f ) ;
					CameraAssist.amendScreenResolution( _camera, ( int )dimension.x, ( int )dimension.y ) ;
				}
			} ;

			@Override
			public void setParent( final UILayout _parent )
			{
				super.setParent( _parent ) ;
				GlobalConfig.addNotify( "RENDERWIDTH", widthNotify ) ;
				GlobalConfig.addNotify( "RENDERHEIGHT", heightNotify ) ;
			}

			public void refresh() {}

			public void shutdown()
			{
				GlobalConfig.removeNotify( "RENDERWIDTH", widthNotify ) ;
				GlobalConfig.removeNotify( "RENDERHEIGHT", heightNotify ) ;
			}
		} ) ;

		return layout ;
	}

	public static <T extends UIElement> UIBasicListener<T> constructUIListener( final String _text,
																				final MalletFont _font,
																				final MalletTexture _sheet,
																				final UIElement.UV _uv )
	{
		return new UIBasicListener<T>( _text, _font, _sheet, _uv ) ;
	}

	public static <T extends UIElement> UIBasicListener<T> constructUIListener( final MalletTexture _sheet,
																				final UIElement.UV _uv )
	{
		return new UIBasicListener<T>( _sheet, _uv ) ;
	}

	public static class UIBasicListener<T extends UIElement> extends UIListener<T>
	{
		private final Vector3 aspectRatio = new Vector3() ;		// Visual elements aspect ratio
		private final Vector3 length = new Vector3() ;			// Actual length of the visual element
		private final Vector3 offset = new Vector3() ;			// Offset within the UIElement

		private boolean retainRatio = false ;

		private UI.Alignment drawAlignmentX = UI.Alignment.LEFT ;
		private UI.Alignment drawAlignmentY = UI.Alignment.LEFT ;

		private UI.Alignment drawTextAlignmentX = UI.Alignment.CENTRE ;
		private UI.Alignment drawTextAlignmentY = UI.Alignment.CENTRE ;

		private final StringBuilder text = new StringBuilder() ;
		private MalletFont font ;
		private MalletColour colour = MalletColour.white() ;

		private final MalletTexture sheet ;
		private final UIElement.UV uv ;

		protected Draw draw = null ;
		protected Draw drawText = null ;

		public UIBasicListener( final MalletTexture _sheet,
								final UIElement.UV _uv )
		{
			this( null, null, _sheet, _uv ) ;
		}

		public UIBasicListener( final String _text,
								final MalletFont _font,
								final MalletTexture _sheet,
								final UIElement.UV _uv )
		{
			font = _font ;
			if( _text != null )
			{
				text.append( _text ) ;
			}

			sheet = _sheet ;
			uv = _uv ;
		}

		public void setRetainRatio( final boolean _ratio )
		{
			retainRatio = _ratio ;
		}

		public void setAlignment( final UI.Alignment _x, final UI.Alignment _y )
		{
			drawAlignmentX = ( _x == null ) ? UI.Alignment.LEFT : _x ;
			drawAlignmentY = ( _y == null ) ? UI.Alignment.LEFT : _y ;
		}

		public void setTextAlignment( final UI.Alignment _x, final UI.Alignment _y )
		{
			drawTextAlignmentX = ( _x == null ) ? UI.Alignment.CENTRE : _x ;
			drawTextAlignmentY = ( _y == null ) ? UI.Alignment.CENTRE : _y ;
		}

		public void setTextColour( final MalletColour _colour )
		{
			colour = ( _colour != null ) ? _colour : MalletColour.white() ;
		}

		public StringBuilder getText()
		{
			return text ;
		}

		/**
			Can be used to construct Draw objects before a 
			DrawDelegate is provided by the Rendering System.
		*/
		public void constructDraws()
		{
			final T parent = getParent() ;
			final int layer = parent.getLayer() ;

			updateLength( parent.getLength() ) ;
			updateOffset( parent.getOffset() ) ;

			if( sheet != null && uv != null )
			{
				draw = DrawAssist.createDraw( parent.getPosition(),
											  offset,
											  new Vector3(),
											  new Vector3( 1, 1, 1 ),
											  layer ) ;
				DrawAssist.amendUI( draw, true ) ;
				DrawAssist.amendShape( draw, Shape.constructPlane( length, uv.min, uv.max ) ) ;

				final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
				ProgramAssist.map( program, "inTex0", sheet ) ;

				DrawAssist.attachProgram( draw, program ) ;
			}

			if( font != null )
			{
				final Vector3 textOffset = new Vector3( parent.getOffset() ) ;
				textOffset.add( length.x / 2, length.y / 2, 0.0f ) ;

				drawText = DrawAssist.createTextDraw( text,
													  font,
													  parent.getPosition(),
													  textOffset,
													  new Vector3(),
													  new Vector3( 1, 1, 1 ),
													  layer + 1 ) ;
				DrawAssist.amendColour( drawText, colour ) ;
				DrawAssist.amendUI( drawText, true ) ;
			}
		}

		/**
			Called when listener receives a valid DrawDelegate
			and when the parent UIElement is flagged as visible.
		*/
		@Override
		public void addDraws( final DrawDelegate<World, Draw> _delegate, final World _world )
		{
			if( draw != null )
			{
				_delegate.addBasicDraw( draw, _world ) ;
			}

			if( drawText != null )
			{
				_delegate.addTextDraw( drawText, _world ) ;
			}
		}

		/**
			Only called if there is a valid DrawDelegate and 
			when the parent UIElement is flagged as invisible.
		*/
		@Override
		public void removeDraws( final DrawDelegate<World, Draw> _delegate )
		{
			_delegate.removeDraw( draw ) ;
			_delegate.removeDraw( drawText ) ;
		}

		@Override
		public void refresh()
		{
			super.refresh() ;
			final T parent = getParent() ;

			updateLength( parent.getLength() ) ;
			updateOffset( parent.getOffset() ) ;

			if( draw != null )
			{
				DrawAssist.amendOrder( draw, parent.getLayer() ) ;
				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), length ) ;
				DrawAssist.forceUpdate( draw ) ;
			}

			if( drawText != null )
			{
				final Vector3 textOffset = DrawAssist.getOffset( drawText ) ;
				textOffset.setXYZ( offset ) ;

				final MalletFont.Metrics metrics = font.getMetrics() ;
				final float x = UI.align( drawTextAlignmentX, font.stringWidth( text ), length.x ) ;
				final float y = UI.align( drawTextAlignmentY, metrics.getHeight(), length.y ) ;

				textOffset.add( x, y, 0.0f ) ;

				DrawAssist.amendOrder( drawText, parent.getLayer() + 1 ) ;
				DrawAssist.forceUpdate( drawText ) ;
			}
		}

		private void updateLength( final Vector3 _length )
		{
			if( uv == null || retainRatio == false )
			{
				length.setXYZ( _length ) ;
				return ;
			}

			UI.calcSubDimension( aspectRatio, sheet, uv ) ;
			UI.fill( UI.Modifier.RETAIN_ASPECT_RATIO, length, aspectRatio, _length ) ;
		}

		private void updateOffset( final Vector3 _offset )
		{
			UI.align( drawAlignmentX, drawAlignmentY, offset, length, getParent().getLength() ) ;
			offset.add( _offset ) ;
		}

		public MalletTexture getTexture()
		{
			return sheet ;
		}

		public Vector3 getLength()
		{
			return length ;
		}
		
		public Vector3 getOffset()
		{
			return offset ;
		}
	}
}
