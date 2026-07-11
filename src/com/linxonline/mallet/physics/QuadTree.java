package com.linxonline.mallet.physics ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.Parallel ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Intersection ;

public final class QuadTree
{
	private static final Hull[] EMPTY_HULLS = new Hull[0] ;

	private static int NEXT_ID = 0 ;

	private final List<QuadNode> nodes = new ArrayList<QuadNode>() ;
	private final ArrayList<Hull> failed = new ArrayList<Hull>() ;

	private final Vector2 absolute = new Vector2() ;
	private final IUpdate update ;

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

		update = new IUpdate()
		{
			// Used when multi-threading
			private final ArrayList<QuadNode> children = new ArrayList<QuadNode>() ;
			private final NodeWorker worker = new NodeWorker() ;

			@Override
			public void update( final float _dt, final List<ContactData> _contacts )
			{
				root.getChildNodes( children ) ;
				if( children.isEmpty() )
				{
					return ;
				}

				//System.out.println( "Nodes: " + children.size() ) ;
				worker.set( _dt ) ;

				Parallel.forEach( children, 10, worker ) ;

				final int size = children.size() ;
				for( int i = 0; i < size; ++i )
				{
					final QuadNode child = children.get( i ) ;
					//System.out.println( "Hulls: " + child.size() ) ;
					child.getContactData( _contacts ) ;
				}

				children.clear() ;
			}
		} ;
	}

	public void insertHulls( final List<Hull> _hulls )
	{
		Parallel.forBatch( _hulls, 2000, ( final int _start, final int _end, final List<Hull> _h ) ->
		{
			final List<QuadNode> nodes = new ArrayList<QuadNode>() ;
			final Vector2 absolute = new Vector2() ;

			for( int i = _start; i < _end; ++i )
			{
				final Hull hull = _h.get( i ) ;

				final int length = hull.getPointsLength() ;
				for( int j = 0; j < length; ++j )
				{
					hull.getPoint( j, absolute ) ;

					final float x = absolute.x ;
					final float y = absolute.y ;

					hull.getPosition( absolute ) ;
					absolute.add( x, y ) ;

					if( needsExpansion( absolute ) )
					{
						synchronized( failed )
						{
							failed.add( hull ) ;
						}
						continue ;
					}

					root.getQuadNodes(absolute, nodes) ;
				}

				//System.out.println( "Fast Inserted into: " + nodes.size() ) ;
				for( final QuadNode node : nodes )
				{
					if( !node.addSync( hull ) )
					{
						synchronized( failed )
						{
							failed.add( hull ) ;
							break ;
						}
					}
				}

				nodes.clear() ;
			}
		} ) ;

		final int size = failed.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Hull hull = failed.get( i ) ;

			final int length = hull.getPointsLength() ;
			for( int j = 0; j < length; ++j )
			{
				hull.getPoint( j, absolute ) ;

				final float x = absolute.x ;
				final float y = absolute.y ;

				hull.getPosition( absolute ) ;
				absolute.add( x, y ) ;

				while( needsExpansion( absolute ) )
				{
					root.expand() ;
				}

				root.getQuadNodes( absolute, nodes ) ;
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
		root.generateContacts( _hull, _contacts ) ;
	}

	public Hull ray( final Ray _ray, final int _f )
	{
		return root.ray( _ray, _f ) ;
	}

	public void update( final float _dt, final List<ContactData> _contacts )
	{
		update.update( _dt, _contacts ) ;
	}

	public void clear()
	{
		root.clear() ;
	}

	private boolean needsExpansion( final Vector2 _pos )
	{
		return Math.abs( _pos.x ) > ROOT_LENGTH || 
				Math.abs( _pos.y ) > ROOT_LENGTH ;
	}

	protected final class QuadNode
	{
		private final float x ;
		private final float y ;

		private CollisionCheck check ;
		private Hull[] hulls = EMPTY_HULLS ;

		private QuadNode topLeft ;
		private QuadNode topRight ;
		private QuadNode bottomLeft ;
		private QuadNode bottomRight ;

		private float length ;
		private int nextHull = 0 ;
		private boolean parent = false ;

		public QuadNode( final float _x, final float _y, final float _length, final Quadrant _quadrant )
		{
			x = _x ;
			y = _y ;
			length = _length ;

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
				generateContactsFromQuadrants( _hull, _contacts ) ;
				return _contacts ;
			}

			check.reset() ;

			// nextHull is the current length of hulls 
			// we want the hull we've passed in to be compared 
			// against all hulls within this node.
			check.setBaseHull( _hull ) ;
			for( int i = 0; i < nextHull; ++i )
			{
				check.generateContactPoint( hulls[i] ) ;
			}
			check.getContacts( _contacts ) ;

			return _contacts ;
		}

		public List<ContactData> getContactData( final List<ContactData> _fill )
		{
			if( parent == true )
			{
				topLeft.getContactData( _fill ) ;
				topRight.getContactData( _fill ) ;
				bottomLeft.getContactData( _fill ) ;
				bottomRight.getContactData( _fill ) ;
				return _fill ;
			}

			_fill.add( check.getContactData() ) ;
			return _fill ;
		}

		public Hull ray( final Ray _ray, final int _f )
		{
			if( parent == true )
			{
				switch( findQuadrant( _ray.getPoint() ) )
				{
					default           : return null ;
					case TOP_LEFT     : return topLeft.ray( _ray, _f ) ;
					case TOP_RIGHT    : return topRight.ray( _ray, _f ) ;
					case BOTTOM_LEFT  : return bottomLeft.ray( _ray, _f ) ;
					case BOTTOM_RIGHT : return bottomRight.ray( _ray, _f ) ;
				}
			}

			Hull best = null ;
			float distance = Float.MAX_VALUE ;

			for( int i = 0; i < nextHull; ++i )
			{
				final Hull hull = hulls[i] ;
				if( Hull.isCollidableWithGroup( hull.getGroupID(), _f ) == false )
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

			return best ;
		}

		public void getQuadNodes( final Vector2 _pt, final List<QuadNode> _toAdd )
		{
			if( parent )
			{
				switch( findQuadrant( _pt ) )
				{
					default           : break ;
					case TOP_LEFT     : topLeft.getQuadNodes( _pt, _toAdd ) ; break ;
					case TOP_RIGHT    : topRight.getQuadNodes( _pt, _toAdd ) ; break ;
					case BOTTOM_LEFT  : bottomLeft.getQuadNodes( _pt, _toAdd ) ; break ;
					case BOTTOM_RIGHT : bottomRight.getQuadNodes( _pt, _toAdd ) ; break ;
				}
				return ;
			}

			if( !_toAdd.contains( this ) )
			{
				_toAdd.add( this ) ;
			}
		}

		public synchronized boolean addSync( final Hull _hull )
		{
			return add( _hull ) ;
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
				if( !createChildren() )
				{
					// If we can't create child nodes we
					// must expand our capacity.
					expandHullCapacity() ;
				}
				return false ;
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

		public void clear()
		{
			if( parent )
			{
				topLeft.clear() ;
				topRight.clear() ;
				bottomLeft.clear() ;
				bottomRight.clear() ;
				return ;
			}

			final int diff = hulls.length - nextHull ;
			if( diff > 50 )
			{
				final Hull[] newHulls = new Hull[nextHull + MAX_HULLS] ;
				System.arraycopy( hulls, 0, newHulls, 0, nextHull ) ;
				hulls = newHulls ;
			}

			for( int i = 0; i < nextHull; ++i )
			{
				hulls[i].changed( false ) ;
				hulls[i] = null ;
			}

			nextHull = 0 ;
		}

		public void generateContactsFromQuadrants( final Hull _hull, final ContactData _contacts )
		{
			// Each Quadrant TOP_LEFT, TOP_RIGHT, 
			// BOTTOM_LEFT, BOTTOM_RIGHT, should only 
			// have the hull stored within it once.
			// Once the hull has been added to the 
			// appropriate node, then we should not attempt 
			// to insert the hull again.
			// Inserting the hull is costly, and should 
			// only be done, if it isn't there already.
			boolean usedTopLeft = false ;
			boolean usedTopRight = false ;
			boolean usedBottomLeft = false ;
			boolean usedBottomRight = false ;

			final Vector2 absolute = new Vector2() ;

			final int length = _hull.getPointsLength() ;
			for( int i = 0; i < length; ++i )
			{
				_hull.getPoint( i, absolute ) ;

				final float x = absolute.x ;
				final float y = absolute.y ;

				_hull.getPosition( absolute ) ;
				absolute.add( x, y ) ;

				// Find out what quadrants the hull covers. 
				switch( findQuadrant( absolute ) )
				{
					case TOP_LEFT     :
					{
						if( usedTopLeft == false )
						{
							usedTopLeft = true ;
							topLeft.generateContacts( _hull, _contacts ) ;
						}
						break ;
					}
					case TOP_RIGHT    :
					{
						if( usedTopRight == false )
						{
							usedTopRight = true ;
							topRight.generateContacts( _hull, _contacts ) ;
						}
						break ;
					}
					case BOTTOM_LEFT  :
					{
						if( usedBottomLeft == false )
						{
							usedBottomLeft = true ;
							bottomLeft.generateContacts( _hull, _contacts ) ;
						}
						break ;
					}
					case BOTTOM_RIGHT :
					{
						if( usedBottomRight == false )
						{
							usedBottomRight = true ;
							bottomRight.generateContacts( _hull, _contacts ) ;
						}
						break ;
					}
				}
			}
		}

		private boolean createChildren()
		{
			//System.out.println( "Create Children" ) ;
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

			length = tempRoot.length ;
			topLeft = tempRoot.topLeft ;
			topRight = tempRoot.topRight ;
			bottomLeft = tempRoot.bottomLeft ;
			bottomRight = tempRoot.bottomRight ;
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



	private interface IUpdate
	{
		public void update( final float _dt, final List<ContactData> _contacts ) ;
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
