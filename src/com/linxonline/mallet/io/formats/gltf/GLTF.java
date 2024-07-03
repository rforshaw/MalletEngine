package com.linxonline.mallet.io.formats.gltf ;

import java.io.UnsupportedEncodingException ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.filesystem.ByteInStream ;
import com.linxonline.mallet.io.formats.json.JObject ;
import com.linxonline.mallet.io.formats.json.JArray ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.util.Tuple ;

import com.linxonline.mallet.renderer.IShape.Attribute ;
import com.linxonline.mallet.renderer.IShape.Style ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.MalletColour ;

public final class GLTF
{
	private final JChunk header ;
	private final BinChunk bin ;

	private GLTF( final JChunk _header, final BinChunk _bin )
	{
		header = _header ;
		bin = _bin ;
	}

	/**
		Return the available meshe names within this gltf file.
		You can use the index of the name with createMeshByIndex().
	*/
	public String[] getMeshNames()
	{
		final int size = header.getMeshSize() ;
		final String[] names = new String[size] ;

		for( int i = 0; i < size; ++i )
		{
			final JChunk.Mesh mesh = header.getMesh( i ) ;
			names[i] = mesh.name ;
		}

		return names ;
	}

	public Shape[] createMeshByIndex( final int _index, final Tuple<String, Attribute>[] _attributes )
	{
		final JChunk.Mesh mesh = header.getMesh( _index ) ;
		final Shape[] shapes = new Shape[mesh.primitives.length] ;
		for( int j = 0; j < shapes.length; ++j )
		{
			final JChunk.Primitive primitive = mesh.primitives[j] ;
			shapes[j] = createShape( header, primitive, _attributes ) ;
		}

		return shapes ;
	}

	public Shape[] createMeshByName( final String _name, final Tuple<String, Attribute>[] _attributes )
	{
		final int size = header.getMeshSize() ;
		for( int i = 0; i < size; ++i )
		{
			final JChunk.Mesh mesh = header.getMesh( i ) ;
			if( _name.equals( mesh.name ) == true )
			{
				final Shape[] shapes = new Shape[mesh.primitives.length] ;
				for( int j = 0; j < shapes.length; ++j )
				{
					final JChunk.Primitive primitive = mesh.primitives[j] ;
					shapes[j] = createShape( header, primitive, _attributes ) ;
				}

				return shapes ;
			}
		}

		throw new RuntimeException( "Failed to find requested mesh: " + _name ) ;
	}

	private Shape createShape( final JChunk _chunk, JChunk.Primitive _primitive, final Tuple<String, Attribute>[] _attributes )
	{
		final int length = _attributes.length ;

		// The model may contain more attributes than we are interested in, 
		// pull out the buffers we want to use for our vertices.
		final Attribute[] swivel = new Attribute[length] ;
		final JChunk.Accessor[] accessors = new JChunk.Accessor[length] ;

		for( int i = 0; i < length; ++i )
		{
			final Tuple<String, Attribute> attribute = _attributes[i] ;
			final String name = attribute.getLeft() ;
			if( name.length() <= 0 )
			{
				// If the user has requested an attribute that has no 
				// name then they intend to provide the data themselves after 
				// the Shape has been created.
				swivel[i] = attribute.getRight() ;
				accessors[i] = createAccessor( swivel[i] ) ;
				continue ;
			}

			final JChunk.Attribute gAttribute = _primitive.getAttributeByName( name ) ;
			accessors[i] = _chunk.getAccessor( gAttribute.index ) ;
			swivel[i] = attribute.getRight() ;

			checkAttributeSanity( swivel[i], accessors[i] ) ;
		}

		final int[] indices = readIndices( _chunk, _primitive ) ;

		final IReader[] readers = constructVertexReaders( _chunk, accessors ) ;
		final byte[] temp = new byte[4 * 16] ;	// Store at most a 4x4 matrix
		final int count = accessors[0].count ;

		final Shape shape = new Shape( determinMode( _primitive ), swivel, indices.length, count ) ;
		shape.addIndices( indices ) ;

		final Object[] vertex = Attribute.createVert( swivel ) ;
		for( int i = 0; i < count; ++i )
		{
			for( int j = 0; j < readers.length; ++j )
			{
				final IReader reader = readers[j] ;
				final int read = reader.readBytes( i, temp ) ;
				switch( reader.type() )
				{
					case "SCALAR" : throw new RuntimeException( "SCALAR not supported as ." ) ;
					case "VEC2"   :
					{
						switch( reader.componentType() )
						{
							default       : throw new RuntimeException( "Component type not supported for VEC2" ) ;
							case 5126 :
							{
								final Vector2 vec2 = ( Vector2 )vertex[j] ;
								vec2.x = ConvertBytes.toFloat( temp, 0 ) ;
								vec2.y = ConvertBytes.toFloat( temp, 4 ) ;
								break ;
							}// Float
						}
						break ;
					}
					case "VEC3"   :
					{
						switch( reader.componentType() )
						{
							default       : throw new RuntimeException( "Component type not supported for VEC3" ) ;
							case 5126 :
							{
								final Vector3 vec3 = ( Vector3 )vertex[j] ;
								vec3.x = ConvertBytes.toFloat( temp, 0 ) ;
								vec3.y = ConvertBytes.toFloat( temp, 4 ) ;
								vec3.z = ConvertBytes.toFloat( temp, 8 ) ;
								break ;
							}// Float
						}
						break ;
					}
					case "VEC4"   :
					{
						switch( reader.componentType() )
						{
							default       : throw new RuntimeException( "Component type not supported for VEC3" ) ;
							case 5120 :
							{
								final MalletColour colour = ( MalletColour )vertex[j] ;
								colour.changeColour( temp[0], temp[1], temp[2], temp[3] ) ;
								break ;
							}// Float
						}
						break ;
					}
					case "MAT2"   : throw new RuntimeException( "MAT2 not supported as ." ) ;
					case "MAT3"   : throw new RuntimeException( "MAT3 not supported as ." ) ;
					case "MAT4"   : throw new RuntimeException( "MAT4 not supported as ." ) ;
				}
			}
			shape.copyVertex( vertex ) ;
		}

		return shape ;
	}

	private JChunk.Accessor createAccessor( final Attribute _swivel )
	{
		switch( _swivel )
		{
			default    : throw new RuntimeException( "Unknown swivel type." ) ;
			case VEC3  : return new PointAccessor() ;
			case FLOAT : return new ColourAccessor() ;
			case VEC2  : return new UVAccessor() ;
		}
	}

	private int[] readIndices( final JChunk _chunk, final JChunk.Primitive _gPrimitive )
	{
		if( _gPrimitive.indices == -1 )
		{
			// TODO: The primitive doesn't have an indices array
			// we need to create one.
			return new int[0] ;
		}
	
		final byte[] temp = new byte[4] ;	// Store at most an int

		final JChunk.Accessor accessor = _chunk.getAccessor( _gPrimitive.indices ) ;
		final JChunk.BufferView view = _chunk.getBufferView( accessor.bufferView ) ;

		final Reader reader = new Reader( bin, accessor, view ) ;
		final int count = accessor.count ;

		final int[] indices = new int[count] ;
		for( int i = 0; i < count; ++i )
		{
			final int read = reader.readBytes( i, temp ) ;
			switch( read )
			{
				case 2 : indices[i] = ConvertBytes.toShort( temp, 0 ) ; break ;
				case 4 : indices[i] = ConvertBytes.toInt( temp, 0 ) ; break ;
			}
		}

		return indices ;
	}

	private IReader[] constructVertexReaders( final JChunk _chunk, final JChunk.Accessor[] _accessors )
	{
		final int length = _accessors.length ;
		final IReader[] readers = new IReader[length] ;
		for( int i = 0; i < length; ++i )
		{
			final JChunk.Accessor accessor = _accessors[i] ;
			readers[i] = _accessors[i].createReader( _chunk, bin ) ;
		}

		return readers ;
	}

	private Style determinMode( JChunk.Primitive _primitive )
	{
		switch( _primitive.mode )
		{
			default : throw new RuntimeException( "Mode " + _primitive.mode + " not supported." ) ;
			case 1  : return Style.LINES ;
			case 3  : return Style.LINE_STRIP ;
			case 4  : return Style.FILL ;
		}
	}

	private void checkAttributeSanity( final Attribute _swivel, final JChunk.Accessor _accessor )
	{
		switch( _swivel )
		{
			case VEC3  :
			{
				if( _accessor.isPoint() == false  )
				{
					throw new RuntimeException( "Accessor is not in vec3 format." ) ;
				}
				break;
			}
			case FLOAT :
			{
				if( _accessor.isColour() == false )
				{
					throw new RuntimeException( "Accessor is not in colour format." ) ;
				}
				break ;
			}
			case VEC2     :
			{
				if( _accessor.isUV() == false )
				{
					throw new RuntimeException( "Accessor is not in uv format." ) ;
				}
				break ;
			}
		}
	}

	// glb binaries are little endian
	// Java is Big endian
	public static GLTF load( final String _file )
	{
		if( GlobalFileSystem.isExtension( _file, ".glb", ".GLB" ) == false )
		{
			Logger.println( _file + " not a valid extension.", Logger.Verbosity.NORMAL ) ;
			return null ;
		}

		final FileStream stream = GlobalFileSystem.getFile( _file ) ;
		if( stream.exists() == false )
		{
			Logger.println( _file + " does not exist.", Logger.Verbosity.NORMAL ) ;
			return null ;
		}

		final int size = ( int )stream.getSize() ;
		final byte[] data = new byte[size] ;

		try( final ByteInStream in = stream.getByteInStream() )
		{
			final int read = in.readBytes( data, 0, 12 ) ;
			if( read != 12 )
			{
				Logger.println( "Failed to read header expected 12 bytes.", Logger.Verbosity.NORMAL ) ;
				return null ;
			}

			final int magic = ConvertBytes.toInt( data, 0 ) ;
			if( magic == 0x46546C67 )
			{
				Logger.println( "Expected glTF at the start of file.", Logger.Verbosity.NORMAL ) ;
				return null ;
			}

			ConvertBytes.flipEndian( data, 4, 4 ) ;
			final long version = toUnsignedLong( ConvertBytes.toInt( data, 4 ) ) ;

			ConvertBytes.flipEndian( data, 8, 4 ) ;
			final long length = toUnsignedLong( ConvertBytes.toInt( data, 8 ) ) ;

			final JChunk jChunk = readJChunk( in, data, 12 ) ;
			if( jChunk == null )
			{
				return null ;
			}

			final BinChunk binChunk = readBinChunk( in, data, jChunk.getEnd() ) ;
			if( binChunk == null )
			{
				return null ;
			}

			return new GLTF( jChunk, binChunk ) ;
		}
		catch( Exception ex )
		{
			return null ;
		}
	}

	private static JChunk readJChunk( final ByteInStream _in, final byte[] _data, int _offset )
	{
		int read = _in.readBytes( _data, _offset, 8 ) ;
		if( read < 8 )
		{
			Logger.println( "Failed to read chunk header from file, expected 8 bytes.", Logger.Verbosity.NORMAL ) ;
			return null ;
		}

		ConvertBytes.flipEndian( _data, _offset, 4 ) ;
		final int length = ( int )toUnsignedLong( ConvertBytes.toInt( _data, _offset ) ) ;

		_offset += 4 ;
		final int type = ConvertBytes.toInt( _data, _offset ) ;
		if( type == 0x4E4F534A )
		{
			Logger.println( "Expected JSON Chunk got " + type + " instead.", Logger.Verbosity.NORMAL ) ;
			return null ;
		}

		_offset += 4 ;
		read = _in.readBytes( _data, _offset, length ) ;
		if( read < length )
		{
			Logger.println( "Failed to read chunk data expected " + length + " bytes received: " + read + " instead.", Logger.Verbosity.NORMAL ) ;
			return null ;
		}

		try
		{
			final JObject obj = JObject.construct( new String( _data, _offset, length, "UTF-8" ) ) ;
			return new JChunk( _offset, length, type, obj ) ;
		}
		catch( UnsupportedEncodingException ex )
		{
			Logger.println( "Failed to encode string utf8 not supported.", Logger.Verbosity.NORMAL ) ;
			return null ;
		}
	}

	private static BinChunk readBinChunk( final ByteInStream _in, final byte[] _data, int _offset )
	{
		int read = _in.readBytes( _data, _offset, 8 ) ;
		if( read < 8 )
		{
			Logger.println( "Failed to read chunk header from file, expected 8 bytes.", Logger.Verbosity.NORMAL ) ;
			return null ;
		}

		ConvertBytes.flipEndian( _data, _offset, 4 ) ;
		final int length = ( int )toUnsignedLong( ConvertBytes.toInt( _data, _offset ) ) ;

		_offset += 4 ;
		final int type = ConvertBytes.toInt( _data, _offset ) ;
		if( type == 0x004E4942 )
		{
			Logger.println( "Expected BIN Chunk got " + type + " instead.", Logger.Verbosity.NORMAL ) ;
			return null ;
		}

		_offset += 4 ;
		read = _in.readBytes( _data, _offset, length ) ;

		final byte[] data = ConvertBytes.newBytes( _data, _offset, length ) ;
		return new BinChunk( _offset, length, type, data ) ;
	}

	/**
		Not available in TeaVM so we'll implement our own
		version instead of using Integer.toUnsignedLong().
	*/
	private static long toUnsignedLong( final int _val )
	{
		return ( ( long ) _val ) & 0xffffffffL ;
	}

	public static class JChunk
	{
		public final int offset ;
		public final int length ;
		public final int type ;

		private final JObject obj ;

		private final int scene ;
		private final JArray scenes ;
		private final JArray nodes ;

		private final JArray bufferViews ;
		private final JArray buffers ;

		private final JArray meshes ;
		private final JArray accessors ;

		private final JArray materials ;
		private final JArray textures ;
		private final JArray images ;

		public JChunk( final int _offset,
					   final int _length,
					   final int _type,
					   final JObject _obj )
		{
			offset = _offset ;
			length = _length ;
			type = _type ;

			obj = _obj ;

			scene = obj.optInt( "scene", 0 ) ;
			scenes = obj.optJArray( "scenes", null ) ;
			nodes = obj.optJArray( "nodes", null ) ;

			bufferViews = obj.optJArray( "bufferViews", null ) ;
			buffers = obj.optJArray( "buffers", null ) ;

			meshes = obj.optJArray( "meshes", null ) ;
			accessors = obj.optJArray( "accessors", null ) ;

			materials = obj.optJArray( "materials", null ) ;
			textures = obj.optJArray( "textures", null ) ;
			images = obj.optJArray( "images", null ) ;
		}

		public Buffer getBuffer( final int _index )
		{
			return new Buffer( buffers.optJObject( _index, null ) ) ;
		}

		public int getBufferSize()
		{
			return buffers.length() ;
		}

		public BufferView getBufferView( final int _index )
		{
			return new BufferView( bufferViews.optJObject( _index, null ) ) ;
		}

		public int getBufferViewSize()
		{
			return bufferViews.length() ;
		}

		public Mesh getMesh( final int _index )
		{
			return new Mesh( meshes.optJObject( _index, null ) ) ;
		}

		public int getMeshSize()
		{
			return meshes.length() ;
		}

		public Accessor getAccessor( final int _index )
		{
			return new Accessor( accessors.optJObject( _index, null ) ) ;
		}

		public int getAccessorSize()
		{
			return accessors.length() ;
		}

		public int getEnd()
		{
			return offset + length ;
		}

		@Override
		public String toString()
		{
			final StringBuilder builder = new StringBuilder() ;
			builder.append( "Offset: " ) ;
			builder.append( offset ) ;
			builder.append( " Length: " ) ;
			builder.append( length ) ;
			builder.append( " Type: " ) ;
			builder.append( type ) ;
			builder.append( " Data : " ) ;
			builder.append( obj.toString() ) ;
			return builder.toString() ;
		}

		public static class Buffer
		{
			public final int byteLength ;
			public final String uri ; 

			public Buffer( final JObject _obj )
			{
				byteLength = _obj.optInt( "byteLength", -1 ) ;
				uri = _obj.optString( "uri", null ) ;
			}
		}

		public static class BufferView
		{
			public final int byteOffset ;
			public final int byteLength ;
			public final int buffer ;

			public BufferView( final JObject _obj )
			{
				byteOffset = _obj.optInt( "byteOffset", -1 ) ;
				byteLength = _obj.optInt( "byteLength", -1 ) ;
				buffer = _obj.optInt( "buffer", -1 ) ;
			}
		}

		public static class Mesh
		{
			public final String name ;
			public final Primitive[] primitives ;

			public Mesh( final JObject _obj )
			{
				name = _obj.optString( "name", null ) ;

				final JArray jPrimitives = _obj.optJArray( "primitives", null ) ;
				primitives = new Primitive[jPrimitives.length()] ;
				for( int i = 0; i < primitives.length; ++i )
				{
					primitives[i] = new Primitive( jPrimitives.getJObject( i ) ) ;
				}
			}
		}

		public static class Primitive
		{
			public final int indices ;
			public final int material ;
			public final int mode ;
			public final Attribute[] attributes ;

			public Primitive( final JObject _obj )
			{
				indices = _obj.optInt( "indices", -1 ) ;
				material = _obj.optInt( "material", -1 ) ;
				mode = _obj.optInt( "mode", 4 ) ;

				final JObject jAttributes = _obj.optJObject( "attributes", null ) ;
				final String[] keys = jAttributes.keys() ;
				attributes = new Attribute[keys.length] ;

				for( int i = 0; i < keys.length; ++i )
				{
					final String name = keys[i] ;
					final int index = jAttributes.optInt( name, -1 ) ;
					attributes[i] = new Attribute( name, index ) ;
				}
			}

			public Attribute getAttributeByName( final String _name )
			{
				for( final Attribute attribute : attributes )
				{
					if( attribute.name.equals( _name ) == true )
					{
						return attribute ;
					}
				}

				return null ;
			}
		}

		public static class Attribute
		{
			public final String name ;
			public final int index ;

			public Attribute( final String _name, final int _index )
			{
				name = _name ;
				index = _index ;
			}
		}

		public static class Accessor
		{
			public final int componentType ;
			public final int bufferView ;
			public final int count ;
			public final String type ;

			public Accessor( final JObject _obj )
			{
				componentType = _obj.optInt( "componentType", -1 ) ;
				bufferView = _obj.optInt( "bufferView", -1 ) ;
				count = _obj.optInt( "count", -1 ) ;
				type = _obj.optString( "type", null ) ;
			}

			public Accessor( final int _componentType,
							 final int _bufferView,
							 final int _count,
							 final String _type )
			{
				componentType = _componentType ;
				bufferView = _bufferView ;
				count = _count ;
				type = _type ;
			}

			public boolean isPoint()
			{
				return componentType == 5126 && "VEC3".equals( type ) ;
			}

			public boolean isNormal()
			{
				return componentType == 5126 && "VEC3".equals( type ) ;
			}

			public boolean isUV()
			{
				return componentType == 5126 && "VEC2".equals( type ) ;
			}

			public boolean isColour()
			{
				return componentType == 5121 && "VEC4".equals( type ) ;
			}

			public int componentSizeInBytes()
			{
				switch( componentType )
				{
					default       : throw new RuntimeException( "Unkown component." ) ;
					case 5120 : return 1 ;	// Byte
					case 5121 : return 1 ;	// UByte
					case 5122 : return 2 ;	// Short
					case 5123 : return 2 ;	// UShort
					case 5125 : return 4 ;	// UInt
					case 5126 : return 4 ;	// Float
				}
			}

			public int numOfComponents()
			{
				switch( type )
				{
					default       : throw new RuntimeException( "Unkown type." ) ;
					case "SCALAR" : return 1 ;
					case "VEC2"   : return 2 ;
					case "VEC3"   : return 3 ;
					case "VEC4"   : return 4 ;
					case "MAT2"   : return 4 ;
					case "MAT3"   : return 9 ;
					case "MAT4"   : return 16 ;
				}
			}

			public IReader createReader( JChunk _chunk, BinChunk _bin )
			{
				final JChunk.BufferView view = _chunk.getBufferView( bufferView ) ;
				return new Reader( _bin, this, view ) ;
			}
		}
	}

	public static class BinChunk
	{
		public final int offset ;
		public final int length ;
		public final int type ;
		public final byte[] data ;

		public BinChunk( final int _offset,
						 final int _length,
						 final int _type,
						 final byte[] _data )
		{
			offset = _offset ;
			length = _length ;
			type = _type ;
			data = _data ;
		}

		public int getEnd()
		{
			return offset + length ;
		}

		public String toString()
		{
			final StringBuilder builder = new StringBuilder() ;
			builder.append( "Offset: " ) ;
			builder.append( offset ) ;
			builder.append( " Length: " ) ;
			builder.append( length ) ;
			builder.append( " Type: " ) ;
			builder.append( type ) ;
			builder.append( " Data : " ) ;
			builder.append( data ) ;
			return builder.toString() ;
		}
	}

	public interface IReader
	{
		public int componentType() ;
		public String type() ;
		public int readBytes( final int _index, final byte[] _populate ) ;
	}

	public static class Reader implements IReader
	{
		private final JChunk.BufferView view ;
		private final JChunk.Accessor accessor ;

		private final byte[] data ;
		private final int numOfComponents ;
		private final int componentSize ;

		public Reader( final BinChunk _bin,
					   final JChunk.Accessor _accessor,
					   final JChunk.BufferView _view )
		{
			view = _view ;
			accessor = _accessor ;

			data = _bin.data ;
			numOfComponents = _accessor.numOfComponents() ;
			componentSize = _accessor.componentSizeInBytes() ;
		}

		@Override
		public int componentType()
		{
			return accessor.componentType ;
		}

		@Override
		public String type()
		{
			return accessor.type ;
		}

		@Override
		public int readBytes( final int _index, final byte[] _populate )
		{
			int rOffset = view.byteOffset + ( _index * componentSize * numOfComponents ) ;
			int wOffset = 0 ;
			for( int i = 0; i < numOfComponents; ++i )
			{
				for( int j = 0; j < componentSize; ++j )
				{
					_populate[wOffset + j] = data[rOffset + j] ;
				}

				ConvertBytes.flipEndian( _populate, wOffset, componentSize ) ;
				rOffset += componentSize ;
				wOffset += componentSize ;
			}
			return numOfComponents * componentSize ;
		}
	}

	private static class PointAccessor extends JChunk.Accessor
	{
		public PointAccessor()
		{
			super( 5126, -1, -1, "VEC3" ) ;
		}

		@Override
		public IReader createReader( JChunk _chunk, BinChunk _bin )
		{
			throw new RuntimeException( "Point Reader not implemented." ) ;
		}
	}

	private static class UVAccessor extends JChunk.Accessor
	{
		public UVAccessor()
		{
			super( 5126, -1, -1, "VEC2" ) ;
		}

		@Override
		public IReader createReader( JChunk _chunk, BinChunk _bin )
		{
			throw new RuntimeException( "UV Reader not implemented." ) ;
		}
	}

	public static class ColourAccessor extends JChunk.Accessor
	{
		public ColourAccessor()
		{
			super( 5120, -1, -1, "VEC4" ) ;
		}

		@Override
		public IReader createReader( JChunk _chunk, BinChunk _bin )
		{
			return new IReader()
			{
				@Override
				public int componentType()
				{
					return 5120 ;
				}

				@Override
				public String type()
				{
					return "VEC4" ;
				}

				@Override
				public int readBytes( final int _index, final byte[] _populate )
				{
					_populate[0] = ( byte )255 ;
					_populate[1] = ( byte )255 ;
					_populate[2] = ( byte )255 ;
					_populate[3] = ( byte )255 ;
					return 4 ;
				}
			} ;
		}
	}
}
