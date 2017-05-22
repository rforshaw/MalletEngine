package com.linxonline.malleteditor.core ;

import com.linxonline.mallet.core.GlobalConfig ;

import com.linxonline.mallet.ui.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.core.* ;
import com.linxonline.mallet.animation.* ;
import com.linxonline.mallet.io.filesystem.* ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.entity.components.* ;

import com.linxonline.mallet.util.settings.Settings ;

public class UIEditorState extends GameState
{
	public UIEditorState( final String _name )
	{
		super( _name ) ;
	}

	@Override
	public void initGame()
	{
		final World edWorld = WorldAssist.getDefaultWorld() ;
		/*final World edWorld = WorldAssist.constructWorld( "EDITOR_WORLD", 1 ) ;
		final Camera edCamera = CameraAssist.createCamera( "EDITOR_CAMERA", new Vector3(),
																			new Vector3(),
																			new Vector3( 1, 1, 1 ) ) ;

		CameraAssist.addCamera( edCamera, edWorld ) ;*/

		final JUI jui = JUI.create( "base/ui/uieditor/main.jui" ) ;
		{
			final UIButton openProject = jui.get( "OpenButton", UIButton.class ) ;
			openProject.addListener( new InputListener<UIButton>()
			{
				@Override
				public InputEvent.Action mouseReleased( final InputEvent _input )
				{
					System.out.println( "Open Project..." ) ;
					return InputEvent.Action.CONSUME ;
				}
			} ) ;
			
			final UIButton saveProject = jui.get( "SaveButton", UIButton.class ) ;
			saveProject.addListener( new InputListener<UIButton>()
			{
				@Override
				public InputEvent.Action mouseReleased( final InputEvent _input )
				{
					System.out.println( "Save Project..." ) ;
					return InputEvent.Action.CONSUME ;
				}
			} ) ;

			final UIButton exitProject = jui.get( "ExitButton", UIButton.class ) ;
			exitProject.addListener( new InputListener<UIButton>()
			{
				@Override
				public InputEvent.Action mouseReleased( final InputEvent _input )
				{
					System.out.println( "Exit Project..." ) ;
					return InputEvent.Action.CONSUME ;
				}
			} ) ;

			createMainView( jui.get( "MainWindow", UIElement.class ), edWorld ) ;
		}

		final Entity entity = new Entity( "UI" ) ;
		final UIComponent component = new UIComponent() ;
		component.addElement( jui.getParent() ) ;

		entity.addComponent( component ) ;
		addEntity( entity ) ;
	}

	private static void createMainView( final UIElement _view, final World _world )
	{
		_view.addListener( new UIListener()
		{
			private Draw draw1 = null ;

			@Override
			public void constructDraws()
			{
				final UIElement parent = getParent() ;
				final Vector3 length = parent.getLength() ;

				draw1 = DrawAssist.createDraw( parent.getPosition(),
											   parent.getOffset(),
											   new Vector3(),
											   new Vector3( 1, 1, 1 ), parent.getLayer() + 1 ) ;

				DrawAssist.amendUI( draw1, true ) ;
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
			public void refresh() {}
		} ) ;
	}
}
