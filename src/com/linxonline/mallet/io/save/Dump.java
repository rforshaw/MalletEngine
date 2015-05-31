package com.linxonline.mallet.io.save ;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.Collection ;
import java.util.Set ;
import java.util.Map ;

import java.lang.Class ;
import java.lang.reflect.Field ;
import java.lang.reflect.Array ;
import java.lang.reflect.Modifier ;
import java.lang.annotation.Annotation ;

import java.lang.IllegalAccessException ;

import com.linxonline.mallet.io.formats.json.* ;
import com.linxonline.mallet.io.filesystem.* ;

/**
	Supports the exporting of Java Primitives, Collections, and Maps.
	Will export classes that use @SaveClass, @NoSave, and @Save.
	Current dump implementation does not handle references. For example, 
	if two pointers point to the same object, then Dump will duplicate 
	the same object for each pointer. Anything that has a reference to 
	an object, but does not own it, should not save it out.
*/
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
	*/
	private static class IOClass
	{
		private final Object object ;												// Object to be saved
		private final Class objectClass ;											// Class that represents object
		private final ArrayList<Field> fields = new ArrayList<Field>() ;			// Fields to be saved - Objects and Primitives
		private final ArrayList<IOClass> mapKeys = new ArrayList<IOClass>() ;		// Objects that represent a Map's key
		private final ArrayList<IOClass> collections = new ArrayList<IOClass>() ;	// Objects to be saved within a Collection or a Map values
		private final IOClass parent ;												// Parent class

		public IOClass( final Object _obj, final Class _class )
		{
			System.out.println( "OBJ: " + _obj + " Class: " + _class ) ;
			object = _obj ;
			objectClass = _class ;

			// Collections and Maps are objects however, 
			// we cannot define how they should be saved using 
			// the defined @Save Annotations. Because of this, 
			// we must handle them manually.
			// Any class that implements Collection or Map interfaces, 
			// will be handled.

			if( isCollection( _class ) == true )
			{
				acquireFields( ( Collection )_obj ) ;
				parent = null ;
			}
			else if( isMap( _class ) == true )
			{
				acquireFields( ( Map )_obj ) ;
				parent = null ;
			}
			else if( isPrimitive( _class ) == true )
			{
				// Primitves do not have fields, they are the 
				// value themselves. A primitve can be represented 
				// as either an individual variable or as an array.
				parent = null ;
			}
			else
			{
				final Class superClass = objectClass.getSuperclass() ;
				if( superClass != null )
				{
					// Iterate over the class's super classes 
					// to save out any variables than may be 
					// flagged within them.
					parent = new IOClass( object, superClass ) ;
				}
				else
				{
					parent = null ;
				}

				acquireFields( _obj, _class ) ;
			}
		}

		/**
			Acquire the objects contained within a 
			Collection.
		*/
		private void acquireFields( final Collection _list )
		{
			for( final Object obj : _list )
			{
				collections.add( new IOClass( obj, obj.getClass() ) ) ;
			}
		}

		/**
			Acquire the objects contained within a 
			Map, key is stored in mapKeys, while 
			the values are stored within collections.
		*/
		private void acquireFields( final Map _map )
		{
			final Set keys = _map.keySet() ;
			for( final Object key : keys )
			{
				mapKeys.add( new IOClass( key, key.getClass() ) ) ;
				final Object value = _map.get( key ) ;
				collections.add( new IOClass( value, value.getClass() ) ) ;
			}
		}

		/**
			Acquire the fields that have been flagged for saving.
		*/
		private void acquireFields( final Object _obj, final Class _class )
		{
			try
			{
				final Field[] declaredFields = _class.getDeclaredFields() ;
				for( final Field field : declaredFields )
				{
					field.setAccessible( true ) ;
					if( field.get( _obj ) != null )
					{
						// Fields set to null cannot be saved,
						// will result in a horrible crash.
						if( toSave( field, _class ) == true )
						{
							fields.add( field ) ;
						}
					}
				}
			}
			catch( IllegalAccessException ex )
			{
				ex.printStackTrace() ;
			}
		}
	}

	private static class JSONDump implements DumpFormat
	{
		private final HashMap<String, Object> saved = new HashMap<String, Object>() ;

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
				saved.clear() ;
				stream.close() ;
				return false ;
			}

			saved.clear() ;
			return stream.close() ;
		}

		private boolean dump( final JSONObject _obj, final IOClass _class )
		{
			final String id = Integer.toHexString( System.identityHashCode( _class.object ) ) ;
			saved.put( id, _class.object ) ;

			_obj.put( "class", _class.objectClass.getName() ) ;
			_obj.put( "id", id ) ;

			if( _class.fields.isEmpty() == false )
			{
				try
				{
					final JSONObject fields = JSONObject.construct() ;			// Fields are stored here
					_obj.put( "fields", fields ) ;

					final JSONObject fieldTypes = JSONObject.construct() ;		// Fields are stored here
					_obj.put( "field-types", fieldTypes ) ;

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
			}

			// Collections and Maps are objects, however 
			// we cannot define how they should be saved using 
			// the defined @Save Annotations. Because of this, 
			// we must handle them manually.
			// Any class that implements Collection or Map interfaces, 
			// will be handled.

			// Used by Map and Collections - stores values
			if( _class.collections.isEmpty() == false )
			{
				final JSONArray collections = JSONArray.construct() ;
				_obj.put( "collections", collections ) ;
				for( final IOClass item : _class.collections )
				{
					final JSONObject jsonItem = JSONObject.construct() ;
					if( dump( jsonItem, item ) == true )
					{
						collections.put( jsonItem ) ;
					}
				}
			}

			// Used by Map - stores keys
			if( _class.mapKeys.isEmpty() == false )
			{
				final JSONArray keys = JSONArray.construct() ;
				_obj.put( "keys", keys ) ;
				for( final IOClass item : _class.mapKeys )
				{
					final JSONObject jsonItem = JSONObject.construct() ;
					if( dump( jsonItem, item ) == true )
					{
						keys.put( jsonItem ) ;
					}
				}
			}

			if( _class.parent != null )
			{
				// Write out the parent class, and 
				// all subsequent classes, shouldstop when 
				// we get to java.lang.Object 
				final JSONObject parentJSON = JSONObject.construct() ;
				_obj.put( "parent", parentJSON ) ;
				return dump( parentJSON, _class.parent ) ;
			}

			if( isPrimitive( _class.objectClass ) == true )
			{
				if( _class.objectClass.isArray() == true )
				{
					return storePrimitives( _obj, _class.objectClass, _class.object ) ;
				}
				else
				{
					return storePrimitive( _obj, _class.objectClass, _class.object ) ;
				}
			}

			return true ;
		}

		private boolean storePrimitive( final JSONObject _obj, final Class _class, final Object _val )
		{
			final PrimType primType = PrimType.getType( _class ) ;
			switch( primType )
			{
				case CHAR    :
				{
					_obj.put( "value", ( Character )_val ) ;
					_obj.put( "type", primType.toString() ) ;
					return true ;
				}
				case BYTE    :
				{
					_obj.put( "value", ( Byte )_val ) ;
					_obj.put( "type", primType.toString() ) ;
					return true ;
				}
				case INT     :
				{
					_obj.put( "value", ( Integer )_val ) ;
					_obj.put( "type", primType.toString() ) ;
					return true ;
				}
				case SHORT   :
				{
					_obj.put( "value", ( Short )_val ) ;
					_obj.put( "type", primType.toString() ) ;
					return true ;
				}
				case LONG    :
				{
					_obj.put( "value", ( Long )_val ) ;
					_obj.put( "type", primType.toString() ) ;
					return true ;
				}
				case FLOAT   :
				{
					_obj.put( "value", ( Float )_val ) ;
					_obj.put( "type", primType.toString() ) ;
					return true ;
				}
				case DOUBLE  :
				{
					_obj.put( "value", ( Double )_val ) ;
					_obj.put( "type", primType.toString() ) ;
					return true ;
				}
				case BOOLEAN :
				{
					_obj.put( "value", ( Boolean )_val ) ;
					_obj.put( "type", primType.toString() ) ;
					return true ;
				}
				case STRING  :
				{
					_obj.put( "value", ( String )_val ) ;
					_obj.put( "type", primType.toString() ) ;
					return true ;
				}
				default      :
				{
					return false ;
				}
			}
		}

		private boolean storePrimitives( final JSONObject _obj, final Class _class, final Object _val )
		{
			final int length = Array.getLength( _val ) ;
			final JSONArray array = JSONArray.construct() ;

			final PrimType primType = PrimType.getType( _class ) ;
			_obj.put( "values", array ) ;
			_obj.put( "type", primType.toString() ) ;

			for( int i = 0; i < length; i++ )
			{
				switch( primType )
				{
					case CHAR    : array.put( Array.getChar( _val, i ) ) ;       break ;
					case BYTE    : array.put( Array.getByte( _val, i ) ) ;       break ;
					case INT     : array.put( Array.getInt( _val, i ) ) ;        break ;
					case SHORT   : array.put( Array.getShort( _val, i ) ) ;      break ;
					case LONG    : array.put( Array.getLong( _val, i ) ) ;       break ;
					case FLOAT   : array.put( Array.getFloat( _val, i ) ) ;      break ;
					case DOUBLE  : array.put( Array.getDouble( _val, i ) ) ;     break ;
					case BOOLEAN : array.put( Array.getBoolean( _val, i ) ) ;    break ;
					case STRING  : array.put( ( String )Array.get( _val, i ) ) ; break ;
					default      : return false ;
				}
			}

			return true ;
		}

		private boolean storeField( final Field _field, final JSONObject _fields, final JSONObject _fieldTypes, final Object _obj ) throws IllegalAccessException
		{
			_field.setAccessible( true ) ;
			final Class classType = _field.getType() ;

			if( isStatic( _field ) == true )
			{
				final String id = Integer.toHexString( System.identityHashCode( _field.get( _obj ) ) ) ;
				// If a field is set as static 
				// then we only want to save out 
				// the field once, as it will be used 
				// by all instances of the class.
				if( saved.containsKey( id ) == true )
				{
					return true ;
				}
			}

			if( classType.isArray() == true )
			{
				return storeArrayField( _field, _fields, _fieldTypes, _obj ) ;
			}
			else
			{
				return storeItemField( _field, _fields, _fieldTypes, _obj ) ;
			}
		}

		private boolean storeArrayField( final Field _field, final JSONObject _fields, final JSONObject _fieldTypes, final Object _obj ) throws IllegalAccessException
		{
			final String name = _field.getName() ;
			final Class classType = _field.getType() ;
			final PrimType primType = PrimType.getType( classType ) ;

			final JSONArray array = JSONArray.construct() ;

			_fields.put( name, array ) ;
			_fieldTypes.put( name, primType.toString() ) ;

			final Object value = _field.get( _obj ) ;
			final int length = Array.getLength( value ) ;

			for( int i = 0; i < length; i++ )
			{
				switch( primType )
				{
					case CHAR    : array.put( Array.getChar( value, i ) ) ;       break ;
					case BYTE    : array.put( Array.getByte( value, i ) ) ;       break ;
					case INT     : array.put( Array.getInt( value, i ) ) ;        break ;
					case SHORT   : array.put( Array.getShort( value, i ) ) ;      break ;
					case LONG    : array.put( Array.getLong( value, i ) ) ;       break ;
					case FLOAT   : array.put( Array.getFloat( value, i ) ) ;      break ;
					case DOUBLE  : array.put( Array.getDouble( value, i ) ) ;     break ;
					case BOOLEAN : array.put( Array.getBoolean( value, i ) ) ;    break ;
					case STRING  : array.put( ( String )Array.get( value, i ) ) ; break ;
					default      :
					{
						final Object object = Array.get( value, i ) ;
						final IOClass objectClass = new IOClass( object, object.getClass() ) ;
						final JSONObject jsonObject = JSONObject.construct() ;

						_fieldTypes.put( name, "OBJECT" ) ;
						array.put( jsonObject ) ;
						dump( jsonObject, objectClass ) ;
					}
				}
			}

			return true ;
		}

		private boolean storeItemField( final Field _field, final JSONObject _fields, final JSONObject _fieldTypes, final Object _obj ) throws IllegalAccessException
		{
			final String name = _field.getName() ;
			final Class classType = _field.getType() ;
			final PrimType primType = PrimType.getType( classType ) ;

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
				case STRING  :
				{
					_fields.put( name, ( String )_field.get( _obj ) ) ;
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

	private static boolean isReference( final Class _class )
	{
		return ( _class.getAnnotation( Reference.class ) != null ) ;
	}

	private static boolean isCollection( final Class _class )
	{
		return Collection.class.isAssignableFrom( _class ) ;
	}

	private static boolean isStatic( final Class _class )
	{
		return Modifier.isStatic( _class.getModifiers() ) ;
	}

	private static boolean isStatic( final Field _field )
	{
		return Modifier.isStatic( _field.getModifiers() ) ;
	}

	private static boolean isMap( final Class _class )
	{
		return Map.class.isAssignableFrom( _class ) ;
	}

	private static boolean isPrimitive( final Class _class )
	{
		return PrimType.getType( _class ) != PrimType.UNKNOWN ;
	}

	private static boolean toSave( final Field _field, final Class _class )
	{
		final boolean isReference = ( _class.getAnnotation( Reference.class ) != null ) ;
		if( isReference == true )
		{
			// Currently we do not want to save out references.
			// A reference is a pointer to an object in which the 
			// pointer does not have absolute control.
			// For example, the Entity pointer, parent, in a Component.
			return false ;
		}
	
		final boolean saveClass = ( _class.getAnnotation( SaveClass.class ) != null ) ;
		if( saveClass == true )
		{
			return _field.getAnnotation( NoSave.class ) == null ;
		}

		return _field.getAnnotation( Save.class ) != null ;
	}
}