package com.linxonline.malleteditor.core ;

import java.util.List ;

import com.linxonline.mallet.core.GlobalConfig ;

import com.linxonline.mallet.ui.* ;
import com.linxonline.mallet.ui.gui.* ;
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

import com.linxonline.mallet.util.* ;
import com.linxonline.mallet.util.settings.Settings ;

public class UIEditorState extends GameState
{
	private UILayout mainView ;
	private UIList elementsView ;

	public UIEditorState( final String _name )
	{
		super( _name ) ;
	}

	@Override
	public void initGame()
	{
		final JUI jui = JUI.create( "base/ui/uieditor/main.jui" ) ;
		{
			final UIButton open = jui.get( "OpenButton", UIButton.class ) ;
			UIButton.connect( open, open.released(), new Connect.Slot<UIButton>()
			{
				final List<UIElement> elements = MalletList.<UIElement>newList() ;

				public void slot( final UIButton _open )
				{
					System.out.println( "Open Project..." ) ;
					mainView.getElements( elements ) ;
					for( UIElement element : elements )
					{
						mainView.removeElement( element ) ;
					}
					elements.clear() ;

					final UIWrapper wrapper = JUIWrapper.loadWrapper( "base/ui/test.jui" ) ;
					wrapper.setLayer( 1 ) ;

					mainView.addElement( wrapper ) ;
				}
			} ) ;

			final UIButton save = jui.get( "SaveButton", UIButton.class ) ;
			UIButton.connect( save, save.released(), new Connect.Slot<UIButton>()
			{
				public void slot( final UIButton _open )
				{
					System.out.println( "Save Project..." ) ;
				}
			} ) ;

			final UIButton exit = jui.get( "ExitButton", UIButton.class ) ;
			UIButton.connect( exit, exit.released(), new Connect.Slot<UIButton>()
			{
				public void slot( final UIButton _open )
				{
					System.out.println( "Exit Project..." ) ;
				}
			} ) ;

			mainView = jui.get( "MainWindow", UILayout.class ) ;
			//mainView.addElement( JUIWrapper.loadWrapper( "base/ui/test.jui" ) ) ;

			//createElementsPanel( jui.get( "UIElementsPanel", UIList.class ) ) ;
			//createStructurePanel( jui.get( "UIStructurePanel", UIList.class ) ) ;
			//createElementDataPanel( jui.get( "UIElementDataPanel", UIList.class ) ) ;
		}

		final Entity entity = new Entity( "UI" ) ;
		final UIComponent component = new UIComponent() ;
		component.addElement( jui.getParent() ) ;

		entity.addComponent( component ) ;
		addEntity( entity ) ;

		getInternalController().processEvent( new Event<Boolean>( "SHOW_GAME_STATE_FPS", true ) ) ;
	}

	/**
		A list of elements that can be added to a UI.
	*/
	private void createElementsPanel( final UIList _view )
	{
	
	}

	/**
		The tree structure of the currently active UI. 
	*/
	private void createStructurePanel( final UIList _view )
	{
	
	}

	/**
		The meta data of the currently selected element.
	*/
	private void createElementDataPanel( final UIList _view )
	{
	
	}
}
