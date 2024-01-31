package com.linxonline.malleteditor.core ;

import com.linxonline.mallet.core.GlobalConfig ;
import com.linxonline.mallet.util.notification.Notification ;

import com.linxonline.mallet.core.GameState ;

import com.linxonline.mallet.ui.gui.GUIComponent ;
import com.linxonline.mallet.ui.UIElement ;
import com.linxonline.mallet.ui.UIFactory ;
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

import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.Program ;

import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.MalletColour ;

import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class EditorState extends GameState
{
	private static final float TOOLBAR_HEIGHT = 30.0f ;
	private static final float SEPERATOR = 5.0f ;
	private static final float EDITOR_LIST_WIDTH = 40.0f ;

	public EditorState( final String _name )
	{
		super( _name ) ;
	}

	@Override
	public void initGame()
	{
		final World edWorld = WorldAssist.add( new World( "EDITOR_WORLD" ) ) ;
		final World uiWorld = WorldAssist.add( new World( "UI_EDITOR_WORLD" ) ) ;

		final Camera edCamera = CameraAssist.add( new Camera( "EDITOR_CAMERA" ) ) ;
		final Camera uiCamera = CameraAssist.add( new Camera( "UI_EDITOR_CAMERA" ) ) ;
	
		edWorld.addCameras( edCamera ) ;
		uiWorld.addCameras( uiCamera ) ;

		loadDefaultUILayout() ;
	}

	private void loadDefaultUILayout()
	{
		/*final UIComponent ui = new UIComponent() ;
		ui.addElement( createMainLayout( ui ) ) ;

		final Entity entity = new Entity() ;
		entity.addComponent( ui ) ;

		addEntity( entity ) ;*/
	}

	/*private UILayout createMainLayout( final UIComponent _ui )
	{
		final UILayout layout = UIFactory.constructWindowLayout( UILayout.Type.VERTICAL, uiCamera ) ;

		layout.addElement( createHeaderToolbar( uiWorld, _ui ) ) ;
		layout.addElement( createMainFrame( edCamera, edWorld, uiWorld ) ) ;
		layout.addElement( createFooterToolbar( uiWorld ) ) ;

		return layout ;
	}

	private static UIMenu createHeaderToolbar( final World _world, final UIComponent _ui )
	{
		final UIMenu layout = new UIMenu( UILayout.Type.HORIZONTAL, TOOLBAR_HEIGHT ) ;

		layout.addListener( new GUIBase()
		{
			private Draw draw = null ;

			@Override
			public void constructDraws()
			{
				final UIElement parent = getParent() ;
				draw = DrawAssist.createDraw( parent.getPosition(),
											  parent.getOffset(),
											  new Vector3(),
											  new Vector3( 1, 1, 1 ), parent.getLayer() ) ;
				DrawAssist.amendUI( draw, true ) ;
				DrawAssist.amendShape( draw, Shape.constructPlane( parent.getLength(), MalletColour.red() ) ) ;
				DrawAssist.attachProgram( draw, ProgramAssist.create( "SIMPLE_GEOMETRY" ) ) ;
			}

			@Override
			public void addDraws( final DrawDelegate _delegate )
			{
				_delegate.addBasicDraw( draw, _world ) ;
			}

			@Override
			public void removeDraws( final DrawDelegate _delegate )
			{
				_delegate.removeDraw( draw ) ;
			}

			@Override
			public void refresh()
			{
				final Vector3 length = getParent().getLength() ;
				final Vector3 offset = getParent().getOffset() ;

				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), length ) ;
				DrawAssist.forceUpdate( draw ) ;
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

		layout.addListener( new GUIBase()
		{
			private Draw draw = null ;

			@Override
			public void constructDraws()
			{
				final UIElement parent = getParent() ;

				final Vector3 length = parent.getLength() ;
				draw = DrawAssist.createDraw( parent.getPosition(),
											  parent.getOffset(),
											  new Vector3(),
											  new Vector3( 1, 1, 1 ), parent.getLayer() ) ;
				DrawAssist.amendUI( draw, true ) ;
				DrawAssist.amendShape( draw, Shape.constructPlane( length, MalletColour.red() ) ) ;
				DrawAssist.attachProgram( draw, ProgramAssist.create( "SIMPLE_GEOMETRY" ) ) ;
			}

			@Override
			public void addDraws( final DrawDelegate _delegate )
			{
				_delegate.addBasicDraw( draw, _world ) ;
			}

			@Override
			public void removeDraws( final DrawDelegate _delegate )
			{
				_delegate.removeDraw( draw ) ;
			}

			public InputEvent.Action exited( final InputEvent _input )
			{
				getParent().destroy() ;
				return InputEvent.Action.PROPAGATE ;
			}

			@Override
			public void refresh()
			{
				final UIElement parent = getParent() ;
				final Vector3 length = parent.getLength() ;
				final Vector3 offset = parent.getOffset() ;

				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), length ) ;
				DrawAssist.forceUpdate( draw ) ;
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
		final MalletFont.Metrics metrics = font.getMetrics() ;

		final float height = metrics.getHeight() ;
		final float width = font.stringWidth( _text ) ;

		final UIMenu.Item item = new UIMenu.Item( null ) ;
		item.setMaximumLength( width + 20.0f, 0.0f, 0.0f ) ;

		item.addListener( new GUIBase()
		{
			private UIMenu dropdown = null ;

			private Draw draw = null ;
			private Draw drawText = null ;

			@Override
			public void constructDraws()
			{
				final UIElement parent = getParent() ;
				final Vector3 length = parent.getLength() ;

				draw = DrawAssist.createDraw( parent.getPosition(),
											  parent.getOffset(),
											  new Vector3(),
											  new Vector3( 1, 1, 1 ), parent.getLayer() + 1 ) ;
				DrawAssist.amendUI( draw, true ) ;
				DrawAssist.amendShape( draw, Shape.constructPlane( length, MalletColour.blue() ) ) ;
				DrawAssist.attachProgram( draw, ProgramAssist.create( "SIMPLE_GEOMETRY" ) ) ;

				final Vector3 textOffset = new Vector3( parent.getOffset() ) ;
				textOffset.add( length.x / 2, length.y / 2, 0.0f ) ;

				drawText = DrawAssist.createTextDraw( _text,
													  font,
													  parent.getPosition(),
													  textOffset,
													  new Vector3(),
													  new Vector3( 1, 1, 1 ), parent.getLayer() + 2 ) ;
				DrawAssist.amendUI( drawText, true ) ;
				DrawAssist.attachProgram( drawText, ProgramAssist.create( "SIMPLE_FONT" ) ) ;
			}

			@Override
			public void addDraws( final DrawDelegate _delegate )
			{
				_delegate.addBasicDraw( draw, _world ) ;
				_delegate.addTextDraw( drawText, _world ) ;
			}

			@Override
			public void removeDraws( final DrawDelegate _delegate )
			{
				_delegate.removeDraw( draw ) ;
				_delegate.removeDraw( drawText ) ;
			}

			@Override
			public InputEvent.Action mouseReleased( final InputEvent _input )
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
		} ) ;

		_toolbar.addElement( item ) ;
	}

	private static UILayout createFooterToolbar( final World _world )
	{
		final UILayout layout = new UILayout( UILayout.Type.HORIZONTAL ) ;
		layout.setMaximumLength( 0.0f, TOOLBAR_HEIGHT, 0.0f ) ;

		layout.addListener( new GUIBase()
		{
			private Draw draw = null ;

			@Override
			public void constructDraws()
			{
				final UIElement parent = getParent() ;
				final Vector3 length = parent.getLength() ;

				draw = DrawAssist.createDraw( parent.getPosition(),
											  parent.getOffset(),
											  new Vector3(),
											  new Vector3( 1, 1, 1 ), parent.getLayer() ) ;
				DrawAssist.amendUI( draw, true ) ;
				DrawAssist.amendShape( draw, Shape.constructPlane( length, MalletColour.red() ) ) ;
				DrawAssist.attachProgram( draw, ProgramAssist.create( "SIMPLE_GEOMETRY" ) ) ;
			}

			@Override
			public void addDraws( final DrawDelegate _delegate )
			{
				_delegate.addBasicDraw( draw, _world ) ;
			}

			@Override
			public void removeDraws( final DrawDelegate _delegate )
			{
				_delegate.removeDraw( draw ) ;
			}

			@Override
			public void refresh()
			{
				final Vector3 length = getParent().getLength() ;
				final Vector3 offset = getParent().getOffset() ;

				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), length ) ;
				DrawAssist.forceUpdate( draw ) ;
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

		layout.addListener( new GUIBase()
		{
			private Draw draw = null ;

			@Override
			public void constructDraws()
			{
				final UIElement parent = getParent() ;
				final Vector3 length = parent.getLength() ;

				draw = DrawAssist.createDraw( parent.getPosition(),
											  parent.getOffset(),
											  new Vector3(),
											  new Vector3( 1, 1, 1 ), parent.getLayer() ) ;
				DrawAssist.amendUI( draw, true ) ;
				DrawAssist.amendShape( draw, Shape.constructPlane( length, MalletColour.blue() ) ) ;
				DrawAssist.attachProgram( draw, ProgramAssist.create( "SIMPLE_GEOMETRY" ) ) ;
			}

			@Override
			public void addDraws( final DrawDelegate _delegate )
			{
				_delegate.addBasicDraw( draw, _world ) ;
			}

			@Override
			public void removeDraws( final DrawDelegate _delegate )
			{
				_delegate.removeDraw( draw ) ;
			}

			@Override
			public void refresh()
			{
				final Vector3 length = getParent().getLength() ;
				final Vector3 offset = getParent().getOffset() ;

				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), length ) ;
				DrawAssist.forceUpdate( draw ) ;
			}
		} ) ;

		return layout ;
	}

	private static UILayout createSeperator( final World _world )
	{
		final UILayout layout = new UILayout( UILayout.Type.HORIZONTAL ) ;
		layout.setMaximumLength( SEPERATOR, 0.0f, 0.0f ) ;

		layout.addListener( new GUIBase()
		{
			private Draw draw = null ;

			@Override
			public void constructDraws()
			{
				final UIElement parent = getParent() ;
				final Vector3 length = parent.getLength() ;

				draw = DrawAssist.createDraw( parent.getPosition(),
											  parent.getOffset(),
											  new Vector3(),
											  new Vector3( 1, 1, 1 ), parent.getLayer() ) ;
				DrawAssist.amendUI( draw, true ) ;
				DrawAssist.amendShape( draw, Shape.constructPlane( length, MalletColour.white() ) ) ;
				DrawAssist.attachProgram( draw, ProgramAssist.create( "SIMPLE_GEOMETRY" ) ) ;
			}

			@Override
			public void addDraws( final DrawDelegate _delegate )
			{
				_delegate.addBasicDraw( draw, _world ) ;
			}

			@Override
			public void removeDraws( final DrawDelegate _delegate )
			{
				_delegate.removeDraw( draw ) ;
			}

			@Override
			public void refresh()
			{
				final Vector3 length = getParent().getLength() ;
				final Vector3 offset = getParent().getOffset() ;

				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), length ) ;
				DrawAssist.forceUpdate( draw ) ;
			}
		} ) ;

		return layout ;
	}

	private static UILayout createMainView( final Camera _camera, final World _world )
	{
		final UILayout layout = new UILayout( UILayout.Type.HORIZONTAL ) ;

		layout.addListener( new GUIBase()
		{
			private Draw draw1 = null ;

			@Override
			public void constructDraws()
			{
				final UIElement parent = getParent() ;
				final Vector3 offset = Vector3.add( parent.getPosition(), parent.getOffset() ) ;
				final Vector3 length = parent.getLength() ;

				draw1 = DrawAssist.createDraw( new Vector3(),
												new Vector3(),
												new Vector3(),
												new Vector3( 1, 1, 1 ), parent.getLayer() ) ;

				DrawAssist.amendShape( draw1, Shape.constructPlane( new Vector3( 10, 10, 0 ), MalletColour.blue() ) ) ;
				DrawAssist.attachProgram( draw1, ProgramAssist.create( "SIMPLE_GEOMETRY" ) ) ;
			}

			@Override
			public void addDraws( final DrawDelegate _delegate )
			{
				_delegate.addBasicDraw( draw1, _world ) ;
			}

			@Override
			public void removeDraws( final DrawDelegate _delegate )
			{
				_delegate.removeDraw( draw1 ) ;
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
			}
		} ) ;

		return layout ;
	}*/
}
