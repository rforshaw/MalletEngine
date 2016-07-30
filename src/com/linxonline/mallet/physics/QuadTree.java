package com.linxonline.mallet.physics ;

import java.util.ArrayList ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.physics.hulls.Hull ;

public class QuadTree
{
	private final int TIER_GRANULAR_LIMIT  ;
	private final int MAX_HULLS ; 
	private final float START_QUAD_OFFSET ;

	private float MAX_QUAD_OFFSET ;

	private enum Quadrant
	{
		ROOT,
		TOP_LEFT,
		TOP_RIGHT,
		BOTTOM_LEFT,
		BOTTOM_RIGHT
	}

	private final QuadNode root ;
	private int checksMade = 0 ;

	public QuadTree()
	{
		this( 0.0f, 0.0f, 1000.0f, 10, 3 ) ;
	}

	public QuadTree( final float _x,
					 final float _y,
					 final float _size,
					 final int _nodeCapacity,
					 final int _tierGranularity )
	{
		root = new QuadNode( _x, _y, Quadrant.ROOT, 0 ) ;
		START_QUAD_OFFSET = _size ;
		TIER_GRANULAR_LIMIT = _tierGranularity ;
		MAX_HULLS = _nodeCapacity ;

		MAX_QUAD_OFFSET = START_QUAD_OFFSET ;
	}

	public void insertHull( final Hull _hull )
	{
		root.insertHull( _hull ) ;
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
		checksMade = 0 ;
		root.update( _dt ) ;
		//System.out.println( "Checks Made: " + checksMade ) ;
	}

	public void clear()
	{
		root.clear() ;
	}

	protected class QuadNode
	{
		private final Vector2 centre = new Vector2() ;
		private final Quadrant quadrant ;

		private Hull[] hulls = new Hull[MAX_HULLS] ;
		private int tier ;

		private QuadNode topLeft ;
		private QuadNode topRight ;
		private QuadNode bottomLeft ;
		private QuadNode bottomRight ;

		private boolean parent = false ;
		private int nextHull = 0 ;

		private final Vector2 absolute = new Vector2() ;		// Used by insertToQuadrant() - Android optimisation

		public QuadNode( final Quadrant _quadrant, final int _tier )
		{
			this( 0.0f, 0.0f, _quadrant, _tier ) ;
		}

		public QuadNode( final float _x, final float _y, final Quadrant _quadrant, final int _tier )
		{
			centre.setXY( _x, _y ) ;
			quadrant = _quadrant ;
			tier = _tier ;
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
			// A parent node should not contain 
			// any hulls, only children should.
			if( parent == false && nextHull < hulls.length )
			{
				// We assume the check to see if the 
				// hull already exists within this node has 
				// been called. 
				hulls[nextHull++] = _hull ;
				return true ;
			}
			else if( parent == false )
			{
				// We assume the check to see if the 
				// hull already exists within this node has 
				// been called.

				// If the node has reached MAX_HULLS
				// then it needs to be divided.
				// Move the existing hulls to its children.
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
					insertToQuadrant( hulls[i] ) ;
					hulls[i] = null ;
				}
			}

			// Should only reach here if parent 
			// is set to true
			return insertToQuadrant( _hull ) ;
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

			// Shift the hulls to the left by 1
			// effectively removing the hull 
			final int index = getIndex( _hull ) ;
			if( index >= 0 )
			{
				for( int i = index + 1; i < nextHull; i++ )
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
			Hull hull1 = null ;
			Hull hull2 = null ;

			final int size = nextHull ;
			for( int i = 0; i < size; i++ )
			{
				hull1 = hulls[i] ;
				if( hull1.isCollidable() == false )
				{
					continue ;
				}

				for( int j = 0; j < size; j++ )
				{
					hull2 = hulls[j] ;
					if( hull1 != hull2 )
					{
						if( hull1.isCollidableWithGroup( hull2.getGroupID() ) == true )
						{
							++checksMade ;
							CollisionCheck.generateContactPoint( hull1, hull2 ) ;
						}
					}
				}
			}
		}

		/**
			Called by a parent node to its children.
			Should only ever be called by a parent node.
		*/
		private void updateChildren( final float _dt )
		{
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
				calculateExpansion( absolute ) ;

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
		private void calculateExpansion( final Vector2 _pos )
		{
			while( Math.abs( _pos.x ) > MAX_QUAD_OFFSET || 
				   Math.abs( _pos.y ) > MAX_QUAD_OFFSET )
			{
				expand() ;
			}
		}

		private boolean createChildren()
		{
			switch( quadrant )
			{
				case ROOT : return createTier( START_QUAD_OFFSET ) ;
				default   : return createTier( centre.x / 2.0f ) ;
			}
		}

		private boolean createTier( final float _offset )
		{
			if( tier > TIER_GRANULAR_LIMIT )
			{
				// At a certain point making the Quad Tree more accurate
				// becomes futile. A node's scope that is too small 
				// will result in more comparisons rather than less.
				return false ;
			}

			parent = true ;
			topLeft = new QuadNode( centre.x - _offset, centre.y + _offset, Quadrant.TOP_LEFT, tier + 1 ) ;
			topRight = new QuadNode( centre.x + _offset, centre.y + _offset, Quadrant.TOP_RIGHT, tier + 1 ) ;
			bottomLeft = new QuadNode( centre.x - _offset, centre.y - _offset, Quadrant.BOTTOM_LEFT, tier + 1 ) ;
			bottomRight = new QuadNode( centre.x + _offset, centre.y + _offset, Quadrant.BOTTOM_RIGHT, tier + 1 ) ;

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
			final float offset = MAX_QUAD_OFFSET ;
			MAX_QUAD_OFFSET = MAX_QUAD_OFFSET * 2.0f ;

			tier -= 1 ;
			final QuadNode tempRoot = new QuadNode( 0.0f, 0.0f, Quadrant.ROOT, tier ) ;
			tempRoot.createTier( MAX_QUAD_OFFSET ) ;

			tempRoot.topLeft.createTier( offset ) ;
			tempRoot.topLeft.bottomRight = topLeft ;

			tempRoot.topRight.createTier( offset ) ;
			tempRoot.topRight.bottomLeft = topRight ;

			tempRoot.bottomLeft.createTier( offset ) ;
			tempRoot.bottomLeft.topRight = bottomLeft ;

			tempRoot.bottomRight.createTier( offset ) ;
			tempRoot.bottomRight.topLeft = bottomRight ;

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
}