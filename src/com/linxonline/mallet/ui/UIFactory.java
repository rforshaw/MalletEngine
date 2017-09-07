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
import com.linxonline.mallet.maths.Vector2 ;

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

		layout.addListener( new ABase<UILayout>()
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

	public static Shape updateEdgeColour( final Shape _shape, final MalletColour _colour )
	{
		final int size = _shape.getVertexSize() ;
		for( int i = 0; i < size; i++ )
		{
			_shape.setColour( i, 1, _colour ) ;
		}
		return _shape ;
	}
	
	public static <T extends UIElement> GUIBasic<T> constructGUIBasic( final String _text,
																		final MalletFont _font,
																		final MalletTexture _sheet,
																		final UIElement.UV _uv )
	{
		return new GUIBasic<T>( _text, _font, _sheet, _uv ) ;
	}

	public static <T extends UIElement> GUIBasic<T> constructGUIBasic( final MalletTexture _sheet,
																		final UIElement.UV _uv )
	{
		return new GUIBasic<T>( _sheet, _uv ) ;
	}

	public static class GUIEdge<T extends UIElement> extends GUIBasic<T>
	{
		private UI.Alignment drawAlignmentX = UI.Alignment.LEFT ;
		private UI.Alignment drawAlignmentY = UI.Alignment.LEFT ;

		private final MalletTexture sheet ;
		private final float edge ;
		protected Draw draw = null ;

		public GUIEdge( final MalletTexture _sheet, final float _edge )
		{
			this( null, null, _sheet, _edge ) ;
		}

		public GUIEdge( final String _text, final MalletFont _font,
						final MalletTexture _sheet, final float _edge )
		{
			super( _text, _font, null, null ) ;
			sheet = _sheet ;
			edge = _edge ;
		}

		public void setAlignment( final UI.Alignment _x, final UI.Alignment _y )
		{
			drawAlignmentX = ( _x == null ) ? UI.Alignment.LEFT : _x ;
			drawAlignmentY = ( _y == null ) ? UI.Alignment.LEFT : _y ;
		}

		/**
			Can be used to construct Draw objects before a 
			DrawDelegate is provided by the Rendering System.
		*/
		public void constructDraws()
		{
			final T parent = getParent() ;
			updateLength( parent.getLength(), getLength() ) ;
			updateOffset( parent.getOffset(), getOffset() ) ;

			if( sheet != null )
			{
				draw = DrawAssist.createDraw( parent.getPosition(),
											  getOffset(),
											  new Vector3(),
											  new Vector3( 1, 1, 1 ),
											  parent.getLayer() ) ;
				DrawAssist.amendUI( draw, true ) ;
				DrawAssist.amendShape( draw, UIFactory.constructEdge( getLength(), edge ) ) ;

				final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
				ProgramAssist.map( program, "inTex0", sheet ) ;

				DrawAssist.attachProgram( draw, program ) ;
			}

			super.constructDraws() ;
		}

		/**
			Called when listener receives a valid DrawDelegate
			and when the parent UIElement is flagged as visible.
		*/
		@Override
		public void addDraws( final DrawDelegate<World, Draw> _delegate, final World _world )
		{
			super.addDraws( _delegate, _world ) ;
			if( draw != null )
			{
				_delegate.addBasicDraw( draw, _world ) ;
			}
		}

		/**
			Only called if there is a valid DrawDelegate and 
			when the parent UIElement is flagged as invisible.
		*/
		@Override
		public void removeDraws( final DrawDelegate<World, Draw> _delegate )
		{
			super.removeDraws( _delegate ) ;
			_delegate.removeDraw( draw ) ;
		}

		@Override
		public void refresh()
		{
			final T parent = getParent() ;
			updateLength( parent.getLength(), getLength() ) ;
			updateOffset( parent.getOffset(), getOffset() ) ;

			if( draw != null )
			{
				
				DrawAssist.amendOrder( draw, parent.getLayer() ) ;
				UIFactory.updateEdge( DrawAssist.getDrawShape( draw ), getLength(), edge ) ;
				DrawAssist.forceUpdate( draw ) ;
			}

			super.refresh() ;
		}

		private void updateLength( final Vector3 _length, final Vector3 _toUpdate )
		{
			_toUpdate.setXYZ( _length ) ;
		}

		private void updateOffset( final Vector3 _offset, final Vector3 _toUpdate )
		{
			UI.align( drawAlignmentX, drawAlignmentY, _toUpdate, getLength(), getParent().getLength() ) ;
			_toUpdate.add( _offset ) ;
		}

		public MalletTexture getTexture()
		{
			return sheet ;
		}
	}

	public static class GUIBasic<T extends UIElement> extends GUIBase<T>
	{
		private final GUIDraw draw ;
		private final GUIText text ;

		public GUIBasic( final MalletTexture _sheet, final UIElement.UV _uv )
		{
			this( null, null, _sheet, _uv ) ;
		}

		public GUIBasic( final String _text,
						 final MalletFont _font,
						 final MalletTexture _sheet,
						 final UIElement.UV _uv )
		{
			draw = new GUIDraw( _sheet, _uv ) ;
			text = new GUIText( _text, _font ) ;
		}

		public void setRetainRatio( final boolean _ratio )
		{
			draw.setRetainRatio( _ratio ) ;
		}

		public void setAlignment( final UI.Alignment _x, final UI.Alignment _y )
		{
			draw.setAlignment( _x, _y ) ;
		}

		public void setTextAlignment( final UI.Alignment _x, final UI.Alignment _y )
		{
			text.setAlignment( _x, _y ) ;
		}

		public void setTextColour( final MalletColour _colour )
		{
			text.setColour( _colour ) ;
		}

		@Override
		public void setParent( final T _parent )
		{
			super.setParent( _parent ) ;
			draw.setParent( _parent ) ;
			text.setParent( _parent ) ;
		}

		/**
			Can be used to construct Draw objects before a 
			DrawDelegate is provided by the Rendering System.
		*/
		@Override
		public void constructDraws() {}

		/**
			Called when listener receives a valid DrawDelegate
			and when the parent UIElement is flagged as visible.
		*/
		@Override
		public void addDraws( final DrawDelegate<World, Draw> _delegate, final World _world )
		{
			draw.addDraws( _delegate, _world ) ;
			text.addDraws( _delegate, _world ) ;
		}

		/**
			Only called if there is a valid DrawDelegate and 
			when the parent UIElement is flagged as invisible.
		*/
		@Override
		public void removeDraws( final DrawDelegate<World, Draw> _delegate )
		{
			draw.removeDraws( _delegate ) ;
			text.removeDraws( _delegate ) ;
		}

		@Override
		public void refresh()
		{
			super.refresh() ;
			draw.refresh() ;
			text.refresh() ;
		}

		public StringBuilder getText()
		{
			return text.getText() ;
		}

		public UI.Alignment getBasicAlignmentX()
		{
			return draw.getAlignmentX() ;
		}

		public UI.Alignment getBasicAlignmentY()
		{
			return draw.getAlignmentY() ;
		}

		public UI.Alignment getTextAlignmentX()
		{
			return text.getAlignmentX() ;
		}

		public UI.Alignment getTextAlignmentY()
		{
			return text.getAlignmentY() ;
		}

		public Draw getBasicDraw()
		{
			return draw.getDraw() ;
		}

		public Draw getTextDraw()
		{
			return text.getDraw() ;
		}

		public MalletFont getFont()
		{
			return text.getFont() ;
		}

		public MalletColour getColour()
		{
			return text.getColour() ;
		}

		public MalletTexture getTexture()
		{
			return draw.getTexture() ;
		}
	}

	public static class GUIDrawEdge<T extends UIElement> extends GUIDraw<T>
	{
		private final float edge ;
		private MalletColour colour ;

		public GUIDrawEdge( final MalletTexture _sheet, final float _edge )
		{
			super( _sheet, null ) ;
			edge = _edge ;
		}

		public void setColour( final MalletColour _colour )
		{
			colour = ( _colour != null ) ? _colour : MalletColour.white() ;
			final Draw draw = getDraw() ;
			if( draw != null )
			{
				final Shape shape = DrawAssist.getDrawShape( getDraw() ) ;
				if( shape != null )
				{
					updateEdgeColour( shape, colour ) ;
				}
			}
		}

		/**
			Can be used to construct Draw objects before a 
			DrawDelegate is provided by the Rendering System.
		*/
		public void constructDraws()
		{
			super.constructDraws() ;

			final T parent = getParent() ;
			final MalletTexture sheet = getTexture() ;

			if( sheet != null )
			{
				final Draw draw = DrawAssist.createDraw( parent.getPosition(),
														 getOffset(),
														 new Vector3(),
														 new Vector3( 1, 1, 1 ),
														 parent.getLayer() ) ;
				DrawAssist.amendUI( draw, true ) ;
				DrawAssist.amendShape( draw, UIFactory.constructEdge( getLength(), edge ) ) ;
				setColour( getColour() ) ;

				final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
				ProgramAssist.map( program, "inTex0", sheet ) ;

				DrawAssist.attachProgram( draw, program ) ;
				setDraw( draw ) ;
			}

			super.constructDraws() ;
		}

		@Override
		public void refresh()
		{
			final T parent = getParent() ;
			updateLength( parent.getLength(), getLength() ) ;
			updateOffset( parent.getOffset(), getOffset() ) ;

			final Draw draw = getDraw() ;
			if( draw != null )
			{
				DrawAssist.amendOrder( draw, parent.getLayer() ) ;
				UIFactory.updateEdge( DrawAssist.getDrawShape( draw ), getLength(), edge ) ;
				DrawAssist.forceUpdate( draw ) ;
			}

			super.refresh() ;
		}

		private void updateLength( final Vector3 _length, final Vector3 _toUpdate )
		{
			_toUpdate.setXYZ( _length ) ;
		}

		private void updateOffset( final Vector3 _offset, final Vector3 _toUpdate )
		{
			UI.align( drawAlignmentX, drawAlignmentY, _toUpdate, getLength(), getParent().getLength() ) ;
			_toUpdate.add( _offset ) ;
		}

		public MalletColour getColour()
		{
			return colour ;
		}
	}

	public static class GUIDraw<T extends UIElement> extends GUIBase<T>
	{
		private final Vector3 aspectRatio = new Vector3() ;		// Visual elements aspect ratio
		protected boolean retainRatio = false ;

		protected UI.Alignment drawAlignmentX = UI.Alignment.LEFT ;
		protected UI.Alignment drawAlignmentY = UI.Alignment.LEFT ;

		private MalletColour colour ;
		private final MalletTexture sheet ;
		private final UIElement.UV uv ;

		protected Draw draw = null ;

		public GUIDraw( final MalletTexture _sheet, final UIElement.UV _uv )
		{
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

		public void setColour( final MalletColour _colour )
		{
			colour = ( _colour != null ) ? _colour : MalletColour.white() ;
			final Draw draw = getDraw() ;
			if( draw != null )
			{
				final Shape shape = DrawAssist.getDrawShape( getDraw() ) ;
				if( shape != null )
				{
					updateEdgeColour( shape, colour ) ;
				}
			}
		}

		/**
			Can be used to construct Draw objects before a 
			DrawDelegate is provided by the Rendering System.
		*/
		@Override
		public void constructDraws()
		{
			final T parent = getParent() ;
			updateLength( parent.getLength(), getLength() ) ;
			updateOffset( parent.getOffset(), getOffset() ) ;

			if( sheet != null && uv != null )
			{
				draw = DrawAssist.createDraw( getPosition(),
											  getOffset(),
											  new Vector3(),
											  new Vector3( 1, 1, 1 ),
											  parent.getLayer() ) ;
				DrawAssist.amendUI( draw, true ) ;
				DrawAssist.amendShape( draw, Shape.constructPlane( getLength(), uv.min, uv.max ) ) ;
				setColour( getColour() ) ;

				final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
				ProgramAssist.map( program, "inTex0", sheet ) ;

				DrawAssist.attachProgram( draw, program ) ;
			}

			super.constructDraws() ;
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
		}

		/**
			Only called if there is a valid DrawDelegate and 
			when the parent UIElement is flagged as invisible.
		*/
		@Override
		public void removeDraws( final DrawDelegate<World, Draw> _delegate )
		{
			_delegate.removeDraw( draw ) ;
		}

		@Override
		public void refresh()
		{
			final T parent = getParent() ;
			updateLength( parent.getLength(), getLength() ) ;
			updateOffset( parent.getOffset(), getOffset() ) ;

			if( draw != null )
			{
				DrawAssist.amendOrder( draw, parent.getLayer() ) ;
				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), getLength() ) ;
				DrawAssist.forceUpdate( draw ) ;
			}

			super.refresh() ;
		}

		private void updateLength( final Vector3 _length, final Vector3 _toUpdate )
		{
			if( uv == null || retainRatio == false )
			{
				_toUpdate.setXYZ( _length ) ;
				return ;
			}

			UI.calcSubDimension( aspectRatio, sheet, uv ) ;
			UI.fill( UI.Modifier.RETAIN_ASPECT_RATIO, _toUpdate, aspectRatio, _length ) ;
		}

		private void updateOffset( final Vector3 _offset, final Vector3 _toUpdate )
		{
			UI.align( drawAlignmentX, drawAlignmentY, _toUpdate, getLength(), getParent().getLength() ) ;
			_toUpdate.add( _offset ) ;
		}

		public void setDraw( final Draw _draw )
		{
			draw = _draw ;
		}

		public Draw getDraw()
		{
			return draw ;
		}

		public UI.Alignment getAlignmentX()
		{
			return drawAlignmentX ;
		}

		public UI.Alignment getAlignmentY()
		{
			return drawAlignmentY ;
		}

		public MalletColour getColour()
		{
			return colour ;
		}

		public MalletTexture getTexture()
		{
			return sheet ;
		}
	}

	public static class GUIText<T extends UIElement> extends GUIBase<T>
	{
		protected UI.Alignment drawAlignmentX = UI.Alignment.CENTRE ;
		protected UI.Alignment drawAlignmentY = UI.Alignment.CENTRE ;

		private final StringBuilder text = new StringBuilder() ;
		private MalletFont font ;
		private MalletColour colour = MalletColour.white() ;

		protected Draw drawText = null ;

		public GUIText( final String _text, final MalletFont _font )
		{
			font = _font ;
			if( _text != null )
			{
				text.append( _text ) ;
			}
		}

		public void setAlignment( final UI.Alignment _x, final UI.Alignment _y )
		{
			drawAlignmentX = ( _x == null ) ? UI.Alignment.CENTRE : _x ;
			drawAlignmentY = ( _y == null ) ? UI.Alignment.CENTRE : _y ;
		}

		public void setColour( final MalletColour _colour )
		{
			colour = ( _colour != null ) ? _colour : MalletColour.white() ;
		}

		/**
			Can be used to construct Draw objects before a 
			DrawDelegate is provided by the Rendering System.
		*/
		@Override
		public void constructDraws()
		{
			final T parent = getParent() ;
			final int layer = parent.getLayer() ;

			if( font != null )
			{
				final Vector3 length = getLength() ;
				final Vector3 offset = getOffset() ;

				final MalletFont.Metrics metrics = font.getMetrics() ;
				offset.x = UI.align( drawAlignmentX, font.stringWidth( text ), length.x ) ;
				offset.y = UI.align( drawAlignmentY, metrics.getHeight(), length.y ) ;

				drawText = DrawAssist.createTextDraw( text,
													  font,
													  getPosition(),
													  getOffset(),
													  new Vector3(),
													  new Vector3( 1, 1, 1 ),
													  layer + 1 ) ;
				DrawAssist.amendTextLength( drawText, font.stringIndexWidth( text, length.x ) ) ;
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
			_delegate.removeDraw( drawText ) ;
		}

		@Override
		public void refresh()
		{
			super.refresh() ;
			final T parent = getParent() ;

			if( drawText != null )
			{
				final Vector3 length = getLength() ;
				final Vector3 offset = getOffset() ;

				final MalletFont.Metrics metrics = font.getMetrics() ;
				offset.x = UI.align( drawAlignmentX, font.stringWidth( text ), length.x ) ;
				offset.y = UI.align( drawAlignmentY, metrics.getHeight(), length.y ) ;

				DrawAssist.amendTextLength( drawText, font.stringIndexWidth( text, length.x ) ) ;
				DrawAssist.amendOrder( drawText, parent.getLayer() + 1 ) ;
				DrawAssist.forceUpdate( drawText ) ;
			}
		}

		public StringBuilder getText()
		{
			return text ;
		}

		public UI.Alignment getAlignmentX()
		{
			return drawAlignmentX ;
		}

		public UI.Alignment getAlignmentY()
		{
			return drawAlignmentY ;
		}

		public Draw getDraw()
		{
			return drawText ;
		}

		public MalletFont getFont()
		{
			return font ;
		}

		public MalletColour getColour()
		{
			return colour ;
		}
	}
}
