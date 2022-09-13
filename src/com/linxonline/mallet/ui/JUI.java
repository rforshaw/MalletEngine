package com.linxonline.mallet.ui ;

import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.formats.json.JObject ;
import com.linxonline.mallet.io.formats.json.JArray ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.MalletFont ;

import com.linxonline.mallet.ui.gui.* ;

/**
	UIMap provides a data-driven means to creating UI.
	The class is broken up into two sections, the ElementGenerator
	and the UIMap populated with the content.

	Element generation uses the data stored by the json 
	file to populate the requested element's meta object.

	This meta object is then passed to UIGenerator to 
	create the actual UI element that will be shown to 
	the user. 
*/
public final class JUI
{
	private final static Map<String, Generator> elementCreators = MalletMap.<String, Generator>newMap() ;
	static
	{
		elementCreators.put( "UIELEMENT", new Generator<UIElement, UIElement.Meta>()
		{
			public UIElement.Meta createMeta( final JObject _ui )
			{
				final UIElement.Meta meta = new UIElement.Meta() ;
				applyBasics( _ui, meta ) ;

				meta.addComponent( JUI.createGUIText( _ui.getJObject( "UITEXT" ), UIRatio.getGlobalUIRatio() ) ) ;
				meta.addComponent( JUI.createGUIDraw( _ui.getJObject( "UIDRAW" ) ) ) ;
				meta.addComponent( JUI.createGUIPanelEdge( _ui.getJObject( "UIEDGE" ) ) ) ;

				return meta ;
			}

			public UIElement create( final JUI _map, final UIElement.Meta _meta, final JObject _ui )
			{
				return UIGenerator.<UIElement>create( _meta ) ;
			}
		} ) ;

		elementCreators.put( "UILAYOUT", new Generator<UILayout, UILayout.Meta>()
		{
			public UILayout.Meta createMeta( final JObject _ui )
			{
				final UILayout.Meta meta = new UILayout.Meta() ;

				applyBasics( _ui, meta ) ;
				meta.setType( ILayout.Type.derive( _ui.optString( "LAYOUT", null ) ) ) ;

				meta.addComponent( JUI.createGUIDraw( _ui.getJObject( "UIDRAW" ) ) ) ;
				meta.addComponent( JUI.createGUIPanelEdge( _ui.getJObject( "UIEDGE" ) ) ) ;

				return meta ;
			}

			public UILayout create( final JUI _map, final UILayout.Meta _meta, final JObject _ui )
			{
				final UILayout element = UIGenerator.<UILayout>create( _meta ) ;
				addChildren( _map, element, _ui.getJArray( "CHILDREN" ) ) ;
				return element ;
			}
		} ) ;

		elementCreators.put( "UIWINDOW_LAYOUT", new Generator<UILayout, UILayout.Meta>()
		{
			public UILayout.Meta createMeta( final JObject _ui )
			{
				final UILayout.Meta meta = new UILayout.Meta()
				{
					@Override
					public String getElementType()
					{
						return "UIWINDOW_LAYOUT" ;
					}
				} ;

				applyBasics( _ui, meta ) ;
				meta.setType( ILayout.Type.derive( _ui.optString( "LAYOUT", null ) ) ) ;

				meta.addComponent( JUI.createGUIDraw( _ui.getJObject( "UIDRAW" ) ) ) ;
				meta.addComponent( JUI.createGUIPanelEdge( _ui.getJObject( "UIEDGE" ) ) ) ;

				return meta ;
			}
		
			public UILayout create( final JUI _map, final UILayout.Meta _meta, final JObject _ui )
			{
				final UILayout element = UIGenerator.<UILayout>create( _meta ) ;
				addChildren( _map, element, _ui.getJArray( "CHILDREN" ) ) ;
				return element ;
			}
		} ) ;

		elementCreators.put( "UITEXTFIELD", new Generator<UITextField, UITextField.Meta>()
		{
			public UITextField.Meta createMeta( final JObject _ui )
			{
				final UITextField.Meta meta = new UITextField.Meta() ;
				applyBasics( _ui, meta ) ;

				meta.addComponent( JUI.createGUIDraw( _ui.getJObject( "UIDRAW" ) ) ) ;
				meta.addComponent( JUI.createGUIPanelEdge( _ui.getJObject( "UIEDGE" ) ) ) ;
				meta.addComponent( JUI.createGUIEditText( _ui.getJObject( "UITEXT" ), UIRatio.getGlobalUIRatio() ) ) ;

				return meta ;
			}

			public UITextField create( final JUI _map, final UITextField.Meta _meta, final JObject _ui )
			{
				return UIGenerator.<UITextField>create( _meta ) ;
			}
		} ) ;

		elementCreators.put( "UIBUTTON", new Generator<UIButton, UIButton.Meta>()
		{
			public UIButton.Meta createMeta( final JObject _ui )
			{
				final UIButton.Meta meta = new UIButton.Meta() ;
				applyBasics( _ui, meta ) ;

				meta.addComponent( JUI.createGUIText( _ui.getJObject( "UITEXT" ), UIRatio.getGlobalUIRatio() ) ) ;
				meta.addComponent( JUI.createGUIPanelDraw( _ui.getJObject( "UIDRAW" ) ) ) ;
				meta.addComponent( JUI.createGUIPanelEdge( _ui.getJObject( "UIEDGE" ) ) ) ;

				return meta ;
			}

			public UIButton create( final JUI _map, final UIButton.Meta _meta, final JObject _ui )
			{
				return UIGenerator.<UIButton>create( _meta ) ;
			}
		} ) ;

		elementCreators.put( "UIMENU", new Generator<UIMenu, UIMenu.Meta>()
		{
			public UIMenu.Meta createMeta( final JObject _ui )
			{
				final UIMenu.Meta meta = new UIMenu.Meta() ;

				applyBasics( _ui, meta ) ;
				meta.setType( ILayout.Type.derive( _ui.optString( "LAYOUT", null ) ) ) ;
				meta.setThickness( ( float )_ui.optDouble( "THICKNESS", 0.0 ) ) ;

				meta.addComponent( JUI.createGUIDraw( _ui.getJObject( "UIDRAW" ) ) ) ;
				meta.addComponent( JUI.createGUIPanelEdge( _ui.getJObject( "UIEDGE" ) ) ) ;

				return meta ;
			}

			public UIMenu create( final JUI _map, final UIMenu.Meta _meta, final JObject _ui )
			{
				final UIMenu element = UIGenerator.<UIMenu>create( _meta ) ;
				addChildren( _map, element, _ui.getJArray( "CHILDREN" ) ) ;
				return element ;
			}
		} ) ;

		elementCreators.put( "UISPACER", new Generator<UISpacer, UISpacer.Meta>()
		{
			public UISpacer.Meta createMeta( final JObject _ui )
			{
				final UISpacer.Meta meta = new UISpacer.Meta() ;

				applyBasics( _ui, meta ) ;
				meta.setAxis( UISpacer.Axis.derive( _ui.optString( "AXIS", null ) ) ) ;

				meta.addComponent( JUI.createGUIDraw( _ui.getJObject( "UIDRAW" ) ) ) ;
				meta.addComponent( JUI.createGUIPanelEdge( _ui.getJObject( "UIEDGE" ) ) ) ;

				return meta ;
			}

			public UISpacer create( final JUI _map, final UISpacer.Meta _meta, final JObject _ui )
			{
				return UIGenerator.<UISpacer>create( _meta ) ;
			}
		} ) ;
		
		elementCreators.put( "UICHECKBOX", new Generator<UICheckbox, UICheckbox.Meta>()
		{
			public UICheckbox.Meta createMeta( final JObject _ui )
			{
				final UICheckbox.Meta meta = new UICheckbox.Meta() ;
				applyBasics( _ui, meta ) ;

				meta.addComponent( JUI.createGUITick( _ui.getJObject( "UITICK" ) )  ) ;
				meta.addComponent( JUI.createGUIPanelDraw( _ui.getJObject( "UIDRAW" ) ) ) ;
				meta.addComponent( JUI.createGUIPanelEdge( _ui.getJObject( "UIEDGE" ) ) ) ;

				return meta ;
			}

			public UICheckbox create( final JUI _map, final UICheckbox.Meta _meta, final JObject _ui )
			{
				return UIGenerator.<UICheckbox>create( _meta ) ;
			}
		} ) ;

		elementCreators.put( "UILIST", new Generator<UIList, UIList.Meta>()
		{
			public UIList.Meta createMeta( final JObject _ui )
			{
				final UIList.Meta meta = new UIList.Meta() ;

				applyBasics( _ui, meta ) ;
				meta.setType( ILayout.Type.derive( _ui.optString( "LAYOUT", null ) ) ) ;

				meta.addComponent( JUI.createGUIDraw( _ui.getJObject( "UIDRAW" ) ) ) ;
				meta.addComponent( JUI.createGUIPanelEdge( _ui.getJObject( "UIEDGE" ) ) ) ;
				meta.addComponent( JUI.createScrollbar( _ui.getJObject( "SCROLLBAR" ) ) ) ;

				return meta ;
			}

			public UIList create( final JUI _map, final UIList.Meta _meta, final JObject _ui )
			{
				final UIList element = UIGenerator.<UIList>create( _meta ) ;
				addChildren( _map, element, _ui.getJArray( "CHILDREN" ) ) ;
				return element ;
			}
		} ) ;

		elementCreators.put( "UIMENU_ITEM", new Generator<UIButton, UIButton.Meta>()
		{
			public UIButton.Meta createMeta( final JObject _ui )
			{
				final UIButton.Meta meta = new UIButton.Meta() ;
				applyBasics( _ui, meta ) ;

				meta.addComponent( JUI.createGUIText( _ui.getJObject( "UITEXT" ), UIRatio.getGlobalUIRatio() ) ) ;
				meta.addComponent( JUI.createGUIDraw( _ui.getJObject( "UIDRAW" ) ) ) ;
				meta.addComponent( JUI.createGUIPanelEdge( _ui.getJObject( "UIEDGE" ) ) ) ;

				return meta ;
			}

			public UIButton create( final JUI _map, final UIButton.Meta _meta, final JObject _ui )
			{
				final UIElement dropdown = JUI.createElement( _map, _ui.getJObject( "DROPDOWN" ) ) ;

				final UIMenu.Item element = new UIMenu.Item( dropdown ) ;
				UIElement.applyMeta( _meta, element ) ;
				UIGenerator.addComponents( element, _meta ) ;

				return element ;
			}
		} ) ;

		elementCreators.put( "UIABSTRACTVIEW", new Generator<UIAbstractView, UIAbstractView.Meta>()
		{
			public UIAbstractView.Meta createMeta( final JObject _ui )
			{
				final UIAbstractView.Meta meta = new UIAbstractView.Meta() ;
				applyBasics( _ui, meta ) ;

				meta.addComponent( JUI.createGUIDraw( _ui.getJObject( "UIDRAW" ) ) ) ;
				meta.addComponent( JUI.createGUIPanelEdge( _ui.getJObject( "UIEDGE" ) ) ) ;

				return meta ;
			}

			public UIAbstractView create( final JUI _map, final UIAbstractView.Meta _meta, final JObject _ui )
			{
				final UIAbstractView element = UIGenerator.<UIAbstractView>create( _meta ) ;
				return element ;
			}
		} ) ;
	}

	/**
		Allow the developer to extend the UI system.
	*/
	public static <T extends UIElement, E extends T.Meta> void addElementGenerator( final String _id, final Generator<T, E> _create )
	{
		elementCreators.put( _id, _create ) ;
	}

	private final Map<String, UIElement> lookup = MalletMap.<String, UIElement>newMap() ;
	private final UIElement parent ;

	private JUI( final JObject _map )
	{
		parent = createElement( this, _map ) ;
		if( parent == null )
		{
			Logger.println( "Error: JUI loaded but no parent element constructed..", Logger.Verbosity.MAJOR ) ;
			lookup.clear() ;
		}
	}

	public static JUI create( final String _path )
	{
		if( GlobalFileSystem.isExtension( _path, ".jui", ".JUI", ".jUI" ) == false )
		{
			return null ;
		}

		final FileStream stream = GlobalFileSystem.getFile( _path ) ;
		if( stream.exists() == false )
		{
			Logger.println( "JUI: " + _path + " doesn't exist.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		final JUI jui = new JUI( JObject.construct( stream ) ) ;
		return jui ;
	}

	public UIElement getParent()
	{
		return parent ;
	}

	public boolean add( final String _name, final UIElement _element )
	{
		if( lookup.containsKey( _name ) == false )
		{
			lookup.put( _name, _element ) ;
			return true ;
		}

		Logger.println( "Error: UI element name: " + _name + " is already in use.", Logger.Verbosity.MAJOR ) ;
		return false ;
	}

	public List<String> getAvailableNames()
	{
		final Set<String> set = lookup.keySet() ;
		final List<String> names = MalletList.<String>newList( set.size() ) ;

		for( final String name : set )
		{
			names.add( name ) ;
		}

		return names ;
	}
	
	public UIElement get( final String _name )
	{
		return lookup.get( _name ) ;
	}

	public <T> T get( final String _name, final Class<T> _clazz )
	{
		final UIElement element = lookup.get( _name ) ;
		if( element != null )
		{
			if( _clazz.isInstance( element ) == false )
			{
				Logger.println( "JUI: Element " + _name + " is not instance requested.", Logger.Verbosity.MAJOR ) ;
				return null ;
			}

			return _clazz.cast( element ) ;
		}

		Logger.println( "JUI: could not find element." + _name, Logger.Verbosity.MAJOR ) ;
		return null ;
	}

	public static UIElement.Meta createMeta( final JObject _ui )
	{
		final String type = _ui.optString( "TYPE", null ) ;
		final Generator<UIElement, UIElement.Meta> create = elementCreators.get( type ) ;
		if( create == null )
		{
			// If a creator is not available we can't construct 
			// the requested type.
			Logger.println( type + " generator doesn't exist.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		//Logger.println( "Creating: " + _ui.toString(), Logger.Verbosity.MINOR ) ;
		return create.createMeta( _ui ) ;
	}

	private static UIElement createElement( final JUI _map, final JObject _ui )
	{
		final String type = _ui.optString( "TYPE", null ) ;
		final Generator<UIElement, UIElement.Meta> create = elementCreators.get( type ) ;
		if( create == null )
		{
			// If a creator is not available we can't construct 
			// the requested type.
			Logger.println( type + " generator doesn't exist.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		//Logger.println( "Creating: " + _ui.toString(), Logger.Verbosity.MINOR ) ;
		final UIElement.Meta meta = create.createMeta( _ui ) ;
		final UIElement element = create.create( _map, meta, _ui ) ;

		applyLookup( _map, element, meta ) ;
		return element ;
	}

	private static void addChildren( final JUI _map, final UILayout _layout, final JArray _children )
	{
		if( _map == null || _children == null )
		{
			return ;
		}

		final int size = _children.length() ;
		for( int i = 0; i < size; i++ )
		{
			final JObject jChild = _children.getJObject( i ) ;
			final UIElement child = createElement( _map, jChild ) ;
			if( child != null )
			{
				_layout.addElement( child ) ;
			}
		}
	}

	public static void applyBasics( final JObject _ui, final UIElement.Meta _meta )
	{
		_meta.setName( _ui.optString( "NAME", null ) ) ;
		_meta.setLength( Vector3.parseVector3( _ui.optString( "LENGTH", null ) ) ) ;
		_meta.setMinimumLength( Vector3.parseVector3( _ui.optString( "MIN_LENGTH", null ) ) ) ;
		_meta.setMaximumLength( Vector3.parseVector3( _ui.optString( "MAX_LENGTH", null ) ) ) ;
		_meta.setLayer( _ui.optInt( "LAYER", 0 ) ) ;
	}

	public static void applyLookup( final JUI _map, final UIElement _element, final UIElement.Meta _meta )
	{
		final String name = _meta.getName() ;
		if( name.isEmpty() == false )
		{
			// Elements are only added to the lookup 
			// table if they have a name defined.
			// Some elements are not important enough to 
			// have a name defined.
			//System.out.println( "Adding: " + name + " to lookup." ) ;
			_map.lookup.put( name, _element ) ;
		}
	}

	public static GUIPanelEdge.Meta createGUIPanelEdge( final JObject _ui )
	{
		if( _ui == null )
		{
			return null ;
		}

		final GUIPanelEdge.Meta meta = new GUIPanelEdge.Meta() ;
		meta.setEdge( ( float )_ui.optDouble( "EDGE", 5.0 ) ) ;
		meta.setSheet( _ui.optString( "TEXTURE", "" ) ) ;
		meta.setNeutralColour( MalletColour.parseColour( _ui.optString( "NEUTRAL", _ui.optString( "COLOUR", null ) ) ) ) ;
		meta.setRolloverColour( MalletColour.parseColour( _ui.optString( "ROLLOVER", null ) ) ) ;
		meta.setClickedColour( MalletColour.parseColour( _ui.optString( "CLICKED", null ) ) ) ;

		return meta ;
	}
	
	public static GUIDraw.Meta createGUIDraw( final JObject _ui )
	{
		if( _ui == null )
		{
			return null ;
		}

		final GUIDraw.Meta meta = new GUIDraw.Meta() ;
		meta.setName( _ui.getString( "NAME" ) ) ;
		meta.setGroup( _ui.getString( "GROUP" ) ) ;
		meta.setUV( createUV( _ui.getJObject( "UV" ) ) ) ;
		meta.setRetainRatio( _ui.optBoolean( "RETAIN_RATIO", false ) ) ;
		meta.setSheet( _ui.optString( "TEXTURE", null ) ) ;

		final JObject align = _ui.optJObject( "ALIGNMENT", null ) ;
		if( align != null )
		{
			meta.setAlignment( UI.Alignment.derive( align.optString( "X", null ) ),
							   UI.Alignment.derive( align.optString( "Y", null ) ) ) ;
		}

		return meta ;
	}

	public static GUIText.Meta createGUIText( final JObject _ui, final UIRatio _ratio )
	{
		if( _ui == null )
		{
			return null ;
		}

		final int fontSize = ( int )_ratio.toPixelY( ( float )_ui.optDouble( "FONT_SIZE", 0.42 ) ) ;

		final GUIText.Meta meta = new GUIText.Meta() ;
		meta.setName( _ui.getString( "NAME" ) ) ;
		meta.setGroup( _ui.getString( "GROUP" ) ) ;
		meta.setText( _ui.optString( "TEXT", "" ) ) ;
		meta.setFont( MalletFont.createByPixel( _ui.optString( "FONT", "Arial" ), MalletFont.PLAIN, fontSize ) ) ;
		meta.setColour( MalletColour.parseColour( _ui.optString( "COLOUR", null ) ) ) ;
		
		final JObject align = _ui.optJObject( "ALIGNMENT", null ) ;
		if( align != null )
		{
			meta.setAlignment( UI.Alignment.derive( align.optString( "X", null ) ),
							   UI.Alignment.derive( align.optString( "Y", null ) ) ) ;
		}

		return meta ;
	}

	public static GUIEditText.Meta createGUIEditText( final JObject _ui, final UIRatio _ratio )
	{
		if( _ui == null )
		{
			return null ;
		}

		final int fontSize = ( int )_ratio.toPixelY( ( float )_ui.optDouble( "FONT_SIZE", 0.42 ) ) ;

		final GUIEditText.Meta meta = new GUIEditText.Meta() ;
		meta.setName( _ui.getString( "NAME" ) ) ;
		meta.setGroup( _ui.getString( "GROUP" ) ) ;
		meta.setText( _ui.optString( "TEXT", "" ) ) ;
		meta.setFont( MalletFont.createByPixel( _ui.optString( "FONT", "Arial" ), MalletFont.PLAIN, fontSize ) ) ;
		meta.setColour( MalletColour.parseColour( _ui.optString( "COLOUR", null ) ) ) ;
		
		final JObject align = _ui.optJObject( "ALIGNMENT", null ) ;
		if( align != null )
		{
			meta.setAlignment( UI.Alignment.derive( align.optString( "X", null ) ),
							   UI.Alignment.derive( align.optString( "Y", null ) ) ) ;
		}

		return meta ;
	}

	public static GUIPanelDraw.Meta createGUIPanelDraw( final JObject _ui )
	{
		if( _ui == null )
		{
			return null ;
		}

		final GUIPanelDraw.Meta meta = new GUIPanelDraw.Meta() ;
		meta.setName( _ui.getString( "NAME" ) ) ;
		meta.setGroup( _ui.getString( "GROUP" ) ) ;
		meta.setNeutralUV( createUV( _ui.optJObject( "NEUTRAL", _ui.getJObject( "UV" ) ) ) ) ;
		meta.setRolloverUV( createUV( _ui.getJObject( "ROLLOVER" ) ) ) ;
		meta.setClickedUV( createUV( _ui.getJObject( "CLICKED" ) ) ) ;
		meta.setRetainRatio( _ui.optBoolean( "RETAIN_RATIO", false ) ) ;
		meta.setSheet( _ui.optString( "TEXTURE", "" ) ) ;

		final JObject align = _ui.optJObject( "ALIGNMENT", null ) ;
		if( align != null )
		{
			meta.setAlignment( UI.Alignment.derive( align.optString( "X", null ) ),
								UI.Alignment.derive( align.optString( "Y", null ) ) ) ;
		}

		return meta ;
	}

	public static GUITick.Meta createGUITick( final JObject _ui )
	{
		if( _ui == null )
		{
			return null ;
		}

		final GUITick.Meta meta = new GUITick.Meta() ;
		meta.setName( _ui.getString( "NAME" ) ) ;
		meta.setGroup( _ui.getString( "GROUP" ) ) ;
		meta.setUV( createUV( _ui.getJObject( "UV" ) ) ) ;
		meta.setRetainRatio( _ui.optBoolean( "RETAIN_RATIO", false ) ) ;
		meta.setSheet( _ui.optString( "TEXTURE", null ) ) ;

		final JObject align = _ui.optJObject( "ALIGNMENT", null ) ;
		if( align != null )
		{
			meta.setAlignment( UI.Alignment.derive( align.optString( "X", null ) ),
								UI.Alignment.derive( align.optString( "Y", null ) ) ) ;
		}

		return meta ;
	}

	public static GUIScrollbar.Meta createScrollbar( final JObject _ui )
	{
		if( _ui == null )
		{
			return null ;
		}

		return new GUIScrollbar.Meta() ;
	}
	
	public static UIElement.UV createUV( final JObject _uv )
	{
		if( _uv == null )
		{
			return null ;
		}

		final Vector2 min = Vector2.parseVector2( _uv.optString( "MIN", "0.0, 0.0" ) ) ;
		final Vector2 max = Vector2.parseVector2( _uv.optString( "MAX", "1.0, 1.0" ) ) ;
		return new UIElement.UV( min, max ) ;
	}

	public interface Generator<E extends UIElement, M extends E.Meta>
	{
		public M createMeta( final JObject _ui ) ;

		public E create( final JUI _map, final M _meta, final JObject _ui ) ;
	}
}
