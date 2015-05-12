package com.linxonline.mallet.io.save ;

import java.util.ArrayList ;

import java.lang.Class ;
import java.lang.reflect.Field ;
import java.lang.annotation.Annotation ;

import java.lang.IllegalAccessException ;

import com.linxonline.mallet.io.formats.json.* ;
import com.linxonline.mallet.io.filesystem.* ;

public class Dump
{
	private static final JSONDump jsonDump = new JSONDump() ;
	private static final BinaryDump binDump = new BinaryDump() ;

	public static boolean dump( final Object _obj, Format _format, final String _file )
	{
		switch( _format )
		{
			case BINARY : return binDump.dump( _file, new IOClass( _obj, _obj.getClass() ) ) ;
			case JSON   : 
			default     : return jsonDump.dump( _file, new IOClass( _obj, _obj.getClass() ) ) ;
		}
	}

	/**
		Iterate over the Object storing Fields that have 
		been tagged for saving.
		In future implementations a class will be able to 
		be tagged, and Fields can be flagged to not be saved.
	*/
	private static class IOClass
	{
		private final Object object ;										// Object to be saved
		private final Class objectClass ;									// Class that represents object
		private final ArrayList<Field> fields = new ArrayList<Field>() ;	// Fields to be saved - Objects and Primitives
		private final IOClass parent ;										// Parent class

		public IOClass( final Object _obj, final Class _class )
		{
			object = _obj ;
			objectClass = _class ;

			final Class superClass = objectClass.getSuperclass() ;
			if( superClass != null )
			{
				parent = new IOClass( object, superClass ) ;
			}
			else
			{
				parent = null ;
			}

			acquireFields( _obj, _class ) ;
		}

		/**
			Acquire the fields that have been flagged for saving.
		*/
		private void acquireFields( final Object _obj, final Class _class )
		{
			final Field[] declaredFields = _class.getDeclaredFields() ;
			for( final Field field : declaredFields )
			{
				if( toSave( field, _class ) == true )
				{
					fields.add( field ) ;
				}
			}
		}
	}

	private static class JSONDump implements DumpFormat
	{
		public boolean dump( final String _file, final IOClass _class )
		{
			final JSONObject baseJSON = JSONObject.construct() ;
			if( dump( baseJSON, _class ) == false )
			{
				System.out.println( "Failed to construct JSON DUmp" ) ;
				return false ;
			}

			final FileStream file = GlobalFileSystem.getFile( _file ) ;
			final StringOutStream stream = file.getStringOutStream() ;
			if( stream.writeLine( baseJSON.toString() ) == false )
			{
				System.out.println( "Failed to write out JSON to " + _file ) ;
				return false ;
			}

			return stream.close() ;
		}

		private boolean dump( final JSONObject _obj, final IOClass _class )
		{
			_obj.put( "class", _class.objectClass.getName() ) ;

			final JSONObject fields = JSONObject.construct() ;			// Fields are stored here
			_obj.put( "fields", fields ) ;

			final JSONObject fieldTypes = JSONObject.construct() ;			// Fields are stored here
			_obj.put( "field-types", fieldTypes ) ;

			try
			{
				for( final Field field : _class.fields )
				{
					storeField( field, fields, fieldTypes, _class.object ) ;
				}
			}
			catch( IllegalAccessException ex )
			{
				ex.printStackTrace() ;
				return false ;
			}

			if( _class.parent != null )
			{
				final JSONObject parentJSON = JSONObject.construct() ;
				_obj.put( "parent", parentJSON ) ;
				return dump( parentJSON, _class.parent ) ;
			}

			return true ;
		}

		private boolean storeField( final Field _field, final JSONObject _fields, final JSONObject _fieldTypes, final Object _obj ) throws IllegalAccessException
		{
			_field.setAccessible( true ) ;
			final PrimType primType = PrimType.getType( _field ) ;
			final String name = _field.getName() ;
			switch( primType )
			{
				case CHAR    :
				{
					_fields.put( name, _field.getChar( _obj ) ) ;
					_fieldTypes.put( name, primType.toString() ) ;
					return true ;
				}
				case BYTE    :
				{
					_fields.put( name, _field.getByte( _obj ) ) ;
					_fieldTypes.put( name, primType.toString() ) ;
					return true ;
				}
				case INT     :
				{
					_fields.put( name, _field.getInt( _obj ) ) ;
					_fieldTypes.put( name, primType.toString() ) ;
					return true ;
				}
				case SHORT   :
				{
					_fields.put( name, _field.getShort( _obj ) ) ;
					_fieldTypes.put( name, primType.toString() ) ;
					return true ;
				}
				case LONG    :
				{
					_fields.put( name, _field.getLong( _obj ) ) ;
					_fieldTypes.put( name, primType.toString() ) ;
					return true ;
				}
				case FLOAT   :
				{
					_fields.put( name, _field.getFloat( _obj ) ) ;
					_fieldTypes.put( name, primType.toString() ) ;
					return true ;
				}
				case DOUBLE  :
				{
					_fields.put( name, _field.getDouble( _obj ) ) ;
					_fieldTypes.put( name, primType.toString() ) ;
					return true ;
				}
				case BOOLEAN :
				{
					_fields.put( name, _field.getBoolean( _obj ) ) ;
					_fieldTypes.put( name, primType.toString() ) ;
					return true ;
				}
				default      :
				{
					final Object object = _field.get( _obj ) ;
					final IOClass objectClass = new IOClass( object, object.getClass() ) ;
					final JSONObject jsonObject = JSONObject.construct() ;

					_fields.put( name, jsonObject ) ;
					_fieldTypes.put( name, "OBJECT" ) ;
					return dump( jsonObject, objectClass ) ;
				}
			}
		}
	}

	private static class BinaryDump implements DumpFormat
	{
		public boolean dump( final String _file, final IOClass _class )
		{
			return false ;
		}
	}

	private static interface DumpFormat
	{
		public boolean dump( final String _file, final IOClass _class ) ;
	}

	private static boolean toSave( final Field _field, final Class _class )
	{
		final boolean saveClass = ( _class.getAnnotation( SaveClass.class ) != null ) ;
		if( saveClass == true )
		{
			return _field.getAnnotation( NoSave.class ) == null ;
		}

		return _field.getAnnotation( Save.class ) != null ;
	}
}