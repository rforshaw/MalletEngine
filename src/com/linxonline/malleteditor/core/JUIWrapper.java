package com.linxonline.malleteditor.core ;

import java.util.List ;

import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.MalletFont ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.filesystem.StringOutStream ;
import com.linxonline.mallet.io.formats.json.JObject ;
import com.linxonline.mallet.io.formats.json.JArray ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public class JUIWrapper
{
	public static boolean saveWrapper( final UIWrapper _wrapper, final String _path )
	{
		final FileStream file = GlobalFileSystem.getFile( _path ) ;
		file.create() ;

		if( file.exists() == false )
		{
			return false ;
		}

		final IAbstractModel model = _wrapper.getMeta() ;
		final JObject ui = JUIWrapper.createJSON( _wrapper, model.root() ) ;

		final StringOutStream stream = file.getStringOutStream() ;
		stream.writeLine( ui.toString( 2 ) ) ;

		stream.close() ;
		return true ;
	}

	private static JObject createJSON( final UIWrapper _wrapper, final UIModelIndex _index )
	{
		final JObject jsonObj = JObject.construct() ;

		final UIElement.Meta meta = _wrapper.getMeta() ;
		jsonObj.put( "TYPE", meta.getElementType() ) ;
		addModelTo( meta, _index, jsonObj ) ;

		final List<UIWrapper> children = _wrapper.getChildren() ;
		if( children != null )
		{
			final JArray jsonChildren = JArray.construct() ;
			for( final UIWrapper child : children )
			{
				final IAbstractModel model = child.getMeta() ;
				jsonChildren.put( JUIWrapper.createJSON( child, model.root() ) ) ;
			}
			jsonObj.put( "CHILDREN", jsonChildren ) ;
		}

		final List<UIElement.MetaComponent> components = meta.getComponents( MalletList.<UIElement.MetaComponent>newList() ) ;
		for( final UIElement.MetaComponent component : components )
		{
			final String type = getMetaComponentID( component.getType() ) ;
			if( type == null )
			{
				Logger.println( "Component type unknown: " + component.getType(), Logger.Verbosity.MAJOR ) ;
				continue ;
			}

			final JObject jsonComp = JObject.construct() ;
			jsonObj.put( type, jsonComp ) ;
			addModelTo( component, component.root(), jsonComp ) ;
		}

		return jsonObj ;
	}

	private static void addModelTo( final UIAbstractModel _model, final UIModelIndex _index, final JObject _to )
	{
		final int rowCount = _model.rowCount( _index ) ;
		final int columnCount = _model.columnCount( _index ) ;

		for( int i = 0; i < rowCount; i++ )
		{
			for( int j = 0; j < columnCount; j++ )
			{
				final UIModelIndex index = new UIModelIndex( _index, i, j ) ;
				final IVariant variant = _model.getData( index, IAbstractModel.Role.User ) ;
				addVariantTo( variant, _to ) ;
			}
		}
	}

	private static void addVariantTo( final IVariant _variant, final JObject _to )
	{
		switch( _variant.getType() )
		{
			case AVariable.STRING_TYPE  :
			{
				_to.put( _variant.getName(), _variant.toString() ) ;
				break ;
			}
			case AVariable.BOOLEAN_TYPE :
			{
				_to.put( _variant.getName(), _variant.toBool() ) ;
				break ;
			}
			case AVariable.INT_TYPE     :
			{
				_to.put( _variant.getName(), _variant.toInt() ) ;
				break ;
			}
			case AVariable.FLOAT_TYPE   :
			{
				_to.put( _variant.getName(), _variant.toFloat() ) ;
				break ;
			}
			case AVariable.OBJECT_TYPE  :
			{
				final Object obj = _variant.toObject() ;
				if( obj instanceof Vector2 )
				{
					final Vector2 vec = ( Vector2 )obj ;
					_to.put( _variant.getName(), vec.x + "," + vec.y ) ;
				}
				else if( obj instanceof Vector3 )
				{
					final Vector3 vec = ( Vector3 )obj ;
					_to.put( _variant.getName(), vec.x + "," + vec.y + "," + vec.z ) ;
				}
				else if( obj instanceof MalletColour )
				{
					final MalletColour colour = ( MalletColour )obj ;
					_to.put( _variant.getName(), colour.getRed() + "," + colour.getGreen() + "," + colour.getBlue() ) ;
				}
				else if( obj instanceof MalletFont )
				{
					final MalletFont font = ( MalletFont )obj ;

					_to.put( _variant.getName(), font.getFontName() ) ;
					_to.put( "FONT_SIZE", 0.42f ) ;
				}
				else if( obj instanceof UIElement.UV )
				{
					final UIElement.UV uv = ( UIElement.UV )obj ;
					final JObject jsonUV = JObject.construct() ;

					_to.put( _variant.getName(), jsonUV ) ;
					jsonUV.put( "MIN", uv.min.x + "," + uv.min.y ) ;
					jsonUV.put( "MAX", uv.max.x + "," + uv.max.y ) ;
				}
				else if( obj instanceof UI.Alignment )
				{
					if( _to.has( "ALIGNMENT" ) == false )
					{
						_to.put( "ALIGNMENT", JObject.construct() ) ;
					}

					final JObject jsonAlign = _to.getJObject( "ALIGNMENT" ) ;
					final UI.Alignment align = ( UI.Alignment )obj ;
					switch( _variant.getName() )
					{
						case "ALIGNMENT_X" : jsonAlign.put( "X", align.toString() ) ; break ;
						case "ALIGNMENT_Y" : jsonAlign.put( "Y", align.toString() ) ; break ;
					}
				}
				else
				{
					_to.put( _variant.getName(), _variant.toString() ) ;
				}
				break ;
			}
			default                             : break ;
		}
	}

	private static String getMetaComponentID( final String _type )
	{
		switch( _type )
		{
			case "UIELEMENT_GUIDRAW"       : return "UIDRAW" ;
			case "UIELEMENT_GUIDRAWEDGE"   : return "UIEDGE" ;
			case "UITEXTFIELD_GUIEDITTEXT" : return "UITEXT" ;
			case "UIELEMENT_GUIPANELDRAW"  : return "UIDRAW" ;
			case "UIELEMENT_GUIPANELEDGE"  : return "UIEDGE" ;
			case "UILIST_GUISCROLLBAR"     : return "SCROLLBAR" ;
			case "UIELEMENT_GUITEXT"       : return "UITEXT" ;
			case "UICHECKBOX_GUITICK"      : return "UITICK" ;
			default                        : return null ;
		}
	}

	public static UIWrapper loadWrapper( final String _path )
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

		return createWrapper( JObject.construct( stream ) ) ;
	}

	public static UIWrapper createWrapper( final JObject _ui )
	{
		final UIElement.Meta meta = JUI.createMeta( _ui ) ;
		if( meta == null )
		{
			return null ;
		}
	
		final UIWrapper wrapper = new UIWrapper( meta ) ;
		addChildren( _ui.optJArray( "CHILDREN", null ), wrapper ) ;
		return wrapper ;
	}

	private static void addChildren( final JArray _children, final UIWrapper _wrapper )
	{
		if( _children == null )
		{
			return ;
		}

		final int size = _children.length() ;
		for( int i = 0; i < size; i++ )
		{
			final JObject jChild = _children.getJObject( i ) ;
			final UIWrapper child = createWrapper( jChild ) ;
			if( child != null )
			{
				_wrapper.insertUIWrapper( child ) ;
			}
		}
	}
}
