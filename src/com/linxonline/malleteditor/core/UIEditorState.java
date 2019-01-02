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
import com.linxonline.mallet.entity.components.AnimComponent ;
import com.linxonline.mallet.entity.components.EventComponent ;
import com.linxonline.mallet.entity.components.MouseComponent ;
import com.linxonline.mallet.entity.components.UIComponent ;

import com.linxonline.mallet.util.* ;
import com.linxonline.mallet.util.settings.Settings ;

public class UIEditorState extends GameState
{
	private UILayout mainView ;
	private UIList elementOptionsPanel ;
	private UIList guiOptionsPanel ;
	private UIList elementStructurePanel ;
	private UIAbstractView elementDataPanel ;
	private UIList componentDataPanel ;

	private UIWrapper root = null ;

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
					cleanup( mainView ) ;

					root = JUIWrapper.loadWrapper( "base/ui/test.jui" ) ;
					root.setLayer( 1 ) ;

					mainView.addElement( root ) ;
				}

				/**
					Remove any elements that are stored in the passed 
					in layout.
				*/
				private void cleanup( final UILayout _view )
				{
					_view.getElements( elements ) ;
					for( UIElement element : elements )
					{
						_view.removeElement( element ) ;
					}
					elements.clear() ;
				}
			} ) ;

			final UIButton save = jui.get( "SaveButton", UIButton.class ) ;
			UIButton.connect( save, save.released(), new Connect.Slot<UIButton>()
			{
				public void slot( final UIButton _open )
				{
					System.out.println( "Save Project..." ) ;
					JUIWrapper.saveWrapper( root, "test_save.jui" ) ;
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
			elementOptionsPanel = jui.get( "UIElementsPanel", UIList.class ) ;
			guiOptionsPanel = jui.get( "GUIPanel", UIList.class ) ;
			elementStructurePanel = jui.get( "UIStructurePanel", UIList.class ) ;
			elementDataPanel = jui.get( "UIElementDataPanel", UIAbstractView.class ) ;
			componentDataPanel = jui.get( "UIComponentDataPanel", UIList.class ) ;
			componentDataPanel.setDefaultElementSize( 0.0f, 4.0f, 0.0f ) ;

			createElementsPanel( elementOptionsPanel ) ;
			createCUIsPanel( guiOptionsPanel ) ;
		}

		final Entity entity = new Entity( "UI" ) ;
		final UIComponent component = new UIComponent( entity ) ;
		component.addElement( jui.getParent() ) ;

		addEntity( entity ) ;

		getInternalController().processEvent( new Event<Boolean>( "SHOW_GAME_STATE_FPS", true ) ) ;
	}

	@Override
	protected void initEventProcessors( final EventController _internal, final EventController _external )
	{
		super.initEventProcessors( _internal, _external ) ;

		_internal.addEventProcessor( new EventProcessor<Entity>( "ADD_ENTITY", "ADD_ENTITY" )
		{
			public void processEvent( final Event<Entity> _event )
			{
				addEntity( _event.getVariable() ) ;
			}
		} ) ;

		_internal.addEventProcessor( new EventProcessor<UIPacket>( "INSERT_UIPACKET", "INSERT_UIPACKET" )
		{
			public void processEvent( final Event<UIPacket> _event )
			{
				final UIPacket packet = _event.getVariable() ;
				if( root != null )
				{
					root.insertUIWrapper( packet.getWrapper(), packet.getX(), packet.getY() ) ;
				}
				else
				{
					if( mainView.intersectPoint( packet.getX(), packet.getY() ) )
					{
						root = packet.getWrapper() ;
						root.setLayer( 1 ) ;
						mainView.addElement( root ) ;
					}
				}
			}
		} ) ;

		_internal.addEventProcessor( new EventProcessor<CUIPacket>( "INSERT_CUIPACKET", "INSERT_CUIPACKET" )
		{
			public void processEvent( final Event<CUIPacket> _event )
			{
				final CUIPacket packet = _event.getVariable() ;
				if( root != null )
				{
					root.insertMetaComponent( packet.getMetaComponent(), packet.getX(), packet.getY() ) ;
				}
			}
		} ) ;

		_internal.addEventProcessor( new EventProcessor<UIWrapper>( "DISPLAY_META", "DISPLAY_META" )
		{
			UIElement.Meta current = null ;

			public void processEvent( final Event<UIWrapper> _event )
			{
				final UIWrapper wrapper = _event.getVariable() ;
				final UIElement.Meta meta = wrapper.getMeta() ;

				if( meta == current )
				{
					return ;
				}

				final List<UIElement> views = componentDataPanel.getElements() ;
				for( final UIElement view : views )
				{
					view.destroy() ;
				}

				current = meta ;
				elementDataPanel.setModel( meta ) ;

				final List<UIElement.MetaComponent> components = meta.getComponents( MalletList.<UIElement.MetaComponent>newList() ) ;
				for( final UIElement.MetaComponent component : components )
				{
					final UIAbstractView view = componentDataPanel.addElement( new UIAbstractView() ) ;
					view.setModel( component ) ;
				}

				components.clear() ;
			}
		} ) ;
	}
	
	/**
		A list of elements that can be added to a UI.
	*/
	private void createElementsPanel( final UIList _view )
	{
		addElementPackage( _view, "Button",     UIButton.Meta.class ) ;
		addElementPackage( _view, "Checkbox",   UICheckbox.Meta.class ) ;
		addElementPackage( _view, "Element",    UIElement.Meta.class ) ;
		addElementPackage( _view, "Layout",     UILayout.Meta.class ) ;
		addElementPackage( _view, "Menu",       UIMenu.Meta.class ) ;
		addElementPackage( _view, "List",       UIList.Meta.class ) ;
		addElementPackage( _view, "Spacer",     UISpacer.Meta.class ) ;
		addElementPackage( _view, "Text field", UITextField.Meta.class ) ;
		
	}

	private static void addElementPackage( final UIList _view, final String _name, final Class<? extends UIElement.Meta> _class )
	{
		final UIButton.Meta meta = new UIButton.Meta() ;

		{
			final GUIPanelEdge.Meta edge = meta.addComponent( new GUIPanelEdge.Meta() ) ;
			edge.setSheet( "base/textures/edge_button.png" ) ;

			final GUIText.Meta text = meta.addComponent( new GUIText.Meta() ) ;
			text.setText( _name ) ;
		}

		final UIButton button = _view.addElement( UIGenerator.<UIButton>create( meta ) ) ;
		button.addComponent( new InputComponent( button )
		{
			@Override
			public InputEvent.Action touchPressed( final InputEvent _input )
			{
				return mousePressed( _input ) ;
			}

			@Override
			public InputEvent.Action mousePressed( final InputEvent _input )
			{
				final Entity entity = UIEditorState.createUIDropEntity( _class, _input ) ;
				getParent().addEvent( new Event<Entity>( "ADD_ENTITY", entity ) ) ;
				return InputEvent.Action.CONSUME ;
			}
		} ) ;
	}

	private void createCUIsPanel( final UIList _view )
	{
		addCUIPackage( _view, "Draw",       GUIDraw.Meta.class ) ;
		addCUIPackage( _view, "Draw Edge",  GUIDrawEdge.Meta.class ) ;
		addCUIPackage( _view, "Panel",      GUIPanelDraw.Meta.class ) ;
		addCUIPackage( _view, "Panel Edge", GUIPanelEdge.Meta.class ) ;
		addCUIPackage( _view, "Text",       GUIText.Meta.class ) ;
		addCUIPackage( _view, "Edit Text",  GUIEditText.Meta.class ) ;
	}

	private static void addCUIPackage( final UIList _view, final String _name, final Class<? extends UIElement.MetaComponent> _class )
	{
		final UIButton.Meta meta = new UIButton.Meta() ;

		{
			final GUIPanelEdge.Meta edge = meta.addComponent( new GUIPanelEdge.Meta() ) ;
			edge.setSheet( "base/textures/edge_button.png" ) ;

			final GUIText.Meta text = meta.addComponent( new GUIText.Meta() ) ;
			text.setText( _name ) ;
		}

		final UIButton button = _view.addElement( UIGenerator.<UIButton>create( meta ) ) ;
		button.addComponent( new InputComponent( button )
		{
			@Override
			public InputEvent.Action touchPressed( final InputEvent _input )
			{
				return mousePressed( _input ) ;
			}

			@Override
			public InputEvent.Action mousePressed( final InputEvent _input )
			{
				final Entity entity = UIEditorState.createCUIDropEntity( _class, _input ) ;
				getParent().addEvent( new Event<Entity>( "ADD_ENTITY", entity ) ) ;
				return InputEvent.Action.CONSUME ;
			}
		} ) ;
	}

	private static Entity createCUIDropEntity( final Class<? extends UIElement.MetaComponent> _class, final InputEvent _event )
	{
		final Entity entity = new Entity( "DROP_ENTITY", "DROP_ENTITY" ) ;
		final EventComponent event = new EventComponent( entity ) ;
		final MouseComponent mouse = new MouseComponent( entity )
		{
			@Override
			public void mouseReleased( final InputEvent _event )
			{
				switch( _event.getInputType() )
				{
					case MOUSE1_RELEASED :
					{
						try
						{
							final UIElement.MetaComponent meta = _class.newInstance() ;
							final Vector2 position = new Vector2( _event.getMouseX(), _event.getMouseY() ) ;

							event.passStateEvent( new Event<CUIPacket>( "INSERT_CUIPACKET", new CUIPacket( meta, position ) ) ) ;
						}
						catch( IllegalAccessException ex )
						{
							System.out.println( "Failed to access UI meta component object.." ) ;
						}
						catch( InstantiationException ex )
						{
							System.out.println( "Failed to instantiate UI meta component object.." ) ;
						}
						finally
						{
							getParent().destroy() ;
						}
						break ;
					}
					default              : break ;
				}
			}
		} ;

		return UIEditorState.setupDropEntity( entity ) ;
	}

	private static Entity createUIDropEntity( final Class<? extends UIElement.Meta> _class, final InputEvent _event )
	{
		final Entity entity = new Entity( "DROP_ENTITY", "DROP_ENTITY" ) ;
		final EventComponent event = new EventComponent( entity ) ;
		final MouseComponent mouse = new MouseComponent( entity )
		{
			@Override
			public void mouseReleased( final InputEvent _event )
			{
				switch( _event.getInputType() )
				{
					case MOUSE1_RELEASED :
					{
						try
						{
							final UIElement.Meta meta = _class.newInstance() ;
							final UIWrapper wrapper = new UIWrapper( meta ) ;
							final Vector2 position = new Vector2( _event.getMouseX(), _event.getMouseY() ) ;

							event.passStateEvent( new Event<UIPacket>( "INSERT_UIPACKET", new UIPacket( wrapper, position ) ) ) ;
						}
						catch( IllegalAccessException ex )
						{
							System.out.println( "Failed to access UI meta object.." ) ;
						}
						catch( InstantiationException ex )
						{
							System.out.println( "Failed to instantiate UI meta object.." ) ;
						}
						finally
						{
							getParent().destroy() ;
						}
						break ;
					}
					default              : break ;
				}
			}
		} ;

		return UIEditorState.setupDropEntity( entity ) ;
	}

	private static Entity setupDropEntity( final Entity _entity )
	{
		_entity.setPosition( 0, 0, 0 ) ;

		final AnimComponent anim   = new AnimComponent( _entity ) ;
		final Anim animation = AnimationAssist.createAnimation( "base/anim/moomba.anim",
																_entity.position,
																new Vector3( -16, -16, 0 ),
																new Vector3(),
																new Vector3( 1, 1, 1 ),
																100 ) ;

		final Shape plane = Shape.constructPlane( new Vector3( 32, 32, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ;
		DrawAssist.amendShape( AnimationAssist.getDraw( animation ), plane ) ;
		DrawAssist.amendInterpolation( AnimationAssist.getDraw( animation ), Interpolation.LINEAR ) ;

		anim.addAnimation( "DEFAULT", animation ) ;
		anim.setDefaultAnim( "DEFAULT" ) ;

		return _entity ;
	}

	/**
		Used to pass a UIWrapper through an Event before 
		being inserted into another UIWrapper at the 
		designated position.
	*/
	private static class UIPacket
	{
		private final UIWrapper wrapper ;
		private final Vector2 position ;

		public UIPacket( final UIWrapper _wrapper, final Vector2 _position )
		{
			wrapper = _wrapper ;
			position = _position ;
		}

		public UIWrapper getWrapper()
		{
			return wrapper ;
		}

		public float getX()
		{
			return position.x ;
		}

		public float getY()
		{
			return position.y ;
		}
	}

	private static class CUIPacket
	{
		private final UIElement.MetaComponent meta ;
		private final Vector2 position ;

		public CUIPacket( final UIElement.MetaComponent _meta, final Vector2 _position )
		{
			meta = _meta ;
			position = _position ;
		}

		public UIElement.MetaComponent getMetaComponent()
		{
			return meta ;
		}

		public float getX()
		{
			return position.x ;
		}

		public float getY()
		{
			return position.y ;
		}
	}
}
