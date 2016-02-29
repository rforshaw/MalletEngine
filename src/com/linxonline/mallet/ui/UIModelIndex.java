package com.linxonline.mallet.ui ;

public class UIModelIndex
{
	private final UIModelIndex parent ;
	private final int row ;
	private final int column ;

	public UIModelIndex()
	{
		this( null, -1, -1 ) ;
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

	public UIModelIndex getChild( final int _row, final int _column )
	{
		return new UIModelIndex( this, _row, _column ) ;
	}

	public UIModelIndex getSibling( final int _row, final int _column )
	{
		return new UIModelIndex( parent, _row, _column ) ;
	}

	public UIModelIndex getParent()
	{
		return parent ;
	}

	public int getRow()
	{
		return row ;
	}

	public int getColumn()
	{
		return column ;
	}
}