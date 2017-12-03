package com.linxonline.malleteditor.core ;

import java.util.List ;
import java.util.Map ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.formats.json.JSONObject ;
import com.linxonline.mallet.io.formats.json.JSONArray ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.ui.* ;

public class JUIWrapper
{
	private final static Map<String, Generator> creators = MalletMap.<String, Generator>newMap() ;
	static
	{
		creators.put( "UIELEMENT", new Generator()
		{
			public UIPacket create( final JSONObject _ui )
			{
				return new JUIPacket( "UIELEMENT" )
				{
					@Override
					public boolean supportsChildren()
					{
						return false ;
					}
				} ;
			}
		} ) ;

		creators.put( "UIBUTTON", new Generator()
		{
			public UIPacket create( final JSONObject _ui )
			{
				return new JUIPacket( "UIBUTTON" )
				{
					@Override
					public boolean supportsChildren()
					{
						return false ;
					}
				} ;
			}
		} ) ;

		creators.put( "UICHECKBOX", new Generator()
		{
			public UIPacket create( final JSONObject _ui )
			{
				return new JUIPacket( "UICHECKBOX" )
				{
					@Override
					public boolean supportsChildren()
					{
						return false ;
					}
				} ;
			}
		} ) ;

		creators.put( "UILAYOUT", new Generator()
		{
			public UIPacket create( final JSONObject _ui )
			{
				return new JUIPacket( "UILAYOUT" )
				{
					@Override
					public boolean supportsChildren()
					{
						return true ;
					}
				} ;
			}
		} ) ;
	}

	/**
		Allow the developer to extend the UI Packet system.
	*/
	public static void addGenerator( final String _id, final Generator _create )
	{
		creators.put( _id, _create ) ;
	}

	private final UIWrapper parent ;

	private JUIWrapper( final JSONObject _map )
	{
		parent = createUIWrapper( _map ) ;
	}

	private UIWrapper createUIWrapper( final JSONObject _map )
	{
		final UIPacket packet = createUIPacket( _map ) ;
		if( packet == null )
		{
			Logger.println( "JUIWrapper packet not created: " + _map.toString(), Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		System.out.println( "Create UIWrapper" ) ;
		final UIWrapper wrapper = new UIWrapper( packet ) ;

		final JSONArray children = _map.optJSONArray( "CHILDREN", null ) ;
		if( children != null )
		{
			final int size = children.length() ;
			for( int i = 0; i < size; i++ )
			{
				final JSONObject jsonChild = children.getJSONObject( i ) ;
				final UIWrapper wrapperChild = createUIWrapper( jsonChild ) ;
				// This implementation is recursive - if the lineage 
				// of a family is too long it will result in a stack overflow.
				if( wrapperChild != null )
				{
					wrapper.insertUIWrapper( wrapperChild ) ;
				}
			}
		}

		return wrapper ;
	}

	private UIPacket createUIPacket( final JSONObject _ui )
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

		return create.create( _ui ) ;
	}

	private UIWrapper getParent()
	{
		return parent ;
	}

	public static UIWrapper load( final String _path )
	{
		if( GlobalFileSystem.isExtension( _path, ".jui", ".JUI", ".jUI" ) == false )
		{
			Logger.println( "JUI: " + _path + " extension not supported.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		final FileStream stream = GlobalFileSystem.getFile( _path ) ;
		if( stream.exists() == false )
		{
			Logger.println( "JUI: " + _path + " doesn't exist.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		final JUIWrapper jui = new JUIWrapper( JSONObject.construct( stream ) ) ;
		stream.close() ;
		return jui.getParent() ;
	}

	public static boolean save( final UIWrapper _wrapper, final String _file )
	{
		return false ;
	}

	/**
		Construct a JSON object that is compatible 
		with the JUI system.
	*/
	private static JSONObject createJSON( final UIPacket _ui )
	{
		final JSONObject root = JSONObject.construct() ;
		root.put( "TYPE", _ui.getType() ) ;

		final Vector3 vec3 = new Vector3() ;
		root.put( "LENGTH", JUIWrapper.toString( _ui.getLength( vec3 ) ) ) ;
		root.put( "MAX_LENGTH", JUIWrapper.toString( _ui.getMaximumLength( vec3 ) ) ) ;
		root.put( "MIN_LENGTH", JUIWrapper.toString( _ui.getMinimumLength( vec3 ) ) ) ;
		
		System.out.println( root ) ;
		return root ;
	}

	private static String toString( final Vector3 _vec3 )
	{
		final StringBuilder buffer = new StringBuilder() ;
		buffer.append( _vec3.x ) ;
		buffer.append( ',' ) ;
		buffer.append( _vec3.y ) ;
		buffer.append( ',' ) ;
		buffer.append( _vec3.z ) ;
		return buffer.toString() ;
	}
	
	private static abstract class JUIPacket extends UIPacket
	{
		public JUIPacket( final String _type )
		{
			super( _type ) ;
		}

		@Override
		public UIElement createElement()
		{
			return JUI.createElement( null, JUIWrapper.createJSON( this ) ) ;
		}
	}
	
	public interface Generator
	{
		public UIPacket create( final JSONObject _ui ) ;
	}
}
