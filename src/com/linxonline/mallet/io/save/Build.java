package com.linxonline.mallet.io.save ;

import java.util.Collection ;
import java.util.Map ;

import java.lang.Class ;
import java.lang.reflect.Field ;
import java.lang.annotation.Annotation ;

import java.lang.IllegalAccessException ;

import com.linxonline.mallet.io.formats.json.* ;
import com.linxonline.mallet.io.filesystem.* ;

/**
	Supports the importing of Java Primitives, Collections, and Maps.
	Will also construct classes that used @SaveClass, @NoSave, and @Save.
*/
public class Build
{
	private static final JSONBuild jsonBuild = new JSONBuild() ;
	private static final BinaryBuild binBuild = new BinaryBuild() ;

	public static Object build( final String _file, final Format _format )
	{
		switch( _format )
		{
			case BINARY : return binBuild.build( _file ) ;
			case JSON   : 
			default     : return jsonBuild.build( _file ) ;
		}
	}

	private static class JSONBuild implements BuildFormat
	{
		public Object build( final String _file )
		{
			final FileStream file = GlobalFileSystem.getFile( _file ) ;
			if( file.exists() == false )
			{
				System.out.println( "File not found: " + _file ) ;
				return null ;
			}

			final StringInStream stream = file.getStringInStream() ;
			final StringBuilder builder = new StringBuilder() ;
			String line = null ;
			while( ( line = stream.readLine() ) != null )
			{
				builder.append( line ) ;
			}

			stream.close() ;
			final JSONObject root = JSONObject.construct( builder.toString() ) ;
			return construct( root ) ;
		}

		private Object construct( final JSONObject _obj )
		{
			final String name = _obj.optString( "class", null ) ;
			if( name == null )
			{
				System.out.println( "Unable to construct object, class not defined." ) ;
				return null ;
			}

			final ClassLoader loader = Build.class.getClassLoader() ;
			try
			{
				final Class clazz = loader.loadClass( name ) ;
				
				if( isPrimitive( clazz ) == true )
				{
					return createPrimitive( _obj ) ;
				}
				else
				{
					final Object object = clazz.newInstance() ;
					insertFields( object, clazz, _obj ) ;
					return object ;
				}
			}
			catch( ClassNotFoundException ex )
			{
				ex.printStackTrace() ;
			}
			catch( InstantiationException ex )
			{
				ex.printStackTrace() ;
			}
			catch( IllegalAccessException ex )
			{
				ex.printStackTrace() ;
			}
			catch( NoSuchFieldException ex )
			{
				ex.printStackTrace() ;
			}

			return null ;
		}

		private Object createPrimitive( final JSONObject _json )
		{
			final PrimType type = PrimType.getType( _json.optString( "type", null ) ) ;
			switch( type )
			{
				case CHAR    : return ' ' ;//field.setChar( _obj, fields.optChar( key, ' ' ) ) ; break ;
				case BYTE    : return ( byte )_json.optInt( "value", 0 ) ;
				case INT     : return _json.optInt( "value", 0 ) ;
				case SHORT   : return ( short )_json.optInt( "value", 0 ) ;
				case LONG    : return _json.optLong( "value", 0L ) ;
				case FLOAT   : return ( float )_json.optDouble( "value", 0.0 ) ;
				case DOUBLE  : return _json.optDouble( "value", 0.0 ) ;
				case BOOLEAN : return _json.optBoolean( "value", false ) ;
				case STRING  : return _json.optString( "value", "" ) ;
				default      : return null ;
			}
		}

		private void insertFields( final Object _obj, final Class _class, final JSONObject _json ) throws ClassNotFoundException,
																										  IllegalAccessException,
																										  NoSuchFieldException
		{
			final JSONObject fields = _json.optJSONObject( "fields" ) ;
			final JSONObject fieldTypes = _json.optJSONObject( "field-types" ) ;
			if( fields != null || fieldTypes != null )
			{
				final String[] keys = fields.keys() ;
				for( final String key : keys )
				{
					final Field field = _class.getDeclaredField( key ) ;
					field.setAccessible( true ) ;
					final PrimType type = PrimType.getType( fieldTypes.optString( key, null ) ) ;
					switch( type )
					{
						case CHAR    : break ;//field.setChar( _obj, fields.optChar( key, ' ' ) ) ; break ;
						case BYTE    : field.setByte( _obj, ( byte )fields.optInt( key, 0 ) ) ;        break ;
						case INT     : field.setInt( _obj, fields.optInt( key, 0 ) ) ;                 break ;
						case SHORT   : field.setShort( _obj, ( short )fields.optInt( key, 0 ) ) ;      break ;
						case LONG    : field.setLong( _obj, fields.optLong( key, 0L ) ) ;              break ;
						case FLOAT   : field.setFloat( _obj, ( float )fields.optDouble( key, 0.0 ) ) ; break ;
						case DOUBLE  : field.setDouble( _obj, fields.optDouble( key, 0.0 ) ) ;         break ;
						case BOOLEAN : field.setBoolean( _obj, fields.optBoolean( key, false ) ) ;     break ;
						case STRING  : field.set( _obj, fields.optString( key, "" ) ) ;                break ;
						// If it is not a primitive type then it must be an object.
						// Whether it is a String, ArrayList, HashMap, or a Mallet 
						// Object is unkown, but we'll find out soon enough. 
						default      : field.set( _obj, construct( fields.optJSONObject( key ) ) ) ;   break ;
					}
				}
			}

			final JSONArray collections = _json.optJSONArray( "collections" ) ;
			final JSONArray mapKeys = _json.optJSONArray( "keys" ) ;
			if( collections != null && mapKeys == null )
			{
				final Collection list = ( Collection )_obj ;
				final int size = collections.length() ;
				for( int i = 0; i < size; i++ )
				{
					list.add( construct( collections.optJSONObject( i ) ) ) ;
				}
			}
			else if( collections != null && mapKeys != null )
			{
				final Map map = ( Map )_obj ;
				final int size = collections.length() ;
				for( int i = 0; i < size; i++ )
				{
					final JSONObject key = mapKeys.optJSONObject( i ) ;
					final JSONObject value = collections.optJSONObject( i ) ;
					map.put( construct( key ), construct( value ) ) ;
				}
			}

			final JSONObject parent = _json.optJSONObject( "parent" ) ;
			if( parent != null )
			{
				final String name = parent.optString( "class", null ) ;
				final ClassLoader loader = _class.getClassLoader() ;
				insertFields( _obj, loader.loadClass( name ), parent ) ;
			}
		}
	}

	private static class BinaryBuild implements BuildFormat
	{
		public Object build( final String _file )
		{
			return null ;
		}
	}

	private static interface BuildFormat
	{
		public Object build( final String _file ) ;
	}

	private static boolean isPrimitive( final Class _class )
	{
		return PrimType.getType( _class ) != PrimType.UNKNOWN ;
	}
}