package com.linxonline.mallet.physics ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Parallel ;
import com.linxonline.mallet.util.time.ElapsedTimer ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.AABB ;
import com.linxonline.mallet.maths.Intersection ;

public final class QuadTree
{
	private static final Hull[] EMPTY_HULLS = new Hull[0] ;

	private final List<Hull> failed = MalletList.<Hull>newList() ;
	private final IUpdate update ;

	private final int MAX_HULLS ; 
	private final float NODE_AREA_LIMIT  ;

	private float ROOT_LENGTH ;

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
		this( 0.0f, 0.0f, 2000.0f, 128.0f, 100 ) ;
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
			private final List<QuadNode> children = MalletList.<QuadNode>newList() ;
			private final NodeWorker worker = new NodeWorker() ;

			@Override
			public void update( final float _dt )
			{
				root.getChildNodes( children ) ;
				if( children.isEmpty() )
				{
					return ;
				}

				//System.out.println( "Nodes: " + children.size() ) ;
				worker.setDeltaTime( _dt ) ;

				Parallel.forEach( children, 10, worker ) ;
				children.clear() ;
			}
		} ;
	}

	public void insertHulls( final List<Hull> _hulls )
	{
		Parallel.forBatch( _hulls, 1000, ( final int _start, final int _end, final List<Hull> _h ) ->
		{
			for( int i = _start; i < _end; ++i )
			{
				final Hull hull = _h.get( i ) ;
				hull.contactData.reset() ;

				if( !root.insertHullFast( hull ) )
				{
					synchronized( failed )
					{
						failed.add( hull ) ;
					}
				}
			}
		} ) ;

		final int size = failed.size() ;
		for( int i = 0; i < size; ++i )
		{
			insertHull( failed.get( i ) ) ;
		}
		failed.clear() ;
	}

	private void insertHull( final Hull _hull )
	{
		while( root.insertHull( _hull ) == false )
		{
			root.expand() ;
		}
	}

	public void removeHull( final Hull _hull )
	{
		root.removeHull( _hull ) ;
	}

	/**
		Generate contact points on the hull that has been 
		passed in.
		The hull will not be added to the Quad Tree but it 
		will be compared to hulls that have been inserted. 
	*/
	public void generateContacts( final Hull _hull )
	{
		root.generateContacts( _hull ) ;
	}

	public Hull ray( final Ray _ray, final int[] _f )
	{
		return root.ray( _ray, _f ) ;
	}

	public boolean exists( final Hull _hull )
	{
		return root.exists( _hull ) ;
	}

	public void update( final float _dt )
	{
		update.update( _dt ) ;
	}

	public void clear()
	{
		root.clear() ;
	}

	protected final class QuadNode
	{
		private final Object lock = new Object() ;
		private final Vector2 centre = new Vector2() ;

		private CollisionCheck check = new CollisionCheck() ;
		private Hull[] hulls = new Hull[MAX_HULLS] ;

		private QuadNode topLeft ;
		private QuadNode topRight ;
		private QuadNode bottomLeft ;
		private QuadNode bottomRight ;

		private float length ;
		private int nextHull = 0 ;
		private boolean parent = false ;

		public QuadNode( final float _x, final float _y, final float _length, final Quadrant _quadrant )
		{
			centre.setXY( _x, _y ) ;
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

		public void generateContacts( final Hull _hull )
		{
			if( parent == true )
			{
				generateContactsFromQuadrants( _hull ) ;
				return ;
			}

			// nextHull is the current length of hulls 
			// we want the hull we've passed in to be compared 
			// against all hulls within this node.
			updateCollisions( nextHull, _hull, hulls ) ;
		}

		public Hull ray( final Ray _ray, final int[] _f )
		{
			if( parent == true )
			{
				switch( findQuadrant( _ray.getPoint(), centre ) )
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

		public boolean insertHullFast( final Hull _hull )
		{
			if( parent == true )
			{
				// A parent node should not contain 
				// any hulls, only children should.
				return insertToQuadrantFast( _hull ) ;
			}

			synchronized( lock )
			{
				if( nextHull < hulls.length )
				{
					// We assume the check to see if the 
					// hull already exists within this node has 
					// been called.
					hulls[nextHull++] = _hull ;
					return true ;
				}
			}

			return false ;
		}

		/**
			Insert the hull into the node or one 
			of the nodes child nodes.
			The node will either split to accomodate 
			the hull, or expand depending on certain 
			criteria.
		*/
		public boolean insertHull( final Hull _hull )
		{
			if( parent == true )
			{
				// A parent node should not contain 
				// any hulls, only children should.
				return insertToQuadrant( _hull ) ;
			}
			else if( nextHull < hulls.length )
			{
				// We assume the check to see if the 
				// hull already exists within this node has 
				// been called.
				hulls[nextHull++] = _hull ;
				return true ;
			}
			else
			{
				// We assume the check to see if the 
				// hull already exists within this node has 
				// been called.

				// If the node has reached MAX_HULLS
				// then it needs to be divided.
				if( createChildren() == false )
				{
					// It will reach a point in which dividing the 
					// Quad Tree will provide no benefit, and we will 
					// just need to increase the maximum amount of hulls
					// the node can contain.
					expandHullCapacity() ;
					return insertHull( _hull ) ;
				}

				return true ;
			}
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
			final Hull[] newHulls = new Hull[hulls.length + MAX_HULLS] ;
			System.arraycopy( hulls, 0, newHulls, 0, hulls.length ) ;
			hulls = newHulls ;
		}

		public void removeHull( final Hull _hull )
		{
			if( parent == true )
			{
				removeFromChildren( _hull ) ;
				return ;
			}

			removeHull( getIndex( _hull ) ) ;
		}

		private void removeHull( final int _index )
		{
			// Shift the hulls to the left by 1
			// effectively removing the hull 
			if( _index >= 0 )
			{
				// Try and remove things from the top of the hull 
				// list first - it will avoid the array shift.
				for( int i = _index + 1; i < nextHull; i++ )
				{
					hulls[i - 1] = hulls[i] ;
				}
				nextHull -= 1 ;
			}
		}

		/**
			Called by a parent node to its children.
			Should only ever be called by a parent node.
		*/
		private void removeFromChildren( final Hull _hull )
		{
			topLeft.removeHull( _hull ) ;
			topRight.removeHull( _hull ) ;
			bottomLeft.removeHull( _hull ) ;
			bottomRight.removeHull( _hull ) ;
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

		public boolean exists( final Hull _hull )
		{
			if( parent == true )
			{
				return existsInChildren( _hull ) ;
			}

			return contains( _hull ) ;
		}

		/**
			Called by a parent node to its children.
			Should only ever be called by a parent node.
		*/
		private boolean existsInChildren( final Hull _hull )
		{
			return topLeft.contains( _hull ) ||
				   topRight.contains( _hull ) ||
				   bottomLeft.contains( _hull ) ||
				   bottomRight.contains( _hull ) ;
		}

		public void update( final float _dt )
		{
			if( parent == false )
			{
				// If the node isn't a parent then it must
				// be the node that contains hulls
				updateThisNode( _dt ) ;
			}
			else
			{
				updateChildren( _dt ) ;
			}
		}

		/**
			If the node is a child and is not a parent 
			node then it will have nodes it must update.
			We will now check them for collisions.
		*/
		private void updateThisNode( final float _dt )
		{
			//System.out.println( "Hulls: " + nextHull ) ;
			for( int i = 0; i < nextHull; ++i )
			{
				//final int index = nextHull - 1 ;
				final Hull hull1 = hulls[i] ;
				if( hull1.isCollidable() == false )
				{
					continue ;
				}

				//System.out.println( hull1 ) ;
				updateCollisions( i, hull1, hulls ) ;
			}
		}

		private void updateCollisions( final int _index, final Hull _hull1, final Hull[] _hulls )
		{
			for( int j = _index + 1; j < nextHull; ++j )
			{
				final Hull hull2 = _hulls[j] ;
				check.generateContactPoint( _hull1, hull2 ) ;
			}
		}
		
		/**
			Called by a parent node to its children.
			Should only ever be called by a parent node.
		*/
		private void updateChildren( final float _dt )
		{
			//System.out.println( "Tier: " + tier + " Quadrant: " + quadrant + " is a Parent." ) ;
			topLeft.update( _dt ) ;
			topRight.update( _dt ) ;
			bottomLeft.update( _dt ) ;
			bottomRight.update( _dt ) ;
		}

		public void clear()
		{
			if( parent == true )
			{
				topLeft.clear() ;
				topRight.clear() ;
				bottomLeft.clear() ;
				bottomRight.clear() ;
			}

			nextHull = 0 ;
		}

		private boolean contains( final Hull _hull )
		{
			final int index = getIndex( _hull ) ;
			return index >= 0 && index < nextHull;
		}

		private int getIndex( final Hull _hull )
		{
			for( int i = 0; i < nextHull; i++ )
			{
				if( hulls[i] == _hull )
				{
					return i ;
				}
			}

			return -1 ;
		}

		/**
			Figure out what node to stick the hull into.
			It's possible for a hull to reside in multiple 
			nodes.
		*/
		private boolean insertToQuadrant( final Hull _hull )
		{
			int added = 0 ;

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

				// It is possible for a hull's points to 
				// go beyond the current scope of the tree,
				// in this case we must expand the tree
				if( needsExpansion( absolute ) == true )
				{
					return false ;
				}

				// Find out what quadrant the hull should reside in
				// A hull could potentially be in multiple 
				// quadrants, as a hulls points may cross 
				// quadrant boundaries.
				switch( findQuadrant( absolute, centre ) )
				{
					case TOP_LEFT     :
					{
						if( usedTopLeft == false )
						{
							usedTopLeft = true ;
							if( topLeft.insertHull( _hull ) == true )
							{
								++added ;
							}
						}
						break ;
					}
					case TOP_RIGHT    :
					{
						if( usedTopRight == false )
						{
							usedTopRight = true ;
							if( topRight.insertHull( _hull ) == true )
							{
								++added ;
							}
						}
						break ;
					}
					case BOTTOM_LEFT  :
					{
						if( usedBottomLeft == false )
						{
							usedBottomLeft = true ;
							if( bottomLeft.insertHull( _hull ) == true )
							{
								++added ;
							}
						}
						break ;
					}
					case BOTTOM_RIGHT :
					{
						if( usedBottomRight == false )
						{
							usedBottomRight = true ;
							if( bottomRight.insertHull( _hull ) == true )
							{
								++added ;
							}
						}
						break ;
					}
				}

				if( added >= 4 )
				{
					return true ;
				}
			}

			return added > 0 ;
		}

		private boolean insertToQuadrantFast( final Hull _hull )
		{
			int added = 0 ;

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

				// It is possible for a hull's points to 
				// go beyond the current scope of the tree,
				// in this case we must expand the tree
				if( needsExpansion( absolute ) == true )
				{
					return false ;
				}

				// Find out what quadrant the hull should reside in
				// A hull could potentially be in multiple 
				// quadrants, as a hulls points may cross 
				// quadrant boundaries.
				switch( findQuadrant( absolute, centre ) )
				{
					case TOP_LEFT     :
					{
						if( usedTopLeft == false )
						{
							usedTopLeft = true ;
							if( topLeft.insertHullFast( _hull ) == true )
							{
								++added ;
							}
						}
						break ;
					}
					case TOP_RIGHT    :
					{
						if( usedTopRight == false )
						{
							usedTopRight = true ;
							if( topRight.insertHullFast( _hull ) == true )
							{
								++added ;
							}
						}
						break ;
					}
					case BOTTOM_LEFT  :
					{
						if( usedBottomLeft == false )
						{
							usedBottomLeft = true ;
							if( bottomLeft.insertHullFast( _hull ) == true )
							{
								++added ;
							}
						}
						break ;
					}
					case BOTTOM_RIGHT :
					{
						if( usedBottomRight == false )
						{
							usedBottomRight = true ;
							if( bottomRight.insertHullFast( _hull ) == true )
							{
								++added ;
							}
						}
						break ;
					}
				}

				if( added >= 4 )
				{
					return true ;
				}
			}

			return added > 0 ;
		}

		public void generateContactsFromQuadrants( final Hull _hull )
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
				switch( findQuadrant( absolute, centre ) )
				{
					case TOP_LEFT     :
					{
						if( usedTopLeft == false )
						{
							usedTopLeft = true ;
							topLeft.generateContacts( _hull ) ;
						}
						break ;
					}
					case TOP_RIGHT    :
					{
						if( usedTopRight == false )
						{
							usedTopRight = true ;
							topRight.generateContacts( _hull ) ;
						}
						break ;
					}
					case BOTTOM_LEFT  :
					{
						if( usedBottomLeft == false )
						{
							usedBottomLeft = true ;
							bottomLeft.generateContacts( _hull ) ;
						}
						break ;
					}
					case BOTTOM_RIGHT :
					{
						if( usedBottomRight == false )
						{
							usedBottomRight = true ;
							bottomRight.generateContacts( _hull ) ;
						}
						break ;
					}
				}
			}
		}

		/**
			Check to see if the _pos is contained within 
			the current scope of the Quad Tree.
			If it's outside the trees scope expand the 
			tree until it is inside.
		*/
		private boolean needsExpansion( final Vector2 _pos )
		{
			return Math.abs( _pos.x ) > ROOT_LENGTH || 
				   Math.abs( _pos.y ) > ROOT_LENGTH ;
		}

		private boolean createChildren()
		{
			//System.out.println( "Create Children" ) ;
			return createTier( length / 2.0f ) ;
		}

		private boolean createTier( final float _offset )
		{
			if( _offset < NODE_AREA_LIMIT )
			{
				// At a certain point making the Quad Tree more accurate
				// becomes futile. A node's scope that is too small 
				// will result in more comparisons rather than less.
				return false ;
			}

			topLeft = new QuadNode( centre.x - _offset, centre.y + _offset, _offset, Quadrant.TOP_LEFT ) ;
			topRight = new QuadNode( centre.x + _offset, centre.y + _offset, _offset, Quadrant.TOP_RIGHT ) ;
			bottomLeft = new QuadNode( centre.x - _offset, centre.y - _offset, _offset, Quadrant.BOTTOM_LEFT ) ;
			bottomRight = new QuadNode( centre.x + _offset, centre.y - _offset, _offset, Quadrant.BOTTOM_RIGHT ) ;

			for( int i = 0; i < nextHull; i++ )
			{
				// Move the existing hulls to its children.
				insertToQuadrant( hulls[i] ) ;
			}

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

			final QuadNode tempRoot = new QuadNode( centre.x, centre.y, ROOT_LENGTH, Quadrant.ROOT ) ;
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
	}

	protected static Quadrant findQuadrant( final Vector2 _point, final Vector2 _centre )
	{
		if( _point.x >= _centre.x )
		{
			return ( _point.y >= _centre.y ) ? Quadrant.TOP_RIGHT : Quadrant.BOTTOM_RIGHT ;
		}
		else
		{
			return ( _point.y >= _centre.y ) ? Quadrant.TOP_LEFT : Quadrant.BOTTOM_LEFT ;
		}
	}

	protected static Quadrant findQuadrant( final Vector3 _point, final Vector2 _centre )
	{
		if( _point.x >= _centre.x )
		{
			return ( _point.y >= _centre.y ) ? Quadrant.TOP_RIGHT : Quadrant.BOTTOM_RIGHT ;
		}
		else
		{
			return ( _point.y >= _centre.y ) ? Quadrant.TOP_LEFT : Quadrant.BOTTOM_LEFT ;
		}
	}

	private interface IUpdate
	{
		public void update( final float _dt ) ;
	}

	private final static class NodeWorker implements Parallel.IRangeRun<QuadNode>
	{
		private float deltaTime = 0.0f ;

		public NodeWorker() {}

		public void setDeltaTime( final float _dt )
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
