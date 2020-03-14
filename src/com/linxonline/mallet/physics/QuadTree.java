package com.linxonline.mallet.physics ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.worker.* ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.physics.hulls.Hull ;

public class QuadTree
{
	private final int MAX_HULLS ; 
	private final float NODE_AREA_LIMIT  ;

	private float ROOT_LENGTH ;

	private final IUpdate update ;

	// Used when multi-threading
	private final List<QuadNode> nodes ;
	private final WorkerGroup workers ;
	private final NodeWorker nodeWorker ;

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
		this( 0.0f, 0.0f, 2000.0f, 256.0f, 100 ) ;
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

		nodes = null ;
		workers = null ;
		nodeWorker = null ;

		update = new IUpdate()
		{
			public void update( final float _dt )
			{
				root.update( _dt ) ;
			}
		} ;
	}

	public QuadTree( final WorkerGroup _workers )
	{
		this( 0.0f, 0.0f, 2000.0f, 256.0f, 100, _workers ) ;
	}

	public QuadTree( final float _x,
					 final float _y,
					 final float _size,
					 final float _nodeAreaLimit,
					 final int _nodeCapacity,
					 final WorkerGroup _workers )
	{
		root = new QuadNode( _x, _y, _size, Quadrant.ROOT ) ;
		NODE_AREA_LIMIT = _nodeAreaLimit ;

		ROOT_LENGTH = root.length ;
		MAX_HULLS = _nodeCapacity ;

		nodes = MalletList.<QuadNode>newList() ;
		workers = _workers ;
		nodeWorker = new NodeWorker() ;

		update = new IUpdate()
		{
			public void update( final float _dt )
			{
				root.getChildNodes( nodes ) ;
				if( nodes.isEmpty() == false )
				{
					nodeWorker.setDeltaTime( _dt ) ;
					nodeWorker.setNodes( nodes ) ;

					workers.exec( nodeWorker ) ;
					nodes.clear() ;
				}
			}
		} ;
	}

	public void insertHull( final Hull _hull )
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

	public boolean exists( final Hull _hull )
	{
		return root.exists( _hull ) ;
	}

	public void update( final float _dt )
	{
		//System.out.println( "Start" ) ;
		update.update( _dt ) ;
	}

	public void clear()
	{
		root.clear() ;
	}

	protected class QuadNode
	{
		private final CollisionCheck check = new CollisionCheck() ;
		private final Vector2 centre = new Vector2() ;

		private Hull[] hulls = new Hull[MAX_HULLS] ;
		private Quadrant quadrant ;
		private float length ;

		private QuadNode topLeft ;
		private QuadNode topRight ;
		private QuadNode bottomLeft ;
		private QuadNode bottomRight ;

		private boolean parent = false ;
		private int nextHull = 0 ;

		private final Vector2 absolute = new Vector2() ;		// Used by insertToQuadrant() - Android optimisation

		public QuadNode( final float _x, final float _y, final float _length, final Quadrant _quadrant )
		{
			centre.setXY( _x, _y ) ;
			quadrant = _quadrant ;
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
					// Quad Tree will provide no benifit, and we will 
					// just need to increase the maximum amount of hulls
					// the node can contain.
					expandHullCapacity() ;
					return insertHull( _hull ) ;
				}

				nextHull = 0 ;
				for( int i = 0; i < hulls.length; i++ )
				{
					// Move the existing hulls to its children.
					insertToQuadrant( hulls[i] ) ;
					hulls[i] = null ;
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
			/*if( nextHull > 0 )
			{
				System.out.println( "Centre: " + centre.toString() + " Length: " + length + " Quadrant: " + quadrant + " Hulls: " + nextHull ) ;
			}*/

			while( nextHull > 0 )
			{
				final Hull hull1 = hulls[0] ;
				if( hull1.isCollidable() == false )
				{
					removeHull( 0 ) ;
					continue ;
				}

				final int size = nextHull ;
				for( int j = 1; j < size; j++ )
				{
					final Hull hull2 = hulls[j] ;
					if( hull1.isCollidableWithGroup( hull2.getGroupID() ) == true )
					{
						check.generateContactPoint( hull1, hull2 ) ;
					}
				}

				// We've compared this hull against all the other 
				// hulls, and generated the needed collision points.
				// No more can be done, leaving it in will only 
				// consume valuable resources. 
				removeHull( 0 ) ;
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

			final Vector2[] points = _hull.getPoints() ;
			for( int i = 0; i < points.length; i++ )
			{
				absolute.setXY( _hull.getPosition() ) ;
				absolute.add( points[i] ) ;
				
				// It is possible for a hulls points to 
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
			}

			return added > 0 ;
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

			parent = true ;
			hulls = new Hull[0] ;

			topLeft = new QuadNode( centre.x - _offset, centre.y + _offset, _offset, Quadrant.TOP_LEFT ) ;
			topRight = new QuadNode( centre.x + _offset, centre.y + _offset, _offset, Quadrant.TOP_RIGHT ) ;
			bottomLeft = new QuadNode( centre.x - _offset, centre.y - _offset, _offset, Quadrant.BOTTOM_LEFT ) ;
			bottomRight = new QuadNode( centre.x + _offset, centre.y - _offset, _offset, Quadrant.BOTTOM_RIGHT ) ;

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
			topLeft.quadrant = Quadrant.BOTTOM_RIGHT ;
			tempRoot.topLeft.bottomRight = topLeft ;

			tempRoot.topRight.createChildren() ;
			topRight.quadrant = Quadrant.BOTTOM_LEFT ;
			tempRoot.topRight.bottomLeft = topRight ;

			tempRoot.bottomLeft.createChildren() ;
			bottomLeft.quadrant = Quadrant.TOP_RIGHT ;
			tempRoot.bottomLeft.topRight = bottomLeft ;

			tempRoot.bottomRight.createChildren() ;
			bottomRight.quadrant = Quadrant.TOP_LEFT ;
			tempRoot.bottomRight.topLeft = bottomRight ;

			length = tempRoot.length ;
			topLeft = tempRoot.topLeft ;
			topRight = tempRoot.topRight ;
			bottomLeft = tempRoot.bottomLeft ;
			bottomRight = tempRoot.bottomRight ;
		}
	}

	protected static Quadrant findQuadrant( final Vector2 _point, final Vector2 _centre )
	{
		if( _point.x >= _centre.x )
		{
			if( _point.y >= _centre.y )
			{
				return Quadrant.TOP_RIGHT ;
			}
			else
			{
				return Quadrant.BOTTOM_RIGHT ;
			}
		}
		else
		{
			if( _point.y >= _centre.y )
			{
				return Quadrant.TOP_LEFT ;
			}
			else
			{
				return Quadrant.BOTTOM_LEFT ;
			}
		}
	}

	private interface IUpdate
	{
		public void update( final float _dt ) ;
	}
	
	private class NodeWorker extends Worker<QuadNode>
	{
		private float deltaTime = 0.0f ;
		private List<QuadNode> nodes ;

		public NodeWorker() {}

		public void setDeltaTime( final float _dt )
		{
			deltaTime = _dt ;
		}

		public void setNodes( final List<QuadNode> _nodes )
		{
			nodes = _nodes ;
		}

		@Override
		public ExecType exec( final int _index, final QuadNode _node )
		{
			_node.update( deltaTime ) ;
			return ExecType.CONTINUE ;
		}

		@Override
		public List<QuadNode> getDataSet()
		{
			return nodes ;
		}
	}
}
