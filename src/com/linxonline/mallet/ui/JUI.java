package com.linxonline.mallet.ui ;

import java.util.Map ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.formats.json.JSONObject ;
import com.linxonline.mallet.io.formats.json.JSONArray ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.MalletMap ;

import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.MalletFont ;

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
				final UIElement element = new UIElement() ;
				applyLengths( element, _ui ) ;
				applyLayer( element, _ui ) ;
				applyLookup( _map, element, _ui ) ;

				final UIListener<UIElement> uiListener = JUI.<UIElement>createUIElementUIListener( _ui.getJSONObject( "UILISTENER" ) ) ;
				if( uiListener != null )
				{
					element.addListener( uiListener ) ;
				}

				return element ;
			}
		} ) ;

		creators.put( "UILAYOUT", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final UILayout.Type type = UILayout.Type.derive( _ui.optString( "LAYOUT", null ) ) ;

				final UILayout element = new UILayout( type ) ;
				applyLengths( element, _ui ) ;
				applyLayer( element, _ui ) ;
				applyLookup( _map, element, _ui ) ;

				final UIListener<UILayout> uiListener = JUI.<UILayout>createUIElementUIListener( _ui.getJSONObject( "UILISTENER" ) ) ;
				if( uiListener != null )
				{
					element.addListener( uiListener ) ;
				}

				addChildren( _map, element, _ui.getJSONArray( "CHILDREN" ) ) ;
				return element ;
			}
		} ) ;

		creators.put( "UIWINDOW_LAYOUT", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final UILayout.Type type = UILayout.Type.derive( _ui.optString( "LAYOUT", null ) ) ;

				final UILayout element = UIFactory.constructWindowLayout( type ) ;
				applyLengths( element, _ui ) ;
				applyLayer( element, _ui ) ;
				applyLookup( _map, element, _ui ) ;

				final UIListener<UILayout> uiListener = JUI.<UILayout>createUIElementUIListener( _ui.getJSONObject( "UILISTENER" ) ) ;
				if( uiListener != null )
				{
					element.addListener( uiListener ) ;
				}

				addChildren( _map, element, _ui.getJSONArray( "CHILDREN" ) ) ;
				return element ;
			}
		} ) ;

		creators.put( "UIBUTTON", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final UIButton element = new UIButton() ;
				applyLengths( element, _ui ) ;
				applyLayer( element, _ui ) ;
				applyLookup( _map, element, _ui ) ;

				final UIButton.UIListener uiListener = createListener( _ui.getJSONObject( "UILISTENER" ) ) ;
				if( uiListener != null )
				{
					element.addListener( uiListener ) ;
				}

				return element ;
			}

			private UIButton.UIListener createListener( final JSONObject _ui )
			{
				if( _ui == null )
				{
					return null ;
				}

				final UIButton.UV neutralUV  = createUV( _ui.getJSONObject( "NEUTRAL_UV" ) ) ;
				final UIButton.UV rolloverUV = createUV( _ui.getJSONObject( "ROLLOVER_UV" ) ) ;
				final UIButton.UV clickedUV  = createUV( _ui.getJSONObject( "CLICKED_UV" ) ) ;

				if( neutralUV == null || rolloverUV == null || clickedUV == null )
				{
					Logger.println( "JUI: UIListener specified without valid uv-maps.", Logger.Verbosity.MAJOR ) ;
					return null ;
				}

				final String text = _ui.optString( "TEXT", "" ) ;
				final String fontName = _ui.optString( "FONT", null ) ;
				final int fontSize = _ui.optInt( "FONT_SIZE", 12 ) ;
				final boolean retainRatio = _ui.optBoolean( "RETAIN_RATIO", false ) ;

				final MalletFont font = ( fontName != null ) ? new MalletFont( fontName, fontSize ) : null ;
				final MalletTexture texture = new MalletTexture( _ui.optString( "TEXTURE", "" ) ) ;

				final UIButton.UIListener listener = UIButton.createUIListener( text, font, texture, neutralUV, rolloverUV, clickedUV ) ;
				listener.setRetainRatio( retainRatio ) ;

				listener.setTextColour( MalletColour.parseColour( _ui.optString( "COLOUR_TEXT", null ) ) ) ;

				{
					final JSONObject align = _ui.optJSONObject( "ALIGNMENT", null ) ;
					if( align != null )
					{
						listener.setAlignment( UI.Alignment.derive( align.optString( "X", null ) ),
											UI.Alignment.derive( align.optString( "Y", null ) ) ) ;
					}
				}

				{
					final JSONObject align = _ui.optJSONObject( "ALIGNMENT_TEXT", null ) ;
					if( align != null )
					{
						listener.setAlignment( UI.Alignment.derive( align.optString( "X", null ) ),
											UI.Alignment.derive( align.optString( "Y", null ) ) ) ;
					}
				}

				return listener ;
			}
		} ) ;

		creators.put( "UIMENU", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final String layout = _ui.optString( "LAYOUT", null ) ;
				final double length = _ui.optDouble( "LENGTH", 0.0 ) ;

				final UIMenu element = new UIMenu( UILayout.Type.derive( layout ), ( float )length ) ;
				applyLookup( _map, element, _ui ) ;
				applyLayer( element, _ui ) ;
				addChildren( _map, element, _ui.getJSONArray( "CHILDREN" ) ) ;

				final UIListener<UIMenu> uiListener = JUI.<UIMenu>createUIElementUIListener( _ui.getJSONObject( "UILISTENER" ) ) ;
				if( uiListener != null )
				{
					element.addListener( uiListener ) ;
				}

				return element ;
			}
		} ) ;

		creators.put( "UISPACER", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final String axis = _ui.optString( "AXIS", null ) ;
				final UISpacer element = new UISpacer( UISpacer.Axis.derive( axis ) ) ;

				final UIListener<UISpacer> uiListener = JUI.<UISpacer>createUIElementUIListener( _ui.getJSONObject( "UILISTENER" ) ) ;
				if( uiListener != null )
				{
					element.addListener( uiListener ) ;
				}

				return element ;
			}
		} ) ;
		
		creators.put( "UICHECKBOX", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final UICheckbox element = new UICheckbox() ;
				applyLengths( element, _ui ) ;
				applyLayer( element, _ui ) ;
				applyLookup( _map, element, _ui ) ;

				final UICheckbox.UIListener uiListener = createListener( _ui.getJSONObject( "UILISTENER" ) ) ;
				if( uiListener != null )
				{
					element.addListener( uiListener ) ;
				}

				return element ;
			}

			private UICheckbox.UIListener createListener( final JSONObject _ui )
			{
				if( _ui == null )
				{
					return null ;
				}

				final UICheckbox.UV neutralUV  = createUV( _ui.getJSONObject( "NEUTRAL_UV" ) ) ;
				final UICheckbox.UV rolloverUV = createUV( _ui.getJSONObject( "ROLLOVER_UV" ) ) ;
				final UICheckbox.UV tickUV     = createUV( _ui.getJSONObject( "TICK_UV" ) ) ;

				if( neutralUV == null || rolloverUV == null || tickUV == null )
				{
					Logger.println( "JUI: UIListener specified without valid uv-maps.", Logger.Verbosity.MAJOR ) ;
					return null ;
				}

				final boolean retainRatio = _ui.optBoolean( "RETAIN_RATIO", false ) ;
				final MalletTexture texture = new MalletTexture( _ui.optString( "TEXTURE", "" ) ) ;

				final UICheckbox.UIListener listener = UICheckbox.createUIListener( texture, neutralUV, rolloverUV, tickUV ) ;
				listener.setRetainRatio( retainRatio ) ;

				listener.setTextColour( MalletColour.parseColour( _ui.optString( "COLOUR_TEXT", null ) ) ) ;

				{
					final JSONObject align = _ui.optJSONObject( "ALIGNMENT", null ) ;
					if( align != null )
					{
						listener.setAlignment( UI.Alignment.derive( align.optString( "X", null ) ),
											UI.Alignment.derive( align.optString( "Y", null ) ) ) ;
					}
				}

				return listener ;
			}
		} ) ;

		creators.put( "UILIST", new Generator()
		{
			public UIElement create( final JUI _map, final JSONObject _ui )
			{
				final String layout = _ui.optString( "LAYOUT", null ) ;
				final double length = _ui.optDouble( "LENGTH", 0.0 ) ;

				final UIMenu element = new UIMenu( UILayout.Type.derive( layout ), ( float )length ) ;
				applyLookup( _map, element, _ui ) ;
				applyLayer( element, _ui ) ;
				addChildren( _map, element, _ui.getJSONArray( "CHILDREN" ) ) ;

				final UIListener<UIMenu> uiListener = JUI.<UIMenu>createUIElementUIListener( _ui.getJSONObject( "UILISTENER" ) ) ;
				if( uiListener != null )
				{
					element.addListener( uiListener ) ;
				}

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

		return new JUI( JSONObject.construct( stream ) ) ;
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

	public UIElement get( final String _name )
	{
		return lookup.get( _name ) ;
	}

	public <T> T get( final String _name, final Class<T> _clazz )
	{
		final UIElement element = lookup.get( _name ) ;
		if( element != null )
		{
			if( _clazz.isInstance( element ) == true )
			{
				return _clazz.cast( element ) ;
			}
		}

		return null ;
	}

	private static UIElement createElement( final JUI _map, final JSONObject _ui )
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

	public static void applyLayer( final UIElement _element, final JSONObject _ui )
	{
		_element.setLayer( _ui.optInt( "LAYER", 0 ) ) ;
	}
	
	public static void applyLengths( final UIElement _element, final JSONObject _ui )
	{
		{
			final Vector3 minLength = Vector3.parseVector3( _ui.optString( "MIN_LENGTH", null ) ) ;
			if( minLength != null )
			{
				_element.setMinimumLength( minLength.x, minLength.y, minLength.z ) ;
			}
		}

		{
			final Vector3 maxLength = Vector3.parseVector3( _ui.optString( "MAX_LENGTH", null ) ) ;
			if( maxLength != null )
			{
				_element.setMaximumLength( maxLength.x, maxLength.y, maxLength.z ) ;
			}
		}

		{
			final Vector3 length = Vector3.parseVector3( _ui.optString( "LENGTH", null ) ) ;
			if( length != null )
			{
				_element.setLength( length.x, length.y, length.z ) ;
			}
		}
	}

	public static void applyLookup( final JUI _map, final UIElement _element, final JSONObject _ui )
	{
		final String name = _ui.optString( "NAME", null ) ;
		if( name != null )
		{
			// Elements are only added to the lookup 
			// table if they have a name defined.
			// Some elements are not important enough to 
			// have a name defined.
			_map.lookup.put( name, _element ) ;
		}
	}

	public static <T extends UIElement> UIListener<T> createUIElementUIListener( final JSONObject _ui )
	{
		if( _ui == null )
		{
			return null ;
		}

		final UIElement.UV uv  = createUV( _ui.getJSONObject( "UV" ) ) ;

		final String text = _ui.optString( "TEXT", "" ) ;
		final String fontName = _ui.optString( "FONT", null ) ;
		final int fontSize = _ui.optInt( "FONT_SIZE", 12 ) ;
		final boolean retainRatio = _ui.optBoolean( "RETAIN_RATIO", false ) ;
		final String texturePath = _ui.optString( "TEXTURE", null ) ;

		final MalletFont font = ( fontName != null ) ? new MalletFont( fontName, fontSize ) : null ;
		final MalletTexture texture = ( texturePath != null ) ? new MalletTexture( texturePath ) : null ;

		final UIFactory.UIBasicListener<T> listener = UIFactory.<T>constructUIListener( text, font, texture, uv ) ;
		listener.setRetainRatio( retainRatio ) ;

		listener.setTextColour( MalletColour.parseColour( _ui.optString( "COLOUR_TEXT", null ) ) ) ;

		{
			final JSONObject align = _ui.optJSONObject( "ALIGNMENT", null ) ;
			if( align != null )
			{
				listener.setAlignment( UI.Alignment.derive( align.optString( "X", null ) ),
									   UI.Alignment.derive( align.optString( "Y", null ) ) ) ;
			}
		}

		{
			final JSONObject align = _ui.optJSONObject( "ALIGNMENT_TEXT", null ) ;
			if( align != null )
			{
				listener.setAlignment( UI.Alignment.derive( align.optString( "X", null ) ),
									   UI.Alignment.derive( align.optString( "Y", null ) ) ) ;
			}
		}

		return listener ;
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
