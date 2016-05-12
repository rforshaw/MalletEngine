package com.linxonline.malleteditor.system ;

import com.linxonline.mallet.system.GlobalConfig ;
import com.linxonline.mallet.util.notification.Notification ;

import com.linxonline.mallet.game.GameState ;

import com.linxonline.mallet.ui.UIElement ;
import com.linxonline.mallet.ui.UILayout ;
import com.linxonline.mallet.ui.UIButton ;
import com.linxonline.mallet.ui.UISpacer ;

import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.entity.components.* ;

import com.linxonline.mallet.renderer.WorldAssist ;
import com.linxonline.mallet.renderer.World ;

import com.linxonline.mallet.renderer.CameraAssist ;
import com.linxonline.mallet.renderer.Camera ;

import com.linxonline.mallet.renderer.Shape ;
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
	private static final float TOOLBAR_HEIGHT = 40.0f ;
	private static final float SEPERATOR = 5.0f ;
	private static final float EDITOR_LIST_WIDTH = 40.0f ;

	public EditorState( final String _name )
	{
		super( _name ) ;
	}

	@Override
	public void initGame()
	{
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
		ui.addElement( createMainLayout() ) ;

		final Entity entity = new Entity() ;
		entity.addComponent( ui ) ;

		addEntity( entity ) ;
	}

	private UILayout createMainLayout()
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
			}
		} ) ;

		GlobalConfig.addNotify( "RENDERHEIGHT", new Notification.Notify<String>()
		{
			public void inform( final String _data )
			{
				dimension.y = GlobalConfig.getInteger( "RENDERHEIGHT", 640 ) ;
				layout.setLength( dimension.x, dimension.y, dimension.z ) ;
			}
		} ) ;

		layout.addElement( createHeaderToolbar() ) ;
		layout.addElement( createMainFrame() ) ;
		layout.addElement( createFooterToolbar() ) ;

		return layout ;
	}

	private static UILayout createHeaderToolbar()
	{
		final UILayout layout = new UILayout( UILayout.Type.HORIZONTAL ) ;
		layout.setMaximumLength( 0.0f, TOOLBAR_HEIGHT, 0.0f ) ;

		layout.addListener( new UILayout.Listener()
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
							delegate.addBasicDraw( draw ) ;
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

	private static UILayout createFooterToolbar()
	{
		final UILayout layout = new UILayout( UILayout.Type.HORIZONTAL ) ;
		layout.setMaximumLength( 0.0f, TOOLBAR_HEIGHT, 0.0f ) ;

		layout.addListener( new UILayout.Listener()
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
							delegate.addBasicDraw( draw ) ;
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

	private static UILayout createMainFrame()
	{
		final UILayout layout = new UILayout( UILayout.Type.HORIZONTAL ) ;

		layout.addElement( createEditorList() ) ;
		layout.addElement( createSeperator() ) ;
		layout.addElement( createMainView() ) ;

		return layout ;
	}

	private static UILayout createEditorList()
	{
		final UILayout layout = new UILayout( UILayout.Type.HORIZONTAL ) ;
		layout.setMinimumLength( 250.0f, 0.0f, 0.0f ) ;

		layout.addListener( new UILayout.Listener()
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
							delegate.addBasicDraw( draw ) ;
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

	private static UILayout createSeperator()
	{
		final UILayout layout = new UILayout( UILayout.Type.HORIZONTAL ) ;
		layout.setMaximumLength( SEPERATOR, 0.0f, 0.0f ) ;

		layout.addListener( new UILayout.Listener()
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
							delegate.addBasicDraw( draw ) ;
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

	private static UILayout createMainView()
	{
		final UILayout layout = new UILayout( UILayout.Type.HORIZONTAL ) ;

		layout.addListener( new UILayout.Listener()
		{
			private DrawDelegate delegate = null ;

			private World world = WorldAssist.constructWorld( "EDITOR_WORLD", 1 ) ;
			private Camera camera = CameraAssist.createCamera( "EDITOR_CAMERA", new Vector3(),
																				new Vector3(),
																				new Vector3( 1, 1, 1 ) ) ;

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
							delegate.addWorld( world ) ;
							delegate.addCamera( camera, world ) ;

							delegate.addBasicDraw( draw1, world ) ;
							//delegate.addBasicDraw( draw2, world ) ;
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
				
				CameraAssist.amendOrthographic( camera, 0.0f, length.y, 0.0f, length.x, -1000.0f, 1000.0f ) ;
				CameraAssist.amendScreenResolution( camera, ( int )length.x, ( int )length.y ) ;
				CameraAssist.amendScreenOffset( camera, ( int )offset.x, ( int )offset.y ) ;
			}

			@Override
			public void refresh()
			{
				final UIElement parent = getParent() ;
				final Vector3 offset = Vector3.add( parent.getPosition(), parent.getOffset() ) ;
				final Vector3 length = parent.getLength() ;

				CameraAssist.amendOrthographic( camera, 0.0f, length.y, 0.0f, length.x, -1000.0f, 1000.0f ) ;
				CameraAssist.amendScreenResolution( camera, ( int )length.x, ( int )length.y ) ;
				CameraAssist.amendScreenOffset( camera, ( int )offset.x, ( int )offset.y ) ;

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