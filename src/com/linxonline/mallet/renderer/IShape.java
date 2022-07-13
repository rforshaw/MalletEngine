package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Vector2 ;

public interface IShape
{
	public enum Style
	{
		LINES,				// Requires a start and an end point to be defined for each line
		LINE_STRIP, 		// Will continue the line from the last point added
		FILL ; 				// Fill the geometry shape, requires the shape to be defined in polygons, will eventually be auto generated.

		public static Style getStyleByString( final String _text )
		{
			switch( _text )
			{
				case "LINES"      : return LINES ;
				case "LINE_STRIP" : return LINE_STRIP ;
				case "FILL"       : return FILL ;
				default           : return LINES ;
			}
		}
	}

	/**
		Use the Attribute to define the vertex structure.
		In the realms of OpenGL this defines the attributes.
	*/
	public enum Attribute
	{
		VEC3,		// Vector3
		FLOAT,		// MalletColour
		VEC2 ;		// Vector2

		/**
			Return a basic vertex structure of two elements.
			Point and Colour.
		*/
		public static Attribute[] constructDefault()
		{
			final Attribute[] swivel = new Attribute[2] ;
			swivel[0] = Attribute.VEC3 ;
			swivel[1] = Attribute.FLOAT ;
			return swivel ;
		}

		public static Attribute[] constructAttribute( final Attribute ... _swivel )
		{
			return _swivel ;
		}

		public static Attribute[] getAttributeByArray( final List<String> _text )
		{
			final int size = _text.size() ;
			final Attribute[] swivel = new Attribute[size] ;

			for( int i = 0; i < size; i++ )
			{
				swivel[i] = getAttributeByString( _text.get( i ) ) ;
			}

			return swivel ;
		}

		public static Attribute getAttributeByString( final String _text )
		{
			switch( _text )
			{
				case "VEC3"  : return VEC3 ;
				case "FLOAT" : return FLOAT ;
				case "VEC2"  : return VEC2 ;
				default      : return VEC3 ;
			}
		}

		/**
			A Vertex should always contain at least one VEC3.
			If a VEC3 exists return its index location, else return -1.
		*/
		public static int getAttributePointIndex( final Attribute[] _swivel )
		{
			for( int i = 0; i < _swivel.length; i++ )
			{
				if( _swivel[i] == Attribute.VEC3 )
				{
					return i ;
				}
			}

			return -1 ;
		}

		/**
			Return the amount of floats required to define the Vertex.
			VEC3  = 3 floats
			FLOAT = 1 float
			VEC2  = 2 floats

			A default swivel would return 4. 3 for VEC3, and 1 for FLOAT.
		*/
		public static int getAttributeFloatSize( final Attribute[] _swivel, final int _length )
		{
			int size = 0 ;
			for( int i = 0; i < _length; i++ )
			{
				switch( _swivel[i] )
				{
					case VEC3  : size += 3 ; break ;	// Vector3
					case FLOAT : size += 1 ; break ;	// MalletColour
					case VEC2  : size += 2 ; break ;	// Vector2
				}
			}

			return size ;
		}

		/**
			Construct a vertex based on _swivel.
		*/
		public static Object[] createVert( final Attribute[] _swivel )
		{
			final Object[] obj = new Object[_swivel.length] ;
			for( int i = 0; i < _swivel.length; i++ )
			{
				switch( _swivel[i] )
				{
					case VEC3  : obj[i] = new Vector3() ;      break ;
					case FLOAT : obj[i] = new MalletColour() ; break ;
					case VEC2  : obj[i] = new Vector2() ;      break ;
				}
			}

			return obj ;
		}

		public static Object[] createVert( final Object ... _objects )
		{
			return _objects ;
		}
	}

	public Style getStyle() ;
	public Attribute[] getAttribute() ;

	/**
		Return the number of vertices stored.
	*/
	public int getVerticesSize() ;

	/**
		Return the number of indices stored.
	*/
	public int getIndicesSize() ;

	/**
		Return the raw indices stored by this shape.
	*/
	public int[] getRawIndices() ;
	
	/**
		Return the raw vertices stored by this shape.
	*/
	public float[] getRawVertices() ;
}
