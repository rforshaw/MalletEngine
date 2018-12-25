package com.linxonline.malleteditor.core ;

import java.util.List ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.filesystem.StringOutStream ;
import com.linxonline.mallet.io.formats.json.JSONObject ;
import com.linxonline.mallet.io.formats.json.JSONArray ;

import com.linxonline.mallet.util.Logger ;
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
		final JSONObject ui = JUIWrapper.createJSON( _wrapper, model.root() ) ;

		final StringOutStream stream = file.getStringOutStream() ;
		stream.writeLine( ui.toString() ) ;

		file.close( stream ) ;
		return true ;
	}

	private static JSONObject createJSON( final UIWrapper _wrapper, final UIModelIndex _index )
	{
		final JSONObject jsonObj = JSONObject.construct() ;

		final UIElement.Meta meta = _wrapper.getMeta() ;
		final int rowCount = meta.rowCount( _index ) ;
		final int columnCount = meta.columnCount( _index ) ;

		for( int i = 0; i < rowCount; i++ )
		{
			for( int j = 0; j < columnCount; j++ )
			{
				final UIModelIndex index = new UIModelIndex( _index, i, j ) ;
				final IVariant variant = meta.getData( index, IAbstractModel.Role.User ) ;
				
				switch( variant.getType() )
				{
					case VariableInterface.STRING_TYPE  :
					{
						jsonObj.put( variant.getName(), variant.toString() ) ;
						break ;
					}
					case VariableInterface.BOOLEAN_TYPE :
					{
						jsonObj.put( variant.getName(), variant.toBool() ) ;
						break ;
					}
					case VariableInterface.INT_TYPE     :
					{
						jsonObj.put( variant.getName(), variant.toInt() ) ;
						break ;
					}
					case VariableInterface.FLOAT_TYPE   :
					{
						jsonObj.put( variant.getName(), variant.toFloat() ) ;
						break ;
					}
					case VariableInterface.OBJECT_TYPE  :
					{
						final Object obj = variant.toObject() ;
						if( obj instanceof Vector2 )
						{
							final Vector2 vec = ( Vector2 )obj ;
							jsonObj.put( variant.getName(), vec.x + "," + vec.y ) ;
						}
						else if( obj instanceof Vector3 )
						{
							final Vector3 vec = ( Vector3 )obj ;
							jsonObj.put( variant.getName(), vec.x + "," + vec.y + "," + vec.z ) ;
						}
						else
						{
							jsonObj.put( variant.getName(), variant.toString() ) ;
						}
						break ;
					}
					default                             : break ;
				}
				
				//JUIWrapper.createJSON( _wrapper, index ) ;
			}
		}
		
		final List<UIWrapper> children = _wrapper.getChildren() ;
		if( children != null )
		{
			final JSONArray jsonChildren = JSONArray.construct() ;
			for( final UIWrapper child : children )
			{
				final IAbstractModel model = child.getMeta() ;
				jsonChildren.put( JUIWrapper.createJSON( child, model.root() ) ) ;
			}
			jsonObj.put( "CHILDREN", jsonChildren ) ;
		}

		return jsonObj ;
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

		return createWrapper( JSONObject.construct( stream ) ) ;
	}

	public static UIWrapper createWrapper( final JSONObject _ui )
	{
		final UIElement.Meta meta = JUI.createMeta( _ui ) ;
		if( meta == null )
		{
			return null ;
		}
	
		final UIWrapper wrapper = new UIWrapper( meta ) ;
		addChildren( _ui.optJSONArray( "CHILDREN", null ), wrapper ) ;
		return wrapper ;
	}

	private static void addChildren( final JSONArray _children, final UIWrapper _wrapper )
	{
		if( _children == null )
		{
			return ;
		}

		final int size = _children.length() ;
		for( int i = 0; i < size; i++ )
		{
			final JSONObject jChild = _children.getJSONObject( i ) ;
			final UIWrapper child = createWrapper( jChild ) ;
			if( child != null )
			{
				_wrapper.insertUIWrapper( child ) ;
			}
		}
	}
}
