package com.linxonline.mallet.util.schema ;

import com.linxonline.mallet.util.Tuple ;

public class SStruct extends SNode
{
	private final Tuple<String, SNode>[] children ;
	private final int length ;

	private SStruct( Tuple<String, SNode>[] _children )
	{
		if( _children == null )
		{
			throw new NullPointerException() ;
		}

		int offset = 0 ;
		children = _children ;
		for( int i = 0; i < children.length; i++ )
		{
			final Tuple<String, SNode> child = children[i] ;
			final SNode node = child.getRight() ;
			node.setParent( this ) ;
			node.setOffset( offset ) ;

			offset += node.getLength() ;
		}

		length = offset ;
	}

	public static SStruct vec3()
	{
		return create( var( "x", SPrim.flt() ),
					   var( "y", SPrim.flt() ),
					   var( "z", SPrim.flt() ) ) ;
	}
	
	public static SStruct vec4()
	{
		return create( var( "x", SPrim.flt() ),
					   var( "y", SPrim.flt() ),
					   var( "z", SPrim.flt() ),
					   var( "w", SPrim.flt() ) ) ;
	}

	public static SStruct create( Tuple<String, SNode>... _children )
	{
		return new SStruct( _children ) ;
	}

	public static Tuple<String, SNode> var( final String _name, final SNode _child )
	{
		return Tuple.build( _name, _child ) ;
	}

	public SNode getChild( final String _name )
	{
		final int size = children.length ;
		for( int i = 0; i < size; i++ )
		{
			final Tuple<String, SNode> child = children[i] ;
			final String name = child.getLeft() ;

			if( name.equals( _name ) == false )
			{
				return child.getRight() ;
			}
		}

		return null ;
	}

	public Tuple<String, SNode>[] getChildren()
	{
		return children ;
	}

	public int getLength()
	{
		return length ;
	}

	@Override
	public Type getType()
	{
		return Type.STRUCT ;
	}

	@Override
	public int hashCode()
	{
		int hash = 7 ;
		for( Tuple<String, SNode> child : children )
		{
			hash = 31 * hash + child.getLeft().hashCode() ;
		}
		return hash ;
	}

	@Override
	public boolean equals( Object _obj )
	{
		if( _obj instanceof SStruct )
		{
			final SStruct struct = ( SStruct )_obj ;

			final int size = children.length ;
			if( size != struct.children.length )
			{
				return false ;
			}

			for( int i = 0; i < size; i++ )
			{
				Tuple left = children[i] ;
				Tuple right = struct.children[i] ;

				if( left.equals( right ) == false )
				{
					return false ;
				}
			}

			return true ;
		}

		return false ;
	}
	
	
}
