package com.linxonline.malleteditor.core ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.formats.json.JSONObject ;
import com.linxonline.mallet.io.formats.json.JSONArray ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.ui.* ;

public class JUIWrapper
{
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
