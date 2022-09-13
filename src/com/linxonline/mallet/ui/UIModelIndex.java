package com.linxonline.mallet.ui ;

public final class UIModelIndex
{
	private final UIModelIndex parent ;		// null == root node
	private final int row ;
	private final int column ;

	public UIModelIndex()
	{
		this( null, -1, -1 ) ;
	}

	public UIModelIndex( final UIModelIndex _parent )
	{
		this( _parent, -1, -1 ) ;
	}

	public UIModelIndex( final int _row, final int _column )
	{
		this( null, _row, _column ) ;
	}

	public UIModelIndex( final UIModelIndex _parent, final int _row, final int _column )
	{
		parent = _parent ;
		row = _row ;
		column = _column ;
	}

	public boolean isValid()
	{
		return row >= 0 && column >= 0 ;
	}

	public UIModelIndex getParent()
	{
		return parent ;
	}

	public UIModelIndex getSibling( final int _row, final int _column )
	{
		return new UIModelIndex( getParent(), _row, _column ) ;
	}

	public UIModelIndex getChild( final int _row, final int _column )
	{
		return new UIModelIndex( this, _row, _column ) ;
	}

	public int getRow()
	{
		return row ;
	}

	public int getColumn()
	{
		return column ;
	}

	@Override
	public int hashCode()
	{
		return ( row * 2 ) + ( column * 3 ) ; 
	}

	@Override
	public boolean equals( final Object _obj )
	{
		if( _obj == null )
		{
			return false ;
		}
		
		if( _obj instanceof UIModelIndex )
		{
			final UIModelIndex index = ( UIModelIndex )_obj ;
			if( index.row == row && index.column == column )
			{
				if( parent == null )
				{
					return index.parent == null ;
				}

				return parent.equals( index.parent ) ;
			}
		}

		return false ;
	}

	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer() ;
		buffer.append( '[' ) ;
		buffer.append( parent ) ;
		buffer.append( ' ' ) ;
		buffer.append( "Row: " ) ;
		buffer.append( getRow() ) ;
		buffer.append( " Col: " ) ;
		buffer.append( getColumn() ) ;
		buffer.append( ']' ) ;
		
		return buffer.toString() ;
	}
}
