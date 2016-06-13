package com.linxonline.malleteditor.system ;

import com.linxonline.mallet.system.GlobalConfig ;
import com.linxonline.mallet.util.notification.Notification ;

import com.linxonline.mallet.game.GameState ;

import com.linxonline.mallet.ui.BaseListener ;
import com.linxonline.mallet.ui.UIElement ;
import com.linxonline.mallet.ui.UILayout ;
import com.linxonline.mallet.ui.UIButton ;
import com.linxonline.mallet.ui.UISpacer ;
import com.linxonline.mallet.ui.UIMenu ;

import com.linxonline.mallet.input.InputEvent ;

import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.entity.components.* ;

import com.linxonline.mallet.renderer.WorldAssist ;
import com.linxonline.mallet.renderer.World ;

import com.linxonline.mallet.renderer.CameraAssist ;
import com.linxonline.mallet.renderer.Camera ;

import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.MalletColour ;

import com.linxonline.mallet.renderer.UpdateType ;
import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class EditorState extends GameState
{
	private static final float TOOLBAR_HEIGHT = 30.0f ;
	private static final float SEPERATOR = 5.0f ;
	private static final float EDITOR_LIST_WIDTH = 40.0f ;

	private final World edWorld = WorldAssist.constructWorld( "EDITOR_WORLD", 1 ) ;
	private final World uiWorld = WorldAssist.constructWorld( "UI_EDITOR_WORLD", 2 ) ;

	private final Camera edCamera = CameraAssist.createCamera( "EDITOR_CAMERA", new Vector3(),
																				 new Vector3(),
																				 new Vector3( 1, 1, 1 ) ) ;
	private final Camera uiCamera = CameraAssist.createCamera( "UI_EDITOR_CAMERA", new Vector3(),
																					new Vector3(),
																					new Vector3( 1, 1, 1 ) ) ;

	public EditorState( final String _name )
	{
		super( _name ) ;
	}

	@Override
	public void initGame()
	{
		CameraAssist.addCamera( edCamera, edWorld ) ;
		CameraAssist.addCamera( uiCamera, uiWorld ) ;

		loadDefaultUILayout() ;
	}

	@Override
	protected void initModes()
	{
		useApplicationMode() ;
	}

	private void loadDefaultUILayout()
	{
		final UIComponent ui = new UIComponent() ;
		ui.addElement( createMainLayout( ui ) ) ;

		final Entity entity = new Entity() ;
		entity.addComponent( ui ) ;

		addEntity( entity ) ;
	}

	private UILayout createMainLayout( final UIComponent _ui )
	{
		final int width = GlobalConfig.getInteger( "RENDERWIDTH", 640 ) ;
		final int height = GlobalConfig.getInteger( "RENDERHEIGHT", 480 ) ;

		final Vector3 dimension = new Vector3( width, height, 0.0f ) ;
		final UILayout layout = new UILayout( UILayout.Type.VERTICAL,
											  new Vector3(),
											  new Vector3(),
											  dimension ) ;

		GlobalConfig.addNotify( "RENDERWIDTH", new Notification.Notify<String>()
		{
			public void inform( final String _data )
			{
				dimension.x = GlobalConfig.getInteger( "RENDERWIDTH", 640 ) ;
				layout.setLength( dimension.x, dimension.y, dimension.z ) ;

				CameraAssist.amendOrthographic( uiCamera, 0.0f, dimension.y, 0.0f, dimension.x, -1000.0f, 1000.0f ) ;
				CameraAssist.amendScreenResolution( uiCamera, ( int )dimension.x, ( int )dimension.y ) ;
			}
		} ) ;

		GlobalConfig.addNotify( "RENDERHEIGHT", new Notification.Notify<String>()
		{
			public void inform( final String _data )
			{
				dimension.y = GlobalConfig.getInteger( "RENDERHEIGHT", 640 ) ;
				layout.setLength( dimension.x, dimension.y, dimension.z ) ;

				CameraAssist.amendOrthographic( uiCamera, 0.0f, dimension.y, 0.0f, dimension.x, -1000.0f, 1000.0f ) ;
				CameraAssist.amendScreenResolution( uiCamera, ( int )dimension.x, ( int )dimension.y ) ;
			}
		} ) ;

		layout.addElement( createHeaderToolbar( uiWorld, _ui ) ) ;
		layout.addElement( createMainFrame( edCamera, edWorld, uiWorld ) ) ;
		layout.addElement( createFooterToolbar( uiWorld ) ) ;

		return layout ;
	}

	private static UIMenu createHeaderToolbar( final World _world, final UIComponent _ui )
	{
		final UIMenu layout = new UIMenu( UILayout.Type.HORIZONTAL, TOOLBAR_HEIGHT ) ;

		layout.addListener( new BaseListener()
		{
			private DrawDelegate delegate = null ;
			private Draw draw = null ;

			@Override
			public void setParent( final UIElement _parent )
			{
				super.setParent( _parent ) ;
				_parent.addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
				{
					public void callback( DrawDelegate _delegate )
					{
						delegate = _delegate ;
						if( draw != null )
						{
							delegate.addBasicDraw( draw, _world ) ;
						}
					}
				} ) ) ;

				final Vector3 length = _parent.getLength() ;

				draw = DrawAssist.createDraw( _parent.getPosition(),
											  _parent.getOffset(),
											  new Vector3(),
											  new Vector3( 1, 1, 1 ), _parent.getLayer() ) ;
				DrawAssist.amendUI( draw, true ) ;
				DrawAssist.amendShape( draw, Shape.constructPlane( length, MalletColour.red() ) ) ;
				DrawAssist.attachProgram( draw, "SIMPLE_GEOMETRY" ) ;
			}

			@Override
			public void refresh()
			{
				final Vector3 length = getParent().getLength() ;
				final Vector3 offset = getParent().getOffset() ;

				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), length ) ;
				DrawAssist.forceUpdate( draw ) ;
			}

			@Override
			public void shutdown()
			{
				if( delegate != null )
				{
					delegate.shutdown() ;
				}
			}
		} ) ;

		addHeaderItem( _world, _ui, layout, "File" ) ;
		addHeaderItem( _world, _ui, layout, "Edit" ) ;
		addHeaderItem( _world, _ui, layout, "View" ) ;
		addHeaderItem( _world, _ui, layout, "Tools" ) ;
		addHeaderItem( _world, _ui, layout, "Help" ) ;
		layout.addElement( new UISpacer() ) ;

		return layout ;
	}

	private static UIMenu createDropDown( final World _world,
										  final UIComponent _ui,
										  final int _layer,
										  final float _x,
										  final float _y )
	{
		final UIMenu layout = new UIMenu( UILayout.Type.VERTICAL, 150.0f ) ;
		layout.setLayer( _layer ) ;
		layout.setPosition( _x, _y, 0.0f ) ;
		layout.setLength( 150.0f, 100.0f, 0.0f ) ;

		layout.addListener( new BaseListener()
		{
			private DrawDelegate delegate = null ;
			private Draw draw = null ;

			@Override
			public void setParent( final UIElement _parent )
			{
				super.setParent( _parent ) ;
				_parent.addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
				{
					public void callback( DrawDelegate _delegate )
					{
						delegate = _delegate ;
						if( draw != null )
						{
							delegate.addBasicDraw( draw, _world ) ;
						}
					}
				} ) ) ;

				final Vector3 length = _parent.getLength() ;
				System.out.println( "Dropdown Length: " + length ) ;

				draw = DrawAssist.createDraw( _parent.getPosition(),
											  _parent.getOffset(),
											  new Vector3(),
											  new Vector3( 1, 1, 1 ), _parent.getLayer() ) ;
				DrawAssist.amendUI( draw, true ) ;
				DrawAssist.amendShape( draw, Shape.constructPlane( length, MalletColour.red() ) ) ;
				DrawAssist.attachProgram( draw, "SIMPLE_GEOMETRY" ) ;
			}

			public InputEvent.Action exited( final InputEvent _input )
			{
				System.out.println( "Exited dropdown" ) ;
				getParent().destroy() ;
				return InputEvent.Action.PROPAGATE ;
			}

			@Override
			public void refresh()
			{
				final UIElement parent = getParent() ;
				final Vector3 length = parent.getLength() ;
				final Vector3 offset = parent.getOffset() ;
				System.out.println( "Refresh dropdown menu: " + length ) ;

				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), length ) ;
				DrawAssist.forceUpdate( draw ) ;
			}

			@Override
			public void shutdown()
			{
				if( delegate != null )
				{
					delegate.shutdown() ;
				}
			}
		} ) ;

		addHeaderItem( _world, _ui, layout, "Test 1" ) ;
		addHeaderItem( _world, _ui, layout, "Test 2" ) ;

		return layout ;
	}

	private static void addHeaderItem( final World _world,
									   final UIComponent _ui,
									   final UILayout _toolbar,
									   final String _text )
	{
		final MalletFont font = new MalletFont( "Arial", 12 ) ;
		final int height = font.getHeight() ;
		final int width = font.stringWidth( _text ) ;

		final UIMenu.Item item = new UIMenu.Item() ;
		item.setMaximumLength( width + 20, 0.0f, 0.0f ) ;

		item.addListener( new BaseListener()
		{
			private UIMenu dropdown = null ;

			private DrawDelegate delegate = null ;
			private Draw draw = null ;
			private Draw drawText = null ;

			@Override
			public void setParent( final UIElement _parent )
			{
				super.setParent( _parent ) ;
				_parent.addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
				{
					public void callback( DrawDelegate _delegate )
					{
						delegate = _delegate ;
						if( draw != null )
						{
							delegate.addBasicDraw( draw, _world ) ;
						}

						if( drawText != null )
						{
							delegate.addTextDraw( drawText, _world ) ;
						}
					}
				} ) ) ;

				final Vector3 length = _parent.getLength() ;

				draw = DrawAssist.createDraw( _parent.getPosition(),
											  _parent.getOffset(),
											  new Vector3(),
											  new Vector3( 1, 1, 1 ), _parent.getLayer() + 1 ) ;
				DrawAssist.amendUI( draw, true ) ;
				DrawAssist.amendShape( draw, Shape.constructPlane( length, MalletColour.blue() ) ) ;
				DrawAssist.attachProgram( draw, "SIMPLE_GEOMETRY" ) ;

				final Vector3 textOffset = new Vector3( _parent.getOffset() ) ;
				textOffset.add( length.x / 2, length.y / 2, 0.0f ) ;

				drawText = DrawAssist.createTextDraw( _text,
													  font,
													  _parent.getPosition(),
													  textOffset,
													  new Vector3(),
													  new Vector3( 1, 1, 1 ), _parent.getLayer() + 2 ) ;
				DrawAssist.amendUI( drawText, true ) ;
				DrawAssist.attachProgram( drawText, "SIMPLE_FONT" ) ;
			}

			@Override
			public InputEvent.Action released( final InputEvent _input )
			{
				if( dropdown != null )
				{
					dropdown.destroy() ;
				}

				final UIElement parent = getParent() ;
				final Vector3 position = parent.getPosition() ;
				final Vector3 length = parent.getLength() ;
				final int layer = parent.getLayer() + 1 ;

				dropdown = createDropDown( _world, _ui, layer, position.x, position.y + length.y ) ;
				_ui.addElement( dropdown ) ;

				parent.makeDirty() ;
				return InputEvent.Action.CONSUME ;
			}

			@Override
			public void refresh()
			{
				final Vector3 length = getParent().getLength() ;
				final Vector3 offset = getParent().getOffset() ;

				{
					Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), length ) ;
					DrawAssist.forceUpdate( draw ) ;
				}

				{
					DrawAssist.amendOffset( drawText, offset.x + ( length.x / 2 ) - ( width / 2 ), offset.y + ( length.y / 2 ) - ( height / 2 ), 0.0f ) ;
					DrawAssist.forceUpdate( drawText ) ;
				}

				if( dropdown != null )
				{
					dropdown.makeDirty() ;
				}
			}

			@Override
			public void shutdown()
			{
				if( delegate != null )
				{
					delegate.shutdown() ;
				}
			}
		} ) ;

		_toolbar.addElement( item ) ;
	}

	private static UILayout createFooterToolbar( final World _world )
	{
		final UILayout layout = new UILayout( UILayout.Type.HORIZONTAL ) ;
		layout.setMaximumLength( 0.0f, TOOLBAR_HEIGHT, 0.0f ) ;

		layout.addListener( new BaseListener()
		{
			private DrawDelegate delegate = null ;
			private Draw draw = null ;

			@Override
			public void setParent( final UIElement _parent )
			{
				super.setParent( _parent ) ;
				_parent.addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
				{
					public void callback( DrawDelegate _delegate )
					{
						delegate = _delegate ;
						if( draw != null )
						{
							delegate.addBasicDraw( draw, _world ) ;
						}
					}
				} ) ) ;

				final Vector3 length = _parent.getLength() ;

				draw = DrawAssist.createDraw( _parent.getPosition(),
											  _parent.getOffset(),
											  new Vector3(),
											  new Vector3( 1, 1, 1 ), _parent.getLayer() ) ;
				DrawAssist.amendUI( draw, true ) ;
				DrawAssist.amendShape( draw, Shape.constructPlane( length, MalletColour.red() ) ) ;
				DrawAssist.attachProgram( draw, "SIMPLE_GEOMETRY" ) ;
			}

			@Override
			public void refresh()
			{
				final Vector3 length = getParent().getLength() ;
				final Vector3 offset = getParent().getOffset() ;

				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), length ) ;
				DrawAssist.forceUpdate( draw ) ;
			}

			@Override
			public void shutdown()
			{
				if( delegate != null )
				{
					delegate.shutdown() ;
				}
			}
		} ) ;

		return layout ;
	}

	private static UILayout createMainFrame( final Camera _edCamera, final World _edWorld, final World _uiWorld )
	{
		final UILayout layout = new UILayout( UILayout.Type.HORIZONTAL ) ;

		layout.addElement( createEditorList( _uiWorld ) ) ;
		layout.addElement( createSeperator( _uiWorld ) ) ;
		layout.addElement( createMainView( _edCamera, _edWorld ) ) ;

		return layout ;
	}

	private static UILayout createEditorList( final World _world )
	{
		final UILayout layout = new UILayout( UILayout.Type.HORIZONTAL ) ;
		layout.setMinimumLength( 250.0f, 0.0f, 0.0f ) ;

		layout.addListener( new BaseListener()
		{
			private DrawDelegate delegate = null ;
			private Draw draw = null ;

			@Override
			public void setParent( final UIElement _parent )
			{
				super.setParent( _parent ) ;
				_parent.addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
				{
					public void callback( DrawDelegate _delegate )
					{
						delegate = _delegate ;
						if( draw != null )
						{
							delegate.addBasicDraw( draw, _world ) ;
						}
					}
				} ) ) ;

				final Vector3 length = _parent.getLength() ;

				draw = DrawAssist.createDraw( _parent.getPosition(),
											  _parent.getOffset(),
											  new Vector3(),
											  new Vector3( 1, 1, 1 ), _parent.getLayer() ) ;
				DrawAssist.amendUI( draw, true ) ;
				DrawAssist.amendShape( draw, Shape.constructPlane( length, MalletColour.blue() ) ) ;
				DrawAssist.attachProgram( draw, "SIMPLE_GEOMETRY" ) ;
			}

			@Override
			public void refresh()
			{
				final Vector3 length = getParent().getLength() ;
				final Vector3 offset = getParent().getOffset() ;

				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), length ) ;
				DrawAssist.forceUpdate( draw ) ;
			}

			@Override
			public void shutdown()
			{
				if( delegate != null )
				{
					delegate.shutdown() ;
				}
			}
		} ) ;

		return layout ;
	}

	private static UILayout createSeperator( final World _world )
	{
		final UILayout layout = new UILayout( UILayout.Type.HORIZONTAL ) ;
		layout.setMaximumLength( SEPERATOR, 0.0f, 0.0f ) ;

		layout.addListener( new BaseListener()
		{
			private DrawDelegate delegate = null ;
			private Draw draw = null ;

			@Override
			public void setParent( final UIElement _parent )
			{
				super.setParent( _parent ) ;
				_parent.addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
				{
					public void callback( DrawDelegate _delegate )
					{
						delegate = _delegate ;
						if( draw != null )
						{
							delegate.addBasicDraw( draw, _world ) ;
						}
					}
				} ) ) ;

				final Vector3 length = _parent.getLength() ;

				draw = DrawAssist.createDraw( _parent.getPosition(),
											  _parent.getOffset(),
											  new Vector3(),
											  new Vector3( 1, 1, 1 ), _parent.getLayer() ) ;
				DrawAssist.amendUI( draw, true ) ;
				DrawAssist.amendShape( draw, Shape.constructPlane( length, MalletColour.white() ) ) ;
				DrawAssist.attachProgram( draw, "SIMPLE_GEOMETRY" ) ;
			}

			@Override
			public void refresh()
			{
				final Vector3 length = getParent().getLength() ;
				final Vector3 offset = getParent().getOffset() ;

				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), length ) ;
				DrawAssist.forceUpdate( draw ) ;
			}

			@Override
			public void shutdown()
			{
				if( delegate != null )
				{
					delegate.shutdown() ;
				}
			}
		} ) ;

		return layout ;
	}

	private static UILayout createMainView( final Camera _camera, final World _world )
	{
		final UILayout layout = new UILayout( UILayout.Type.HORIZONTAL ) ;

		layout.addListener( new BaseListener()
		{
			private DrawDelegate delegate = null ;
			private Draw draw1 = null ;
			private Draw draw2 = null ;

			@Override
			public void setParent( final UIElement _parent )
			{
				super.setParent( _parent ) ;
				_parent.addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
				{
					public void callback( DrawDelegate _delegate )
					{
						delegate = _delegate ;
						if( draw1 != null && draw2 != null )
						{
							delegate.addBasicDraw( draw1, _world ) ;
						}
					}
				} ) ) ;

				final Vector3 offset = Vector3.add( _parent.getPosition(), _parent.getOffset() ) ;
				final Vector3 length = _parent.getLength() ;

				draw1 = DrawAssist.createDraw( new Vector3(),
												new Vector3(),
												new Vector3(),
												new Vector3( 1, 1, 1 ), _parent.getLayer() ) ;

				DrawAssist.amendShape( draw1, Shape.constructPlane( new Vector3( 10, 10, 0 ), MalletColour.blue() ) ) ;
				DrawAssist.attachProgram( draw1, "SIMPLE_GEOMETRY" ) ;

				draw2 = DrawAssist.createDraw( _parent.getPosition(),
												_parent.getOffset(),
												new Vector3(),
												new Vector3( 1, 1, 1 ), _parent.getLayer() ) ;

				DrawAssist.amendUI( draw2, true ) ;
				DrawAssist.amendShape( draw2, Shape.constructPlane( length, MalletColour.blue() ) ) ;
				DrawAssist.attachProgram( draw2, "SIMPLE_GEOMETRY" ) ;

				CameraAssist.amendOrthographic( _camera, 0.0f, length.y, 0.0f, length.x, -1000.0f, 1000.0f ) ;
				CameraAssist.amendScreenResolution( _camera, ( int )length.x, ( int )length.y ) ;
				CameraAssist.amendScreenOffset( _camera, ( int )offset.x, ( int )offset.y ) ;
			}

			@Override
			public void refresh()
			{
				final UIElement parent = getParent() ;
				final Vector3 offset = Vector3.add( parent.getPosition(), parent.getOffset() ) ;
				final Vector3 length = parent.getLength() ;

				CameraAssist.amendOrthographic( _camera, 0.0f, length.y, 0.0f, length.x, -1000.0f, 1000.0f ) ;
				CameraAssist.amendScreenResolution( _camera, ( int )length.x, ( int )length.y ) ;
				CameraAssist.amendScreenOffset( _camera, ( int )offset.x, ( int )offset.y ) ;

				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw2 ), length ) ;
				DrawAssist.forceUpdate( draw2 ) ;
			}

			@Override
			public void shutdown()
			{
				if( delegate != null )
				{
					delegate.shutdown() ;
				}
			}
		} ) ;

		return layout ;
	}
}