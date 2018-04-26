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
	private UIList elementOptionsPanel ;
	private UIList elementStructurePanel ;
	private UIAbstractView elementDataPanel ;

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
			elementStructurePanel = jui.get( "UIStructurePanel", UIList.class ) ;
			elementDataPanel = jui.get( "UIElementDataPanel", UIAbstractView.class ) ;

			createElementsPanel( elementOptionsPanel ) ;
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

		_internal.addEventProcessor( new EventProcessor<UIWrapper>( "DISPLAY_META", "DISPLAY_META" )
		{
			final List<UIElement> elements = MalletList.<UIElement>newList() ;
			UIElement.Meta current = null ;

			public void processEvent( final Event<UIWrapper> _event )
			{
				final UIWrapper wrapper = _event.getVariable() ;
				final UIElement.Meta meta = wrapper.getMeta() ;

				if( meta == current )
				{
					return ;
				}

				current = meta ;
				elementDataPanel.setModel( meta ) ;
			}
		} ) ;
	}
	
	/**
		A list of elements that can be added to a UI.
	*/
	private void createElementsPanel( final UIList _view )
	{
		addElementPackage( _view, "Button", UIButton.Meta.class ) ;
		addElementPackage( _view, "Layout", UILayout.Meta.class ) ;
	}

	private static void addElementPackage( final UIList _view, final String _name, final Class<? extends UIElement.Meta> _class )
	{
		final UIButton.Meta meta = new UIButton.Meta() ;

		{
			final GUIPanelEdge.Meta edge = meta.addListener( new GUIPanelEdge.Meta() ) ;
			edge.setSheet( "base/textures/edge_button.png" ) ;

			final GUIText.Meta text = meta.addListener( new GUIText.Meta() ) ;
			text.setText( _name ) ;
		}

		final UIButton button = _view.addElement( UIGenerator.<UIButton>create( meta ) ) ;
		button.addListener( new InputListener<UIButton>()
		{
			@Override
			public InputEvent.Action touchPressed( final InputEvent _input )
			{
				return mousePressed( _input ) ;
			}

			@Override
			public InputEvent.Action mousePressed( final InputEvent _input )
			{
				final Entity entity = UIEditorState.createDropEntity( _class, _input ) ;
				getParent().addEvent( new Event<Entity>( "ADD_ENTITY", entity ) ) ;
				return InputEvent.Action.CONSUME ;
			}
		} ) ;
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

	private static Entity createDropEntity( final Class<? extends UIElement.Meta> _class, final InputEvent _event )
	{
		final Entity entity = new Entity( "DROP_ENTITY", "DROP_ENTITY" ) ;
		entity.setPosition( 0, 0, 0 ) ;

		final AnimComponent anim   = new AnimComponent( entity ) ;
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

		final Anim animation = AnimationAssist.createAnimation( "base/anim/moomba.anim",
																entity.position,
																new Vector3( -16, -16, 0 ),
																new Vector3(),
																new Vector3( 1, 1, 1 ),
																100 ) ;

		final Shape plane = Shape.constructPlane( new Vector3( 32, 32, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ;
		DrawAssist.amendShape( AnimationAssist.getDraw( animation ), plane ) ;
		DrawAssist.amendInterpolation( AnimationAssist.getDraw( animation ), Interpolation.LINEAR ) ;

		anim.addAnimation( "DEFAULT", animation ) ;
		anim.setDefaultAnim( "DEFAULT" ) ;

		return entity ;
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
}
