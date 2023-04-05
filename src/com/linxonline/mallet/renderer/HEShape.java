package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;
import com.linxonline.mallet.util.buffers.IntegerBuffer ;

/**
	Intended to be used for geometry that requires
	to be edited or manipulated in some way.
*/
public class HEShape implements IShape
{
	private final Attribute[] attributes ;
	private final int[] attributeOffsets ;
	private final int vertexFloatSize ;

	private Style style = Style.LINE_STRIP ;

	private final List<Vertex> vertices = MalletList.<Vertex>newList() ;
	private final List<Edge> edges = MalletList.<Edge>newList() ;
	private final List<Face> faces = MalletList.<Face>newList() ;

	public HEShape( final Style _style, final Attribute[] _attributes )
	{
		attributes = new Attribute[_attributes.length] ;
		attributeOffsets = new int[_attributes.length] ;

		System.arraycopy( _attributes, 0, attributes, 0, _attributes.length ) ;
		for( int i = 0; i < _attributes.length; ++i )
		{
			attributeOffsets[i] = Attribute.getAttributeFloatSize( attributes, i ) ;
		}
		vertexFloatSize = Attribute.getAttributeFloatSize( attributes, attributes.length ) ;

		style = _style ;
	}

	public Vertex addVertex( final Object[] _data )
	{
		final Vertex vertex = new Vertex( vertexFloatSize, attributes, _data ) ;
		vertex.setIndex( vertices.size() ) ;

		vertices.add( vertex ) ;
		return vertex ;
	}

	/**
		Create an edge based on the two passed in vertices.
		Note: These vertices should be part of one shape instance,
		do not share vertices create for one shape with another.
	*/
	public Edge addEdge( final Vertex _v1, final Vertex _v2 )
	{
		final Edge edge1 = new Edge( _v1 ) ;
		edges.add( edge1 ) ;

		final Edge edge2 = new Edge( _v2 ) ;
		edges.add( edge2 ) ;

		edge1.pair = edge2 ;
		edge2.pair = edge1 ;

		return edge1 ;
	}

	/**
		Create a set of edges and assing it to the passed
		in face, if you don't pass in a face it will create
		a new face on your behalf.
		The edges are assumed to be in order of their intended
		assignment.
	*/
	public Face connect( Face _face, final Edge ... _edges )
	{
		if( _face == null )
		{
			_face = new Face( _edges[0] ) ;
			faces.add( _face ) ;
		}

		final int size = _edges.length - 1 ;
		for( int i = 0; i < size; ++i )
		{
			final Edge edge = _edges[i] ;
			edge.face = _face ;
			edge.next = _edges[i + 1] ;
		}
		_edges[size].next = _edges[0] ;

		return _face ;
	}

	public Style getStyle()
	{
		return style ;
	}

	public Attribute[] getAttribute()
	{
		return attributes ;
	}

	@Override
	public int getVerticesSize()
	{
		return vertices.size() ;
	}

	@Override
	public int getIndicesSize()
	{
		switch( style )
		{
			default         :
			case LINES      :
			case LINE_STRIP :
			case FILL       : return 0 ;
		}
	}

	@Override
	public int[] getRawIndices()
	{
		switch( style )
		{
			default         :
			case LINES      :
			case LINE_STRIP :
			case FILL       : return triangulate() ;
		}
	}

	private int[] triangulate()
	{
		// Each face is assumed to be 3 points.
		final int[] indices = new int[faces.size() * 3] ;
		int increment = 0 ;

		for( final Face face : faces )
		{
			Edge start = face.getStart() ;
			Edge edge = start ;
			do
			{
				final Vertex vertex = edge.getOrigin() ;
				indices[increment++] = vertex.getIndex() ;
				edge = edge.next ;
			}
			while( edge != start ) ;
		}

		return indices ;
	}

	@Override
	public float[] getRawVertices()
	{
		final int size = vertices.size() ;
		final float[] to = new float[size * vertexFloatSize] ;

		int offset = 0 ;
		for( final Vertex vert : vertices )
		{
			final float[] from = vert.data ; 
			System.arraycopy( from, 0, to, offset, from.length ) ;
			offset += vertexFloatSize ;
		}

		return to ;
	}

	public final class Vertex
	{
		private final float[] data ;
		private int index ;
		// Store one edge that uses this vertex as its origin.
		// A vertex can have more than one edge that uses it as the origin,
		// however this is stored for convenience.
		private Edge edge ;

		private Vertex( final int _vertexSize, final Attribute[] _attributes, final Object[] _data )
		{
			data = new float[_vertexSize] ;

			int increment = 0 ;
			for( int i = 0; i < _attributes.length; i++ )
			{
				switch( _attributes[i] )
				{
					case VEC3  :
					{
						final Vector3 point = ( Vector3 )_data[i] ;
						FloatBuffer.set( data, increment, point ) ;
						increment += 3 ;
						break ;
					}
					case FLOAT :
					{
						final MalletColour colour = ( MalletColour )_data[i] ;
						data[increment++] = colour.toFloat() ;
						break ;
					}
					case VEC2     :
					{
						final Vector2 uv = ( Vector2 )_data[i] ;
						FloatBuffer.set( data, increment, uv ) ;
						increment += 2 ;
						break ;
					}
				}
			}
		}

		private void setIndex( final int _index )
		{
			index = _index ;
		}

		public int getIndex()
		{
			return index ;
		}

		public Edge getEdge()
		{
			return edge ;
		}
	}

	/**
		An edge represents one side of two vertex points.
		This is where half-edge gets its name, the edge stored
		in the pair represents the opposing side.
	*/
	public final class Edge
	{
		private Vertex origin ;		// Starting point
		private Edge pair ;			// Opposing edge
		private Edge next ;			// Next edge in loop
		private Face face ;			// The face the edge is associated with.

		private Edge( final Vertex _origin )
		{
			origin = _origin ;
			origin.edge = this ;
		}

		/**
			Return the starting vertex of this edge.
			The edge origin stored in 'next' will represent
			the edge's end vertex.
		*/
		public Vertex getOrigin()
		{
			return origin ;
		}

		/**
			Return the opposing edge.
		*/
		public Edge getPair()
		{
			return pair ;
		}

		/**
			Return the next edge connected to
			this edge.
		*/
		public Edge getNext()
		{
			return next ;
		}

		/**
			The same edge should only appear on a
			per-face basis.
		*/
		public Face getFace()
		{
			return face ;
		}
	}

	public final class Face
	{
		private final Edge start ;

		private Face( final Edge _start )
		{
			start = _start ;
		}

		public Edge getStart()
		{
			return start ;
		}

		/**
			Return the edge that has the passed in vertex.
		*/
		public Edge getEdge( final Vertex _vertex )
		{
			Edge edge = start ;
			do
			{
				if( edge.origin == _vertex )
				{
					return edge ;
				}

				edge = edge.next ;
			}
			while( edge != start ) ;

			return null ;
		}
	}
}
