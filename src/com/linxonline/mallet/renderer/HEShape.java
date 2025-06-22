package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.Iterator ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;
import com.linxonline.mallet.util.buffers.IntegerBuffer ;
import com.linxonline.mallet.util.Logger ;

/**
	Intended to be used for geometry that requires
	to be edited or manipulated in some way.
*/
public class HEShape implements IShape
{
	private final Attribute[] attributes ;
	private final int[] attributeOffsets ;
	private final int vertexFloatSize ;

	private final int positionIndex ;			// Used to determine which attribute represents the vertex-position.

	private final List<Vertex> vertices = MalletList.<Vertex>newList() ;
	private final List<Edge> edges = MalletList.<Edge>newList() ;
	private final List<Face> faces = MalletList.<Face>newList() ;

	public HEShape( final Attribute[] _attributes )
	{
		this( _attributes, 0 ) ;
	}

	public HEShape( final Attribute[] _attributes, final int _positionIndex )
	{
		attributes = new Attribute[_attributes.length] ;
		attributeOffsets = new int[_attributes.length] ;

		System.arraycopy( _attributes, 0, attributes, 0, _attributes.length ) ;
		for( int i = 0; i < _attributes.length; ++i )
		{
			attributeOffsets[i] = Attribute.getAttributeFloatSize( attributes, i ) ;
		}
		vertexFloatSize = Attribute.getAttributeFloatSize( attributes, attributes.length ) ;

		positionIndex = _positionIndex ;
	}

	public HEShape( final HEShape _shape )
	{
		attributes = new Attribute[_shape.attributes.length] ;
		attributeOffsets = new int[attributes.length] ;

		System.arraycopy( _shape.attributes, 0, attributes, 0, attributes.length ) ;
		System.arraycopy( _shape.attributeOffsets, 0, attributeOffsets, 0, attributeOffsets.length ) ;

		vertexFloatSize = _shape.vertexFloatSize ;
		positionIndex = _shape.positionIndex ;

		// Make a copy of all the vertices.
		for( final Vertex vertex : _shape.vertices )
		{
			addVertex( vertex.copy() ) ;
		}

		// Create new edges using the index
		// location of each vertex, these indexes
		// should be the same for the copy.
		for( final Edge edge : _shape.edges )
		{
			final Vertex origin = edge.getOrigin() ;
			final Vertex dest = edge.getDestination() ;
			addEdge( origin.getIndex(), dest.getIndex() ) ;
		}

		// Loop over the edges we have created and
		// find the edge that should be assigned to
		// 'next' if the edge had one.
		final int size = _shape.edges.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Edge edge = _shape.edges.get( i ) ;
			final Edge copy = edges.get( i ) ;

			copy.next = getMatchingEdge( _shape.edges, edge.next ) ;
			copy.pair.next = getMatchingEdge( _shape.edges, edge.pair.next ) ;
		}

		// We now move onto making the faces.
		// Loop over each face and create a copy
		// that uses the matching start edge.
		// We then loop over all the edges and
		// assign them to the newly created face.
		for( final Face face : _shape.faces )
		{
			final Edge start = getMatchingEdge( _shape.edges, face.getStart() ) ;
			final Face copy = new Face( start ) ;
			faces.add( copy ) ;

			Edge edge = start ;
			do
			{
				edge.face = copy ;
				edge = edge.next ;
			}
			while( edge != start ) ;
		}
	}

	/**
		Used when making a copy of an HEShape.
		The edges are duplicated and then the copy
		operation begin to remap the edges.next to point
		to the correct edge from the copies using the old
		edge index to find the copy.
	*/
	private Edge getMatchingEdge( final List<Edge> _edges, final Edge _edge )
	{
		if( _edge == null )
		{
			return null ;
		}

		final int size = _edges.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Edge edge = _edges.get( i ) ;
			if( edge == _edge )
			{
				return edges.get( i ) ;
			}
			else if( edge.getPair() == _edge )
			{
				return edges.get( i ).getPair() ;
			}
		}

		return null ;
	}
	
	public Vertex addVertex( final Object[] _data )
	{
		return addVertex( new Vertex( vertexFloatSize, attributes, _data ) ) ;
	}

	private Vertex addVertex( final Vertex _vert )
	{
		_vert.setIndex( vertices.size() ) ;
		vertices.add( _vert ) ;
		return _vert ;
	}

	public Vertex getVertex( final int _i )
	{
		return vertices.get( _i ) ;
	}

	public int getNumOfVertices()
	{
		return vertices.size() ;
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
		//edges.add( edge2 ) ;

		edge1.pair = edge2 ;
		edge2.pair = edge1 ;

		return edge1 ;
	}

	public Edge addEdge( final int _v1, final int _v2 )
	{
		return addEdge( getVertex( _v1 ), getVertex( _v2 ) ) ;
	}

	public Edge getEdge( final int _i )
	{
		return edges.get( _i ) ;
	}

	public int getNumOfEdges()
	{
		return edges.size() ;
	}

	/**
		Create a set of edges and assign it to the passed
		in face, if you don't pass in a face it will create
		a new face on your behalf.
		The edges are assumed to be in order of their intended
		assignment.
	*/
	public Face connect( Face _face, final Edge ... _edges )
	{
		if( _edges.length <= 2 )
		{
			Logger.println( "Too few edges to make face.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		final int size = _edges.length - 1 ;
		if( _edges[0].getOrigin() != _edges[size].getDestination() )
		{
			Logger.println( "Start edge and end edge do not finish on the same vertex.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		if( _face == null )
		{
			_face = new Face( _edges[0] ) ;
			faces.add( _face ) ;
		}

		for( int i = 0; i < size; ++i )
		{
			final Edge edge = _edges[i] ;
			edge.face = _face ;
			edge.next = _edges[i + 1] ;
		}
		_edges[size].next = _edges[0] ;

		return _face ;
	}

	public Face getFace( final int _i )
	{
		return faces.get( _i ) ;
	}

	public int getNumOfFaces()
	{
		return faces.size() ;
	}

	public Vertex getClosestVertex( final float _x, final float _y, final float _z )
	{
		final Vector3 a = new Vector3( _x, _y, _z ) ;

		Vertex closest = null ;
		float closestDistance = Float.MAX_VALUE ;

		for( final Vertex vertex : vertices )
		{
			final Vector3 b = vertex.getVector3( positionIndex, new Vector3() ) ;
			final float distance = Vector3.distance( a, b ) ;
			if( distance < closestDistance )
			{
				closest = vertex ;
				closestDistance = distance ;
			}
		}

		return closest ;
	}

	public Edge getClosestEdge( final float _x, final float _y, final float _z )
	{
		final Vector3 point = new Vector3() ;

		Edge closestEdge = null ;
		float closestDistance = Float.MAX_VALUE ;

		for( final Edge edge : edges )
		{
			point.setXYZ( _x, _y, _z ) ;
			final float distance = calcDistance( edge, point ) ;
			if( distance < closestDistance )
			{
				closestEdge = edge ;
				closestDistance = distance ;
			}
		}

		return closestEdge ;
	}

	private float calcDistance( final Edge _edge, final Vector3 _c )
	{
		final Vector3 a = _edge.getOrigin().getVector3( positionIndex, new Vector3() ) ;
		final Vector3 b = _edge.getDestination().getVector3( positionIndex, new Vector3() ) ;

		final Vector3 ab = Vector3.subtract( b, a ) ;
		final Vector3 ac = Vector3.subtract( _c, a ) ;
		final Vector3 bc = Vector3.subtract( _c, b ) ;

		float e = Vector3.dot( ac, ab ) ;
		if( e <= 0.0f )
		{
			return Vector3.dot( ac, ac ) ;
		}

		float f = Vector3.dot( ab, ab ) ;
		if( e >= f )
		{
			return Vector3.dot( bc, bc ) ;
		}

		return Vector3.dot( ac, ac ) - e * e / f ;
	}

	@Override
	public Style getStyle()
	{
		return Style.FILL ;
	}

	@Override
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
		int size = 0 ;

		for( final Face face : faces )
		{
			size += face.getIndicesCount() ;
		}

		return size ;
	}

	@Override
	public IShape.IIndexWrite writeIndices( final int _indexOffset, final IShape.IIndexWrite _write )
	{
		for( final Face face : faces )
		{
			switch( face.getEdgeCount() )
			{
				default :
				{
					face.triangulate( _indexOffset, _write ) ;
					break ;
				}
				case 3  :
				{
					final Edge e0 = face.getStart() ;
					final Edge e1 = e0.next ;
					final Edge e2 = e1.next ;

					final Vertex v0 = e0.getOrigin() ;
					final Vertex v1 = e1.getOrigin() ;
					final Vertex v2 = e2.getOrigin() ;

					_write.put( _indexOffset + v0.getIndex() ) ;
					_write.put( _indexOffset + v1.getIndex() ) ;
					_write.put( _indexOffset + v2.getIndex() ) ;
					break ;
				}
				/*case 4  :
				{
					if( offset + 6 >= temp.length )
					{
						temp = IntegerBuffer.expand( temp, temp.length * 2 ) ;
					}

					final Edge e0 = face.getStart() ;
					final Edge e1 = e0.next ;
					final Edge e2 = e1.next ;
					final Edge e3 = e2.next ;

					final Vertex v0 = e0.getOrigin() ;
					final Vertex v1 = e1.getOrigin() ;
					final Vertex v2 = e2.getOrigin() ;
					final Vertex v3 = e3.getOrigin() ;

					temp[offset++] = v0.getIndex() ;
					temp[offset++] = v1.getIndex() ;
					temp[offset++] = v2.getIndex() ;

					temp[offset++] = v0.getIndex() ;
					temp[offset++] = v2.getIndex() ;
					temp[offset++] = v3.getIndex() ;
					break ;
				}*/
			}
		}

		return _write ;
	}

	@Override
	public IShape.IVertWrite writeVertices( final IShape.IVertWrite _write )
	{
		for( final Vertex vert : vertices )
		{
			_write.put( vert.data ) ;
		}

		return _write ;
	}

	public final class Vertex
	{
		private final float[] data ;
		private int index ;
		// Store one edge that uses this vertex as its origin.
		// A vertex can have more than one edge that uses it as the origin,
		// however this is stored for convenience.
		private Edge edge ;

		private Vertex( final Vertex _vertex )
		{
			data = new float[_vertex.data.length] ;
			System.arraycopy( _vertex.data, 0, data, 0, data.length ) ;

			index = -1 ;
			edge = null ;
		}

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
						final Colour colour = ( Colour )_data[i] ;
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

		public void setColour( final int _attributeIndex, final Colour _colour )
		{
			final int start = attributeOffsets[_attributeIndex] ;
			FloatBuffer.set( data, start, _colour.toFloat() ) ;
		}

		public void setFloat( final int _attributeIndex, final float _val )
		{
			final int start = attributeOffsets[_attributeIndex] ;
			FloatBuffer.set( data, start, _val ) ;
		}

		public void setVector2( final int _attributeIndex, final float _x, final float _y )
		{
			final int start = attributeOffsets[_attributeIndex] ;
			FloatBuffer.set( data, start, _x, _y ) ;
		}

		public void translateVector2( final int _attributeIndex, final float _x, final float _y )
		{
			final int start = attributeOffsets[_attributeIndex] ;
			FloatBuffer.add( data, start, _x, _y ) ;
		}

		public void setVector3( final int _attributeIndex, final float _x, final float _y, final float _z )
		{
			final int start = attributeOffsets[_attributeIndex] ;
			FloatBuffer.set( data, start, _x, _y, _z ) ;
		}

		public void translateVector3( final int _attributeIndex, final float _x, final float _y, final float _z )
		{
			final int start = attributeOffsets[_attributeIndex] ;
			FloatBuffer.add( data, start, _x, _y, _z ) ;
		}

		/**
			Delete the vertex and remove any edges that
			have this vertex as their origin, this also
			removes the edge pair too.
		*/
		public void delete()
		{
			if( vertices.remove( this ) == false )
			{
				return ;
			}

			final Iterator<Edge> iter = edges.iterator() ;
			while( iter.hasNext() )
			{
				final Edge edge = iter.next() ;

				// If the edge's 'destination' is our vertex
				// then set 'next' to null.
				if( edge.next != null )
				{
					if( edge.next.getOrigin() == this )
					{
						edge.next = null ;
						continue ;
					}
				}

				// If our edge has a pair that has this
				// vertex as its origin then we remove it.
				// If the edge has our vertex as an origin
				// then we remove it.
				if( edge.getOrigin() == this ||
					edge.pair.getOrigin() == this )
				{
					if( edge.face != null )
					{
						edge.face.delete() ;
					}

					iter.remove() ;
					continue ;
				}
			}

			// Update the index specified on the
			// vertices - if we don't this will
			// create problems when triangulating. 
			final int size = vertices.size() ;
			for( int i = 0; i < size; ++i )
			{
				final Vertex vertex = vertices.get( i ) ;
				vertex.setIndex( i ) ;
			}

			index = -1 ;
			edge = null ;
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

		public Vector3 getVector3( final int _attributeIndex, final Vector3 _fill )
		{
			final int start = attributeOffsets[_attributeIndex] ;
			return FloatBuffer.fill( data, _fill, start ) ;
		}

		public Vector2 getVector2( final int _attributeIndex, final Vector2 _fill )
		{
			final int start = attributeOffsets[_attributeIndex] ;
			return FloatBuffer.fill( data, _fill, start ) ;
		}

		public Vertex copy()
		{
			return new Vertex( this ) ;
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

		public void delete()
		{
			if( edges.remove( this ) == false )
			{
				return ;
			}

			if( face != null )
			{
				face.delete() ;
			}

			pair.delete() ;
		}

		public Edge extrude()
		{
			final Vertex v0 = getOrigin() ;
			final Vertex v1 = getDestination() ;

			final Vertex v2 = addVertex( v1.copy() ) ;
			final Vertex v3 = addVertex( v0.copy() ) ;

			final Edge e0 = this ; // v0, v1
			final Edge e1 = addEdge( v1, v2 ) ;
			final Edge e3 = addEdge( v3, v0 ) ;
			final Edge e2 = addEdge( v2, v3 ) ;

			final Face f1 = connect( null, e0, e1, e2, e3 ) ;
			return e2 ;
		}

		public Edge insert( final float _ratio )
		{
			final Vertex v0 = getOrigin() ;
			final Vertex v1 = getDestination() ;

			final Vector3 start = v0.getVector3( positionIndex, new Vector3() ) ;
			final Vector3 end = v1.getVector3( positionIndex, new Vector3() ) ;

			final Vector3 diff = Vector3.subtract( end, start ) ;
			diff.multiply( _ratio ) ;

			return insert( start.x + diff.x, start.y + diff.y, start.z + diff.z ) ;
		}

		public Edge insert( final float _x, final float _y, final float _z )
		{
			final Vertex v0 = getOrigin() ;
			final Vertex v1 = getDestination() ;

			final Vertex middle = addVertex( v0.copy() ) ;
			middle.setVector3( positionIndex, _x, _y, _z ) ;

			final Edge edge = addEdge( middle, v1 ) ;
			edge.face = face ;
			edge.next = next ;
			next = edge ;

			edge.pair.next = pair ;
			pair.origin = middle ;

			return edge ;
		}
		
		public void translateVector2( final int _attributeIndex, final float _x, final float _y )
		{
			final Vertex v1 = getOrigin() ;
			v1.translateVector2( _attributeIndex, _x, _y ) ;

			final Vertex v2 = getDestination() ;
			v2.translateVector2( _attributeIndex, _x, _y ) ;
		}

		public void translateVector3( final int _attributeIndex, final float _x, final float _y, final float _z )
		{
			final Vertex v1 = getOrigin() ;
			v1.translateVector3( _attributeIndex, _x, _y, _z ) ;

			final Vertex v2 = getDestination() ;
			v2.translateVector3( _attributeIndex, _x, _y, _z ) ;
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

		public Vertex getDestination()
		{
			return pair.getOrigin() ;
		}

		/**
			An edge is considered on the boundary,
			if either it or its pair have no face assigned.
		*/
		public boolean isBoundary()
		{
			return face == null || pair.face == null ;
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
		private Edge start ;

		private Face( final Edge _start )
		{
			start = _start ;
		}

		public void delete()
		{
			if( faces.remove( this ) == false )
			{
				return ;
			}
		
			Edge edge = start ;
			do
			{
				// Loop over each edge within the
				// face and null out the 'next' field.

				// This effectively removes any trace
				// of the face. 

				final Edge temp = edge ;
				edge = edge.next ;
				temp.next = null ;
			}
			while( edge != start ) ;
		}
		
		public Edge getStart()
		{
			return start ;
		}

		public int getEdgeCount()
		{
			int count = 0 ;
			Edge edge = start ;
			do
			{
				++count ;
				edge = edge.next ;
			}
			while( edge != start ) ;

			return count ;
		}

		public int getVertexCount()
		{
			int count = 0 ;

			Edge edge = start ;
			do
			{
				++count ;
				edge = edge.next ;
			}
			while( edge != start ) ;

			return count ;
		}

		public int getTriangleCount()
		{
			final int count = getVertexCount() ;
			return count - 2 ;
		}

		public int getIndicesCount()
		{
			return getTriangleCount() * 3 ;
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

		public Vertex getClosestVertex( final float _x, final float _y, final float _z )
		{
			final Vector3 a = new Vector3( _x, _y, _z ) ;

			Vertex closest = null ;
			float closestDistance = Float.MAX_VALUE ;

			Edge edge = start ;
			do
			{
				final Vertex vertex = edge.getOrigin() ;

				final Vector3 b = vertex.getVector3( positionIndex, new Vector3() ) ;
				final float distance = Vector3.distance( a, b ) ;
				if( distance < closestDistance )
				{
					closest = vertex ;
					closestDistance = distance ;
				}

				edge = edge.next ;
			}
			while( edge != start ) ;

			return closest ;
		}

		public Edge getClosestEdge( final float _x, final float _y, final float _z )
		{
			final Vector3 point = new Vector3() ;

			Edge closestEdge = null ;
			float closestDistance = Float.MAX_VALUE ;

			Edge edge = start ;
			do
			{
				point.setXYZ( _x, _y, _z ) ;
				final float distance = calcDistance( edge, point ) ;
				if( distance < closestDistance )
				{
					closestEdge = edge ;
					closestDistance = distance ;
				}

				edge = edge.next ;
			}
			while( edge != start ) ;

			return closestEdge ;
		}

		public Vector3 calculateNormal( final Vector3 _fill )
		{
			_fill.setXYZ( 0.0f, 0.0f, 0.0f ) ;

			final Vector3 normal = new Vector3() ;
			final Vector3 a = new Vector3() ;
			final Vector3 b = new Vector3() ;

			Edge edge = start ;
			do
			{
				final Vertex start = edge.getOrigin() ;
				final Vertex end = edge.getDestination() ;

				start.getVector3( positionIndex, a ) ;
				end.getVector3( positionIndex, b ) ;

				Vector3.cross( a, b, normal ) ;
				_fill.add( normal ) ;

				edge = edge.next ;
			}
			while( edge != start ) ;

			return _fill ;
		}

		private IShape.IIndexWrite triangulate( final int _indexOffset, final IShape.IIndexWrite _write )
		{
			final List<Vertex> vertices = getOrderedVertices( MalletList.<Vertex>newList() ) ;
			final Vector3 faceNormal = calculateNormal( new Vector3() ) ;

			final Triangle ear = new Triangle() ;

			do
			{
				if( populateTriangle( faceNormal, vertices, ear ) == null )
				{
					// We failed to find a triangle!
					break ;
				}

				vertices.remove( ear.getTip() ) ;

				_write.put( _indexOffset + ear.getPrevIndex() ) ;
				_write.put( _indexOffset + ear.getCurrentIndex() ) ;
				_write.put( _indexOffset + ear.getNextIndex() ) ;
			}
			while( vertices.size() > 2 ) ;

			return _write ;
		}

		private Triangle populateTriangle( final Vector3 _faceNormal, final List<Vertex> _verts, final Triangle _triangle )
		{
			final int size = _verts.size() ;

			for( int i = 0; i < size; ++i )
			{
				final int prevIndex = ( i > 0 ) ? i - 1 : size - 1 ;
				final int nextIndex = ( ( i + 1 ) < size ) ? i + 1 : 0 ;

				final Vertex prev = _verts.get( prevIndex ) ;
				final Vertex current = _verts.get( i ) ;
				final Vertex next = _verts.get( nextIndex ) ;

				_triangle.set( prev, current, next, i ) ;
				if( size <= 3 )
				{
					// If there are only 3 verts then we
					// return the triangle that was made.
					return _triangle ;
				}

				// Determine if the triangle is convex and adheres
				// to the expected winding-order (should be CCW).
				if( _triangle.isConvex( _faceNormal ) == false )
				{
					continue ;
				}

				for( int j = 0; j < size; ++j )
				{
					if( j == prevIndex || j == i || j == nextIndex )
					{
						continue ;
					}

					if( _triangle.isWithin( _verts.get( j ) ) == true )
					{
						// Break to outer loop and find a valid triangle.
						break ;
					}

					return _triangle ;
				}
			}

			return null ;
		}

		private List<Vertex> getOrderedVertices( final List<Vertex> _vertices )
		{
			Edge edge = start ;
			do
			{
				_vertices.add( edge.getOrigin() ) ;
				edge = edge.next ;
			}
			while( edge != start ) ;

			return _vertices ;
		}
	}

	private final class Triangle
	{
		private final Vector3 a = new Vector3() ;
		private final Vector3 b = new Vector3() ;
		private final Vector3 c = new Vector3() ;

		private final Vector3 p = new Vector3() ;

		private Vertex prev ;
		private Vertex current ;
		private Vertex next ;

		private int earTip = -1 ;

		public Triangle() {}

		public void set( final Vertex _p, final Vertex _c, final Vertex _n, final int _tip )
		{
			prev = _p ;
			current = _c ;
			next = _n ;

			earTip = _tip ;
		}

		public boolean isConvex( final Vector3 _normal )
		{
			prev.getVector3( positionIndex, a ) ;
			current.getVector3( positionIndex, b ) ;
			next.getVector3( positionIndex, c ) ;

			a.subtract( b ) ;
			c.subtract( b ) ;

			Vector3.cross( c, a, p ) ;
			//System.out.println( "T: " + p.toString() + " F: " + _normal.toString() ) ;

			return ( signum( p.x ) == signum( _normal.x ) &&
					 signum( p.y ) == signum( _normal.y ) &&
					 signum( p.z ) == signum( _normal.z ) ) ;
		}

		private int signum( final float _val )
		{
			return ( int )Math.signum( _val ) ;
		}

		public boolean isWithin( final Vertex _vertex )
		{
			prev.getVector3( positionIndex, a ) ;
			current.getVector3( positionIndex, b ) ;
			next.getVector3( positionIndex, c ) ;

			_vertex.getVector3( positionIndex, p ) ;

			a.subtract( p ) ;
			b.subtract( p ) ;
			c.subtract( p ) ;

			final float ab = Vector3.dot( a, b ) ;
			final float ac = Vector3.dot( a, c ) ;
			final float bc = Vector3.dot( b, c ) ;
			final float cc = Vector3.dot( c, c ) ;

			if( bc * ac - cc * ab < 0.0f )
			{
				return false ;
			}

			final float bb = Vector3.dot( b, b ) ;
			if( ab * bc - ac * bb < 0.0f )
			{
				return false ;
			}

			return true ;
		}

		public int getPrevIndex()
		{
			return prev.getIndex() ;
		}

		public int getCurrentIndex()
		{
			return current.getIndex() ;
		}

		public int getNextIndex()
		{
			return next.getIndex() ;
		}

		public int getTip()
		{
			return earTip ;
		}
	}
}
