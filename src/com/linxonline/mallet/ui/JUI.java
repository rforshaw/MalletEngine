package com.linxonline.mallet.ui ;

import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.formats.json.JSONObject ;
import com.linxonline.mallet.io.formats.json.JSONArray ;

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
	The class is broken up into two sections, the Generator
	and the UIMap populated with the content.
*/
public class JUI
{
	private final static Map<String, Generator> creators = MalletMap.<String, Generator>newMap() ;
	static
	{
		creators.put( "UIELEMENT", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final UIElement.Meta meta = new UIElement.Meta() ;

				applyBasics( _map, _ui, meta ) ;
				final UIElement element = UIElement.applyMeta( meta, new UIElement() ) ;
				applyLookup( _map, element, meta ) ;

				element.addListener( JUI.<UIElement>createGUIText( _ui.getJSONObject( "UITEXT" ), element.getRatio() ) ) ;
				element.addListener( JUI.<UIElement>createGUIDraw( _ui.getJSONObject( "UIDRAW" ) ) ) ;
				element.addListener( JUI.<UIElement>createGUIPanelEdge( _ui.getJSONObject( "UIEDGE" ) ) ) ;

				return element ;
			}
		} ) ;

		creators.put( "UILAYOUT", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final UILayout.Meta meta = new UILayout.Meta() ;

				applyBasics( _map, _ui, meta ) ;
				meta.setType( UILayout.Type.derive( _ui.optString( "LAYOUT", null ) ) ) ;

				final UILayout element = UILayout.applyMeta( meta, new UILayout( meta.getType() ) ) ;
				applyLookup( _map, element, meta ) ;

				element.addListener( JUI.<UILayout>createGUIDraw( _ui.getJSONObject( "UIDRAW" ) ) ) ;
				element.addListener( JUI.<UILayout>createGUIPanelEdge( _ui.getJSONObject( "UIEDGE" ) ) ) ;

				addChildren( _map, element, _ui.getJSONArray( "CHILDREN" ) ) ;
				return element ;
			}
		} ) ;

		creators.put( "UIWINDOW_LAYOUT", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final UILayout.Meta meta = new UILayout.Meta() ;

				applyBasics( _map, _ui, meta ) ;
				meta.setType( UILayout.Type.derive( _ui.optString( "LAYOUT", null ) ) ) ;

				final UILayout element = UIFactory.constructWindowLayout( meta.getType() ) ;
				UILayout.applyMeta( meta, element ) ;
				applyLookup( _map, element, meta ) ;

				element.addListener( JUI.<UILayout>createGUIDraw( _ui.getJSONObject( "UIDRAW" ) ) ) ;
				element.addListener( JUI.<UILayout>createGUIPanelEdge( _ui.getJSONObject( "UIEDGE" ) ) ) ;

				addChildren( _map, element, _ui.getJSONArray( "CHILDREN" ) ) ;
				return element ;
			}
		} ) ;

		creators.put( "UITEXTFIELD", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final UITextField.Meta meta = new UITextField.Meta() ;

				applyBasics( _map, _ui, meta ) ;
				final UIElement element = UIElement.applyMeta( meta, new UITextField() ) ;
				applyLookup( _map, element, meta ) ;

				element.addListener( JUI.<UITextField>createGUIDraw( _ui.getJSONObject( "UIDRAW" ) ) ) ;
				element.addListener( JUI.<UITextField>createGUIPanelEdge( _ui.getJSONObject( "UIEDGE" ) ) ) ;
				element.addListener( createEditText( _ui.getJSONObject( "UITEXT" ), element.getRatio() )  ) ;

				return element ;
			}

			private GUIEditText createEditText( final JSONObject _ui, final UIRatio _ratio )
			{
				if( _ui == null )
				{
					return null ;
				}

				final String text = _ui.optString( "TEXT", "" ) ;
				final String fontName = _ui.optString( "FONT", null ) ;
				final int fontSize = ( int )_ratio.toPixelY( ( float )_ui.optDouble( "FONT_SIZE", 0.42 ) ) ;

				final MalletFont font = ( fontName != null ) ? MalletFont.createByPixel( fontName, MalletFont.PLAIN, fontSize ) : null ;

				final GUIEditText listener = new GUIEditText( text, font ) ;
				listener.setColour( MalletColour.parseColour( _ui.optString( "COLOUR_TEXT", null ) ) ) ;

				final JSONObject align = _ui.optJSONObject( "ALIGNMENT_TEXT", null ) ;
				if( align != null )
				{
					listener.setAlignment( UI.Alignment.derive( align.optString( "X", null ) ),
										   UI.Alignment.derive( align.optString( "Y", null ) ) ) ;
				}

				return listener ;
			}
		} ) ;

		creators.put( "UIBUTTON", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final UIButton.Meta meta = new UIButton.Meta() ;

				applyBasics( _map, _ui, meta ) ;
				final UIElement element = UIElement.applyMeta( meta, new UIButton() ) ;
				applyLookup( _map, element, meta ) ;

				element.addListener( JUI.createGUIText( _ui.getJSONObject( "UITEXT" ), element.getRatio() ) ) ;
				element.addListener( JUI.<UIButton>createGUIPanelDraw( _ui.getJSONObject( "UIDRAW" ) ) ) ;
				element.addListener( JUI.<UIButton>createGUIPanelEdge( _ui.getJSONObject( "UIEDGE" ) ) ) ;

				return element ;
			}
		} ) ;

		creators.put( "UIMENU", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final UIMenu.Meta meta = new UIMenu.Meta() ;

				applyBasics( _map, _ui, meta ) ;
				meta.setType( UIMenu.Type.derive( _ui.optString( "LAYOUT", null ) ) ) ;
				meta.setThickness( ( float )_ui.optDouble( "THICKNESS", 0.0 ) ) ;

				final UIMenu element = UIMenu.applyMeta( meta, new UIMenu( meta.getType(), meta.getThickness() ) ) ;
				applyLookup( _map, element, meta ) ;

				addChildren( _map, element, _ui.getJSONArray( "CHILDREN" ) ) ;

				element.addListener( JUI.<UIMenu>createGUIDraw( _ui.getJSONObject( "UIDRAW" ) ) ) ;
				element.addListener( JUI.<UIMenu>createGUIPanelEdge( _ui.getJSONObject( "UIEDGE" ) ) ) ;

				return element ;
			}
		} ) ;

		creators.put( "UISPACER", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final UISpacer.Meta meta = new UISpacer.Meta() ;

				applyBasics( _map, _ui, meta ) ;
				meta.setAxis( UISpacer.Axis.derive( _ui.optString( "AXIS", null ) ) ) ;

				final UISpacer element = UISpacer.applyMeta( meta, new UISpacer( meta.getAxis() ) ) ;
				applyLookup( _map, element, meta ) ;

				element.addListener( JUI.<UISpacer>createGUIDraw( _ui.getJSONObject( "UIDRAW" ) ) ) ;
				element.addListener( JUI.<UISpacer>createGUIPanelEdge( _ui.getJSONObject( "UIEDGE" ) ) ) ;

				return element ;
			}
		} ) ;
		
		creators.put( "UICHECKBOX", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final UICheckbox.Meta meta = new UICheckbox.Meta() ;

				applyBasics( _map, _ui, meta ) ;
				final UIElement element = UIElement.applyMeta( meta, new UICheckbox() ) ;
				applyLookup( _map, element, meta ) ;

				element.addListener( createGUITick( _ui.getJSONObject( "UITICK" ) )  ) ;
				element.addListener( JUI.<UICheckbox>createGUIPanelDraw( _ui.getJSONObject( "UIDRAW" ) ) ) ;
				element.addListener( JUI.<UICheckbox>createGUIPanelEdge( _ui.getJSONObject( "UIEDGE" ) ) ) ;

				return element ;
			}

			private UICheckbox.GUITick createGUITick( final JSONObject _ui )
			{
				if( _ui == null )
				{
					return null ;
				}

				final UIElement.UV uv  = createUV( _ui.getJSONObject( "UV" ) ) ;
				final boolean retainRatio = _ui.optBoolean( "RETAIN_RATIO", false ) ;
				final String texturePath = _ui.optString( "TEXTURE", null ) ;

				final MalletTexture texture = ( texturePath != null ) ? new MalletTexture( texturePath ) : null ;

				final UICheckbox.GUITick tick = new UICheckbox.GUITick( texture, uv ) ;
				tick.setRetainRatio( retainRatio ) ;

				{
					final JSONObject align = _ui.optJSONObject( "ALIGNMENT", null ) ;
					if( align != null )
					{
						tick.setAlignment( UI.Alignment.derive( align.optString( "X", null ) ),
										   UI.Alignment.derive( align.optString( "Y", null ) ) ) ;
					}
				}

				return tick ;
			}
		} ) ;

		creators.put( "UILIST", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final UIList.Meta meta = new UIList.Meta() ;

				applyBasics( _map, _ui, meta ) ;
				meta.setType( UIList.Type.derive( _ui.optString( "LAYOUT", null ) ) ) ;

				final UILayout element = UILayout.applyMeta( meta, new UIList( meta.getType() ) ) ;
				applyLookup( _map, element, meta ) ;

				element.addListener( JUI.<UIList>createGUIDraw( _ui.getJSONObject( "UIDRAW" ) ) ) ;
				element.addListener( JUI.<UIList>createGUIPanelEdge( _ui.getJSONObject( "UIEDGE" ) ) ) ;
				element.addListener( createScrollbar( _ui.getJSONObject( "SCROLLBAR" ) ) ) ;

				addChildren( _map, element, _ui.getJSONArray( "CHILDREN" ) ) ;
				return element ;
			}

			private GUIScrollbar<UIList> createScrollbar( final JSONObject _ui )
			{
				if( _ui == null )
				{
					return null ;
				}

				final UIElement.UV uv  = createUV( _ui.getJSONObject( "UV" ) ) ;
				final String texturePath = _ui.optString( "TEXTURE", null ) ;

				final MalletTexture texture = ( texturePath != null ) ? new MalletTexture( texturePath ) : null ;

				return new GUIScrollbar(  texture, uv) ;
			}
		} ) ;

		creators.put( "UIMENU_ITEM", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final UIButton.Meta meta = new UIButton.Meta() ;

				applyBasics( _map, _ui, meta ) ;
				
				final UIMenu.Item element = new UIMenu.Item( JUI.createElement( _map, _ui.getJSONObject( "DROPDOWN" ) ) ) ;
				UIElement.applyMeta( meta, element ) ;

				applyLookup( _map, element, meta ) ;

				element.addListener( JUI.<UIMenu.Item>createGUIText( _ui.getJSONObject( "UITEXT" ), element.getRatio() ) ) ;
				element.addListener( JUI.<UIMenu.Item>createGUIDraw( _ui.getJSONObject( "UIDRAW" ) ) ) ;
				element.addListener( JUI.<UIMenu.Item>createGUIPanelEdge( _ui.getJSONObject( "UIEDGE" ) ) ) ;

				return element ;
			}
		} ) ;
	}

	/**
		Allow the developer to extend the UI system.
	*/
	public static void addGenerator( final String _id, final Generator _create )
	{
		creators.put( _id, _create ) ;
	}

	private final Map<String, UIElement> lookup = MalletMap.<String, UIElement>newMap() ;
	private final UIElement parent ;

	private JUI( final JSONObject _map )
	{
		parent = createElement( this, _map ) ;
		if( parent == null )
		{
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

		final JUI jui = new JUI( JSONObject.construct( stream ) ) ;
		stream.close() ;
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

	public static UIElement createElement( final JUI _map, final JSONObject _ui )
	{
		final String type = _ui.optString( "TYPE", null ) ;
		final Generator create = creators.get( type ) ;
		if( create == null )
		{
			// If a creator is not available we can't construct 
			// the requested type.
			Logger.println( type + " generator doesn't exist.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		return create.create( _map, _ui ) ;
	}

	private static void addChildren( final JUI _map, final UILayout _layout, final JSONArray _children )
	{
		if( _children == null )
		{
			return ;
		}

		final int size = _children.length() ;
		for( int i = 0; i < size; i++ )
		{
			final JSONObject jChild = _children.getJSONObject( i ) ;
			final UIElement child = createElement( _map, jChild ) ;
			if( child != null )
			{
				_layout.addElement( child ) ;
			}
		}
	}

	public static void applyBasics( final JUI _map, final JSONObject _ui, final UIElement.Meta _meta )
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

	public static <T extends UIElement> GUIDrawEdge<T> createGUIPanelEdge( final JSONObject _ui )
	{
		if( _ui == null )
		{
			return null ;
		}

		final GUIPanelEdge.Meta meta = new GUIPanelEdge.Meta() ;
		meta.setEdge( ( float )_ui.optDouble( "EDGE", 5.0 ) ) ;
		meta.setSheet( _ui.optString( "TEXTURE", "" ) ) ;
		meta.setNeutralColour( MalletColour.parseColour( _ui.optString( "COLOUR_NEUTRAL", null ) ) ) ;
		meta.setRolloverColour( MalletColour.parseColour( _ui.optString( "COLOUR_ROLLOVER", null ) ) ) ;
		meta.setClickedColour( MalletColour.parseColour( _ui.optString( "COLOUR_CLICKED", null ) ) ) ;

		return new GUIPanelEdge<T>( meta ) ;
	}
	
	public static <T extends UIElement> GUIDraw<T> createGUIDraw( final JSONObject _ui )
	{
		if( _ui == null )
		{
			return null ;
		}

		final GUIDraw.Meta meta = new GUIDraw.Meta() ;
		meta.setUV( createUV( _ui.getJSONObject( "UV" ) ) ) ;
		meta.setRetainRatio( _ui.optBoolean( "RETAIN_RATIO", false ) ) ;
		meta.setSheet( _ui.optString( "TEXTURE", null ) ) ;

		final JSONObject align = _ui.optJSONObject( "ALIGNMENT", null ) ;
		if( align != null )
		{
			meta.setAlignment( UI.Alignment.derive( align.optString( "X", null ) ),
							   UI.Alignment.derive( align.optString( "Y", null ) ) ) ;
		}

		return new GUIDraw<T>( meta ) ;
	}

	public static <T extends UIElement> GUIText<T> createGUIText( final JSONObject _ui, final UIRatio _ratio )
	{
		if( _ui == null )
		{
			return null ;
		}

		final int fontSize = ( int )_ratio.toPixelY( ( float )_ui.optDouble( "FONT_SIZE", 0.42 ) ) ;

		final GUIText.Meta meta = new GUIText.Meta() ;
		meta.setText( _ui.optString( "TEXT", "" ) ) ;
		meta.setFont( MalletFont.createByPixel( _ui.optString( "FONT", "Arial" ), MalletFont.PLAIN, fontSize ) ) ;
		meta.setColour( MalletColour.parseColour( _ui.optString( "COLOUR", null ) ) ) ;
		
		final JSONObject align = _ui.optJSONObject( "ALIGNMENT", null ) ;
		if( align != null )
		{
			meta.setAlignment( UI.Alignment.derive( align.optString( "X", null ) ),
							   UI.Alignment.derive( align.optString( "Y", null ) ) ) ;
		}

		return new GUIText<T>( meta ) ;
	}

	public static <T extends UITextField> GUIEditText<T> createGUIEditText( final JSONObject _ui, final UIRatio _ratio )
	{
		if( _ui == null )
		{
			return null ;
		}

		final int fontSize = ( int )_ratio.toPixelY( ( float )_ui.optDouble( "FONT_SIZE", 0.42 ) ) ;

		final GUIEditText.Meta meta = new GUIEditText.Meta() ;
		meta.setText( _ui.optString( "TEXT", "" ) ) ;
		meta.setFont( MalletFont.createByPixel( _ui.optString( "FONT", "Arial" ), MalletFont.PLAIN, fontSize ) ) ;
		meta.setColour( MalletColour.parseColour( _ui.optString( "COLOUR", null ) ) ) ;
		
		final JSONObject align = _ui.optJSONObject( "ALIGNMENT", null ) ;
		if( align != null )
		{
			meta.setAlignment( UI.Alignment.derive( align.optString( "X", null ) ),
							   UI.Alignment.derive( align.optString( "Y", null ) ) ) ;
		}

		return new GUIEditText<T>( meta ) ;
	}

	public static <T extends UIElement> GUIPanelDraw<T> createGUIPanelDraw( final JSONObject _ui )
	{
		if( _ui == null )
		{
			return null ;
		}

		final GUIPanelDraw.Meta meta = new GUIPanelDraw.Meta() ;
		meta.setNeutralUV( createUV( _ui.getJSONObject( "NEUTRAL_UV" ) ) ) ;
		meta.setRolloverUV( createUV( _ui.getJSONObject( "ROLLOVER_UV" ) ) ) ;
		meta.setClickedUV( createUV( _ui.getJSONObject( "CLICKED_UV" ) ) ) ;
		meta.setRetainRatio( _ui.optBoolean( "RETAIN_RATIO", false ) ) ;
		meta.setSheet( _ui.optString( "TEXTURE", "" ) ) ;

		final JSONObject align = _ui.optJSONObject( "ALIGNMENT", null ) ;
		if( align != null )
		{
			meta.setAlignment( UI.Alignment.derive( align.optString( "X", null ) ),
								UI.Alignment.derive( align.optString( "Y", null ) ) ) ;
		}

		return new GUIPanelDraw<T>( meta ) ;
	}

	public static UIElement.UV createUV( final JSONObject _uv )
	{
		if( _uv == null )
		{
			return null ;
		}

		final Vector2 min = Vector2.parseVector2( _uv.optString( "MIN", "0.0, 0.0" ) ) ;
		final Vector2 max = Vector2.parseVector2( _uv.optString( "MAX", "1.0, 1.0" ) ) ;
		return new UIElement.UV( min, max ) ;
	}

	public interface Generator
	{
		public UIElement create( final JUI _map, final JSONObject _ui ) ;
	}
}
