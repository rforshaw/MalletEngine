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
		Use the Swivel to define the vertex structure.
		In the realms of OpenGL this defines the attributes.
	*/
	public enum Swivel
	{
		POINT,		// Vector3
		COLOUR,		// MalletColour
		UV,			// Vector2
		NORMAL ;	// Vector3

		/**
			Return a basic vertex structure of two elements.
			Point and Colour.
		*/
		public static Swivel[] constructDefault()
		{
			final Swivel[] swivel = new Swivel[2] ;
			swivel[0] = Swivel.POINT ;
			swivel[1] = Swivel.COLOUR ;
			return swivel ;
		}

		public static Swivel[] constructSwivel( final Swivel ... _swivel )
		{
			return _swivel ;
		}

		public static Swivel[] getSwivelByArray( final List<String> _text )
		{
			final int size = _text.size() ;
			final Swivel[] swivel = new Swivel[size] ;

			for( int i = 0; i < size; i++ )
			{
				swivel[i] = getSwivelByString( _text.get( i ) ) ;
			}

			return swivel ;
		}

		public static Swivel getSwivelByString( final String _text )
		{
			switch( _text )
			{
				case "POINT"  : return POINT ;
				case "COLOUR" : return COLOUR ;
				case "UV"     : return UV ;
				case "NORMAL" : return NORMAL ;
				default       : return POINT ;
			}
		}

		/**
			A Vertex should always contain at least one POINT.
			If a POINT exists return its index location, else return -1.
		*/
		public static int getSwivelPointIndex( final Swivel[] _swivel )
		{
			for( int i = 0; i < _swivel.length; i++ )
			{
				if( _swivel[i] == Swivel.POINT )
				{
					return i ;
				}
			}

			return -1 ;
		}

		/**
			Return the amount of floats required to define the Vertex.
			POINT  = 3 floats
			COLOUR = 1 float
			UV     = 2 floats
			NORMAL = 3 floats

			A default swivel would return 4. 3 for POINT, and 1 for COLOUR.
		*/
		public static int getSwivelFloatSize( final Swivel[] _swivel, final int _length )
		{
			int size = 0 ;
			for( int i = 0; i < _length; i++ )
			{
				switch( _swivel[i] )
				{
					case POINT  : size += 3 ; break ;	// Vector3
					case COLOUR : size += 1 ; break ;	// MalletColour
					case UV     : size += 2 ; break ;	// Vector2
					case NORMAL : size += 3 ; break ;	// Vector3
				}
			}

			return size ;
		}

		/**
			Construct a vertex based on _swivel.
		*/
		public static Object[] createVert( final Swivel[] _swivel )
		{
			final Object[] obj = new Object[_swivel.length] ;
			for( int i = 0; i < _swivel.length; i++ )
			{
				switch( _swivel[i] )
				{
					case POINT  : obj[i] = new Vector3() ;      break ;
					case COLOUR : obj[i] = new MalletColour() ; break ;
					case UV     : obj[i] = new Vector2() ;      break ;
					case NORMAL : obj[i] = new Vector3() ;      break ;
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
	public Swivel[] getSwivel() ;

	/**
		Return the number of vertices stored.
	*/
	public int getVerticesSize() ;

	/**
		Return the number of indices stored.
	*/
	public int getIndicesSize() ;

	/**
		Add the index to the shape.
	*/
	public void addIndex( final int _index ) ;
	
	/**
		Added the indices to the shape in the order 
		they were passed in.
	*/
	public void addIndices( final int ... _index ) ;

	/**
		Copy the vertex passed into the shape.
		It does not retain the vertex object.
		This allows _vertex to be reused.
	*/
	public void copyVertex( final Object[] _vertex ) ;

	/**
		Copy the vertices passed into the shape.
		It does not retain the vertex objects, vertices
		are stored in the order they were passed.
		This allows _vertex to be reused.
	*/
	public void copyVertices( final Object[] ... _vertex ) ;

	/**
		Copy the vertex specified at _index location, into 
		the passed in Object array.
		The Object array must be of the correct size and 
		the correct composition for the operation to succeed.
		Return null if unable to copy, else return the Object array.
	*/
	public Object[] copyVertexTo( final int _index, final Object[] _to ) ;

	/**
		Copy the vertex specified at _index location, into 
		the passed in float array.
		The float array must be of the correct size and 
		the correct composition for the operation to succeed.
		Return null if unable to copy, else return the float array.
	*/
	public float[] copyVertexTo( final int _index, final float[] _to ) ;

	/**
		Return the raw indices stored by this shape.
	*/
	public int[] getRawIndices() ;
	
	/**
		Return the raw vertices stored by this shape.
	*/
	public float[] getRawVertices() ;
}
