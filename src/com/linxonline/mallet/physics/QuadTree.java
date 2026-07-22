package com.linxonline.mallet.physics ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.Parallel ;

import com.linxonline.mallet.maths.AABB ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Intersection ;

public final class QuadTree
{
	private static final Hull[] EMPTY_HULLS = new Hull[0] ;

	private final ArrayList<QuadNode> children = new ArrayList<QuadNode>() ;
	private final NodeWorker worker = new NodeWorker() ;

	private final Parallel.IListRun<Hull> insertionWorker ;

	private final List<QuadNode> nodes = new ArrayList<QuadNode>() ;
	private final ArrayList<Hull> failed = new ArrayList<Hull>() ;

	private final AABB aabb = AABB.create() ;

	private final int MAX_HULLS ; 
	private final float NODE_AREA_LIMIT  ;

	private float ROOT_LENGTH ;
	private int lastFailedSize = 0 ;

	private enum Quadrant
	{
		ROOT,
		TOP_LEFT,
		TOP_RIGHT,
		BOTTOM_LEFT,
		BOTTOM_RIGHT
	}

	private final QuadNode root ;

	public QuadTree()
	{
		this( 0.0f, 0.0f, 2000.0f, 128.0f, 20 ) ;
	}

	public QuadTree( final float _x,
					 final float _y,
					 final float _size,
					 final float _nodeAreaLimit,
					 final int _nodeCapacity )
	{
		root = new QuadNode( _x, _y, _size, Quadrant.ROOT ) ;
		NODE_AREA_LIMIT = _nodeAreaLimit ;

		ROOT_LENGTH = root.length ;
		MAX_HULLS = _nodeCapacity ;

		insertionWorker = ( final int _start, final int _end, final List<Hull> _h ) ->
		{
			final List<Hull> fails = new ArrayList<Hull>() ;
			final List<QuadNode> nodes = new ArrayList<QuadNode>() ;
			final AABB aabb = AABB.create() ;

			for( int i = _start; i < _end; ++i )
			{
				final Hull hull = _h.get( i ) ;
				hull.getAABB( aabb ) ;

				root.getQuadNodes( aabb, nodes ) ;
				if( nodes.isEmpty() )
				{
					fails.add( hull ) ;
					break ;
				}

				for( final QuadNode node : nodes )
				{
					if( !node.addSync( hull ) )
					{
						fails.add( hull ) ;
						break ;
					}
				}

				nodes.clear() ;
			}

			synchronized( failed )
			{
				failed.addAll( fails ) ;
			}
		} ;
	}

	public void insertHulls( final List<Hull> _hulls )
	{
		Parallel.forEach( children, 100, ( final int _index, final QuadNode _node ) ->
		{
			_node.clear() ;
		} ) ;

		Parallel.forBatch( _hulls, 500, insertionWorker ) ;
		if( failed.isEmpty() )
		{
			return ;
		}

		final int size = failed.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Hull hull = failed.get( i ) ;
			hull.getAABB( aabb ) ;

			while( needsExpansion( aabb ) )
			{
				root.expand() ;
			}

			root.getQuadNodes( aabb, nodes ) ;
			if( nodes.isEmpty() )
			{
				continue ;
			}

			for( final QuadNode node : nodes )
			{
				node.add( hull ) ;
			}
			nodes.clear() ;
		}

		if( size < lastFailedSize )
		{
			failed.trimToSize() ;
		}

		lastFailedSize = size ;
		failed.clear() ;
	}

	/**
		Generate contact points on the hull that has been 
		passed in.
		The hull will not be added to the Quad Tree but it 
		will be compared to hulls that have been inserted. 
	*/
	public void generateContacts( final Hull _hull, final ContactData _contacts )
	{
		final List<QuadNode> nodes = new ArrayList<QuadNode>() ;
		final AABB aabb = AABB.create() ;

		_hull.getAABB( aabb ) ;

		root.getQuadNodes( aabb, nodes ) ;

		for( final QuadNode node : nodes )
		{
			node.generateContacts( _hull, _contacts ) ;
		}
	}

	/**
		Return the first hull the ray collides with.
	*/
	public Hull ray( final Ray _ray, final int _f )
	{
		return root.ray( _ray, _f ) ;
	}

	public void update( final float _dt, final List<ContactData> _contacts )
	{
		if( children.isEmpty() )
		{
			// There should always be at least 4
			// child nodes provided by the root.
			// If we have no children then we need
			// to go get them.
			root.getChildNodes( children ) ;
		}

		worker.set( _dt ) ;

		Parallel.forEach( children, 100, worker ) ;

		final int size = children.size() ;
		for( int i = 0; i < size; ++i )
		{
			final QuadNode child = children.get( i ) ;
			child.getContactData( _contacts ) ;
			child.stop() ;
		}
	}

	/**
		If a QuadNode expands, or creates child nodes
		then empty the child array.
	*/
	private void emptyChildren()
	{
		children.clear() ;
	}

	private boolean needsExpansion( final AABB _aabb )
	{
		return !root.aabb.intersectAABB( _aabb ) ;
	}

	protected final class QuadNode
	{
		private final float x ;
		private final float y ;

		private CollisionCheck check ;
		private Hull[] hulls = EMPTY_HULLS ;

		private volatile QuadNode topLeft ;
		private volatile QuadNode topRight ;
		private volatile QuadNode bottomLeft ;
		private volatile QuadNode bottomRight ;

		private AABB aabb ;
		private float length ;
		private int nextHull = 0 ;
		private boolean parent = false ;

		public QuadNode( final float _x, final float _y, final float _length, final Quadrant _quadrant )
		{
			x = _x ;
			y = _y ;
			length = _length ;

			aabb = AABB.create( x, y, length ) ;

			if( _quadrant == Quadrant.ROOT )
			{
				// Initially the root node was considered a leaf.
				// Hulls added to it were directly added to hulls.
				// When you reach node capacity children were created 
				// and the hulls reinserted - if the hulls were outside 
				// of the roots boundaries problems arise.
				createChildren() ;
			}
		}

		public ContactData generateContacts( final Hull _hull, final ContactData _contacts )
		{
			if( parent == true )
			{
				return _contacts ;
			}

			// nextHull is the current length of hulls 
			// we want the hull we've passed in to be compared 
			// against all hulls within this node.
			check.setBaseHull( _hull ) ;
			for( int i = 0; i < nextHull; ++i )
			{
				check.generateContactPoint( hulls[i], _contacts ) ;
			}

			return _contacts ;
		}

		public List<ContactData> getContactData( final List<ContactData> _fill )
		{
			if( parent == true )
			{
				return _fill ;
			}

			final ContactData contacts = check.getContactData() ;
			if( contacts.size() > 0 )
			{
				_fill.add( contacts ) ;
			}

			return _fill ;
		}

		public Hull ray( final Ray _ray, final int _filter )
		{
			if( !aabb.ray( _ray.getPoint(), _ray.getDirection(), _ray.getIntersection() ).isValid() )
			{
				return null ;
			}

			final Intersection inter = _ray.getIntersection() ;

			Hull best = null ;
			float distance = Float.MAX_VALUE ;

			if( parent == true )
			{
				Hull hull = topLeft.ray( _ray, _filter ) ;
				if( inter.isValid() && inter.getDistance() < distance )
				{
					best = hull ;
					distance = inter.getDistance() ;
				}

				hull = topRight.ray( _ray, _filter ) ;
				if( inter.isValid() && inter.getDistance() < distance )
				{
					best = hull ;
					distance = inter.getDistance() ;
				}

				hull = bottomLeft.ray( _ray, _filter ) ;
				if( inter.isValid() && inter.getDistance() < distance )
				{
					best = hull ;
					distance = inter.getDistance() ;
				}

				hull = bottomRight.ray( _ray, _filter ) ;
				if( inter.isValid() && inter.getDistance() < distance )
				{
					best = hull ;
					distance = inter.getDistance() ;
				}

				inter.setDistance( distance ) ;
				return best ;
			}

			for( int i = 0; i < nextHull; ++i )
			{
				final Hull hull = hulls[i] ;
				if( Hull.isCollidableWithGroup( hull.getGroupID(), _filter ) == false )
				{
					// The client is not interested if this hull intersects 
					// the ray being cast.
					continue ;
				}

				// Loop over the available hulls and using the aabb
				// return the hull closest to the casting-point.
				if( hull.ray( _ray ) )
				{
					final Intersection intersection = _ray.getIntersection() ;
					final float dist = intersection.getDistance() ;
					if( dist < distance )
					{
						distance = dist ;
						best = hull ;
					}
				}
			}

			inter.setDistance( distance ) ;
			return best ;
		}

		public void getQuadNodes( final AABB _aabb, final List<QuadNode> _toAdd )
		{
			if( !aabb.intersectAABB( _aabb ) )
			{
				return ;
			}

			if( parent )
			{
				topLeft.getQuadNodes( _aabb, _toAdd ) ;
				topRight.getQuadNodes( _aabb, _toAdd ) ;
				bottomLeft.getQuadNodes( _aabb, _toAdd ) ;
				bottomRight.getQuadNodes( _aabb, _toAdd ) ;
				return ;
			}

			if( !_toAdd.contains( this ) )
			{
				_toAdd.add( this ) ;
			}
		}

		public boolean addSync( final Hull _hull )
		{
			if( parent )
			{
				return false ;
			}

			synchronized( this )
			{
				return add( _hull ) ;
			}
		}

		public boolean add( final Hull _hull )
		{
			if( parent )
			{
				return false ;
			}

			if( hulls.length == 0 )
			{
				hulls = new Hull[MAX_HULLS] ;
				check = new CollisionCheck() ;
			}

			if( nextHull >= hulls.length )
			{
				//System.out.println( "Not enough space: " + hulls.length ) ;
				if( createChildren() )
				{
					return false ;
				}

				// If we can't create child nodes we
				// must expand our capacity.
				expandHullCapacity() ;
			}

			hulls[nextHull++] = _hull ;
			return true ;
		}

		/**
			It reaches a point in which splitting 
			a node into quadrants will not solved 
			the capacity problem.
			When this happens we have to expand 
			the nodes hull array.
		*/
		private void expandHullCapacity()
		{
			final Hull[] newHulls = new Hull[hulls.length * 2] ;
			System.arraycopy( hulls, 0, newHulls, 0, hulls.length ) ;
			hulls = newHulls ;
		}

		/**
			Should only be called from the root node.
			Returns all current leaf/child nodes that
			contain hulls.
		*/
		public void getChildNodes( final List<QuadNode> _nodes )
		{
			if( parent == true )
			{
				topLeft.getChildNodes( _nodes ) ;
				topRight.getChildNodes( _nodes ) ;
				bottomLeft.getChildNodes( _nodes ) ;
				bottomRight.getChildNodes( _nodes ) ;
			}
			else
			{
				if( nextHull > 0 )
				{
					_nodes.add( this ) ;
				}
			}
		}

		public void update( final float _dt )
		{
			if( parent )
			{
				// We grab out child nodes upfront, so
				// we know when update() is called, it will
				// be on a child.
				return ;
			}

			check.reset() ;

			for( int i = 0; i < nextHull; ++i )
			{
				final Hull hull1 = hulls[i] ;
				if( hull1.isCollidable() == false )
				{
					continue ;
				}

				final boolean changed1 = hull1.hasChanged() ;

				check.setBaseHull( hull1 ) ;
				for( int j = i + 1; j < nextHull; ++j )
				{
					final Hull hull2 = hulls[j] ;
					if( !changed1 && !hull2.hasChanged() )
					{
						continue ;
					}

					check.generateContactPoint( hull2 ) ;
				}
			}
		}

		public void stop()
		{
			if( parent )
			{
				return ;
			}

			for( int i = 0; i < nextHull; ++i )
			{
				final Hull hull = hulls[i] ;
				hull.changed( false ) ;
			}
		}

		public void clear()
		{
			if( parent )
			{
				return ;
			}

			if( hulls.length > MAX_HULLS )
			{
				if( nextHull < MAX_HULLS )
				{
					final Hull[] newHulls = new Hull[MAX_HULLS] ;
					System.arraycopy( hulls, 0, newHulls, 0, nextHull ) ;
					hulls = newHulls ;
				}
			}

			for( int i = 0; i < nextHull; ++i )
			{
				hulls[i] = null ;
			}

			nextHull = 0 ;
		}

		private boolean createChildren()
		{
			return createTier( length / 2.0f ) ;
		}

		private boolean createTier( final float _offset )
		{
			if( _offset < NODE_AREA_LIMIT || parent )
			{
				// At a certain point making the Quad Tree more accurate
				// becomes futile. A node's scope that is too small 
				// will result in more comparisons rather than less.
				return false ;
			}

			topLeft = new QuadNode( x - _offset, y + _offset, _offset, Quadrant.TOP_LEFT ) ;
			topRight = new QuadNode( x + _offset, y + _offset, _offset, Quadrant.TOP_RIGHT ) ;
			bottomLeft = new QuadNode( x - _offset, y - _offset, _offset, Quadrant.BOTTOM_LEFT ) ;
			bottomRight = new QuadNode( x + _offset, y - _offset, _offset, Quadrant.BOTTOM_RIGHT ) ;

			parent = true ;
			nextHull = 0 ;
			check = null ;
			hulls = EMPTY_HULLS ;

			QuadTree.this.emptyChildren() ;

			return true ;
		}

		/**
			Expand the scope of the Quad Tree by doubling 
			its initial spacial size.
			Used when a hull's position is out of the 
			Quad Tree's boundaries.
		*/
		private void expand()
		{
			ROOT_LENGTH += ROOT_LENGTH ;

			final QuadNode tempRoot = new QuadNode( x, y, ROOT_LENGTH, Quadrant.ROOT ) ;
			tempRoot.createChildren() ;

			tempRoot.topLeft.createChildren() ;
			tempRoot.topLeft.bottomRight = topLeft ;

			tempRoot.topRight.createChildren() ;
			tempRoot.topRight.bottomLeft = topRight ;

			tempRoot.bottomLeft.createChildren() ;
			tempRoot.bottomLeft.topRight = bottomLeft ;

			tempRoot.bottomRight.createChildren() ;
			tempRoot.bottomRight.topLeft = bottomRight ;

			topLeft = tempRoot.topLeft ;
			topRight = tempRoot.topRight ;
			bottomLeft = tempRoot.bottomLeft ;
			bottomRight = tempRoot.bottomRight ;

			length = tempRoot.length ;
			aabb = AABB.create( x, y, length ) ;

			QuadTree.this.emptyChildren() ;
		}

		public int size()
		{
			return nextHull ;
		}

		protected Quadrant findQuadrant( final Vector2 _point )
		{
			if( _point.x >= x )
			{
				return ( _point.y >= y ) ? Quadrant.TOP_RIGHT : Quadrant.BOTTOM_RIGHT ;
			}
			else
			{
				return ( _point.y >= y ) ? Quadrant.TOP_LEFT : Quadrant.BOTTOM_LEFT ;
			}
		}

		protected Quadrant findQuadrant( final Vector3 _point )
		{
			if( _point.x >= x )
			{
				return ( _point.y >= y ) ? Quadrant.TOP_RIGHT : Quadrant.BOTTOM_RIGHT ;
			}
			else
			{
				return ( _point.y >= y ) ? Quadrant.TOP_LEFT : Quadrant.BOTTOM_LEFT ;
			}
		}
	}

	private final static class NodeWorker implements Parallel.IRangeRun<QuadNode>
	{
		private float deltaTime = 0.0f ;

		public NodeWorker() {}

		public void set( final float _dt )
		{
			deltaTime = _dt ;
		}

		@Override
		public void run( final int _index, final QuadNode _node )
		{
			_node.update( deltaTime ) ;
		}
	}
}
