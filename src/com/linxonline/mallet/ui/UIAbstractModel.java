package com.linxonline.mallet.ui ;

import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.MalletMap ;

public class UIAbstractModel implements IAbstractModel
{
	private final Matrix root = new Matrix( null, -1, -1 ) ;
	private final Connect connect ;

	public UIAbstractModel()
	{
		this( new Connect() ) ;
	}

	public UIAbstractModel( final Connect _connect )
	{
		connect = _connect ;
	}

	@Override
	public final UIModelIndex root()
	{
		return root.getHandler() ;
	}

	@Override
	public int rowCount( final UIModelIndex _index )
	{
		final UIModelIndex index = ( _index != null ) ? _index : root.getHandler() ;
		return root.rowCount( index ) ;
	}

	@Override
	public int columnCount( final UIModelIndex _index )
	{
		final UIModelIndex index = ( _index != null ) ? _index : root.getHandler() ;
		return root.columnCount( index ) ;
	}

	@Override
	public boolean createData( final UIModelIndex _parent, final int _row, final int _column )
	{
		final UIModelIndex parent = ( _parent != null ) ? _parent : root.getHandler() ;
		return root.createData( parent, _row, _column ) ;
	}

	@Override
	public void setData( final UIModelIndex _index, final IVariant _variant, final Role _role ) throws IndexOutOfBoundsException
	{
		final UIModelIndex index = ( _index != null ) ? _index : root.getHandler() ;
		root.setData( index, _variant, _role ) ;
	}

	@Override
	public IVariant getData( final UIModelIndex _index, final IAbstractModel.Role _role ) throws IndexOutOfBoundsException
	{
		final UIModelIndex index = ( _index != null ) ? _index : root.getHandler() ;
		return root.getData( index, _role ) ;
	}

	@Override
	public boolean exists( final UIModelIndex _index )
	{
		final UIModelIndex index = ( _index != null ) ? _index : root.getHandler() ;
		return root.exists( index ) ;
	}
	
	@Override
	public void removeData( final UIModelIndex _index )
	{
		final UIModelIndex index = ( _index != null ) ? _index : root.getHandler() ;
		root.removeData( index ) ;
	}

	@Override
	public Set<ItemFlags> getDataFlags( final UIModelIndex _index, final Set<ItemFlags> _flags )
	{
		final UIModelIndex index = ( _index != null ) ? _index : root.getHandler() ;
		return root.getDataFlags( index, _flags ) ;
	}

	@Override
	public boolean hasChildren( final UIModelIndex _index )
	{
		return rowCount( _index ) > 0 ;
	}

	@Override
	public Connect getConnect()
	{
		return connect ;
	}

	private static class Matrix
	{
		private final UIModelIndex handler ;
		private List<List<Matrix>> rows ;

		private IVariant display ;
		private IVariant edit ;
		private IVariant user ;

		public Matrix( final UIModelIndex _parent, final int _row, final int _column )
		{
			handler = new UIModelIndex( _parent, _row, _column ) ;
		}

		public UIModelIndex getHandler()
		{
			return handler ;
		}

		public int rowCount( final UIModelIndex _index )
		{
			if( isHandler( _index ) == true )
			{
				if( rows != null )
				{
					if( rows.isEmpty() == false )
					{
						return rows.size() ;
					}
				}
				return 0 ;
			}

			// Go down all of the parents until we find a parent 
			// that matches our current Matrix, we then use that 
			// index to get the child matrix that will eventually 
			// lead us to the Matrix that we are looking for.
			UIModelIndex parent = _index.getParent() ;
			while( parent != null )
			{
				if( isHandler( parent ) == true )
				{
					final Matrix child = rows.get( _index.getRow() ).get( _index.getColumn() ) ;
					return child.rowCount( _index ) ;
				}
				parent = parent.getParent() ;
			}

			// If we do not find a parent that matches our current
			// matrix then we would never find the Matrix that represents _index.
			return -1 ;
		}

		public int columnCount( final UIModelIndex _index )
		{
			if( isHandler( _index ) == true )
			{
				if( rows != null )
				{
					if( rows.isEmpty() == false )
					{
						return rows.get( 0 ).size() ;
					}
				}
				return 0 ;
			}

			// Go down all of the parents until we find a parent 
			// that matches our current Matrix, we then use that 
			// index to get the child matrix that will eventually 
			// lead us to the Matrix that we are looking for.
			UIModelIndex parent = _index.getParent() ;
			while( parent != null )
			{
				if( isHandler( parent ) == true )
				{
					final Matrix child = rows.get( _index.getRow() ).get( _index.getColumn() ) ;
					return child.columnCount( _index ) ;
				}
				parent = parent.getParent() ;
			}

			// If we do not find a parent that matches our current
			// matrix then we would never find the Matrix that represents _index.
			return -1 ;
		}

		public boolean createData( final UIModelIndex _parent, final int _row, final int _column )
		{
			if( isHandler( _parent ) == true )
			{
				addNewRows( _row ) ;
				addNewColumns( _column ) ;
				return true ;
			}
			
			// Go down all of the parents until we find a parent 
			// that matches our current Matrix, we then use that 
			// index to get the child matrix that will eventually 
			// lead us to the Matrix that we are looking for.
			UIModelIndex parent = _parent.getParent() ;
			while( parent != null )
			{
				if( isHandler( parent ) == true )
				{
					final Matrix child = rows.get( parent.getRow() ).get( parent.getColumn() ) ;
					return child.createData( _parent, _row, _column ) ;
				}
				parent = parent.getParent() ;
			}

			// If we do not find a parent that matches our current
			// matrix then we would never find the Matrix that represents _index.
			return false ;
		}

		public void setData( final UIModelIndex _index, final IVariant _variant, final IAbstractModel.Role _role )
		{
			if( isHandler( _index ) == true )
			{
				setVariant( _variant, _role ) ;
				return ;
			}

			// Go down all of the parents until we find a parent 
			// that matches our current Matrix, we then use that 
			// index to get the child matrix that will eventually 
			// lead us to the Matrix that we are looking for.
			UIModelIndex parent = _index.getParent() ;
			while( parent != null )
			{
				if( isHandler( parent ) == true )
				{
					final Matrix child = rows.get( _index.getRow() ).get( _index.getColumn() ) ;
					child.setData( _index, _variant, _role ) ;
					return ;
				}
				parent = parent.getParent() ;
			}

			// If we do not find a parent that matches our current
			// matrix then we would never find the Matrix that represents _index.
			return ;
		}

		public IVariant getData( final UIModelIndex _index, final IAbstractModel.Role _role )
		{
			if( isHandler( _index ) == true )
			{
				return getVariant( _role ) ;
			}

			// Go down all of the parents until we find a parent 
			// that matches our current Matrix, we then use that 
			// index to get the child matrix that will eventually 
			// lead us to the Matrix that we are looking for.
			UIModelIndex parent = _index.getParent() ;
			while( parent != null )
			{
				if( isHandler( parent ) == true )
				{
					final Matrix child = rows.get( _index.getRow() ).get( _index.getColumn() ) ;
					return child.getData( _index, _role ) ;
				}
				parent = parent.getParent() ;
			}

			// If we do not find a parent that matches our current
			// matrix then we would never find the Matrix that represents _index.
			return null ;
		}

		public boolean exists( final UIModelIndex _index )
		{
			if( isHandler( _index ) == true )
			{
				return true ;
			}

			// Go down all of the parents until we find a parent 
			// that matches our current Matrix, we then use that 
			// index to get the child matrix that will eventually 
			// lead us to the Matrix that we are looking for.
			UIModelIndex parent = _index.getParent() ;
			while( parent != null )
			{
				if( isHandler( parent ) == true )
				{
					final int rowIndex = _index.getRow() ;
					if( rows == null )
					{
						return false ;
					}
					else if( rows.size() <= rowIndex || rowIndex < 0 )
					{
						return false ;
					}

					final int columnIndex = _index.getColumn() ;
					final List<Matrix> row = rows.get( rowIndex ) ;
					if( row == null )
					{
						return false ;
					}
					else if( row.size() <= columnIndex || columnIndex < 0 )
					{
						return false ;
					}

					final Matrix child = row.get( columnIndex ) ;
					if( child == null )
					{
						return false ;
					}

					return child.exists( _index ) ;
				}
				parent = parent.getParent() ;
			}

			// If we do not find a parent that matches our current
			// matrix then we would never find the Matrix that represents _index.
			return false ;
		}

		/**
			NOTE: Currently implemented to remove data from a specific cell.
		*/
		public void removeData( final UIModelIndex _index )
		{
			if( isHandler( _index ) == true )
			{
				setVariant( null, IAbstractModel.Role.Display ) ;
				setVariant( null, IAbstractModel.Role.Edit ) ;
				setVariant( null, IAbstractModel.Role.User ) ;
				return ;
			}

			// Go down all of the parents until we find a parent 
			// that matches our current Matrix, we then use that 
			// index to get the child matrix that will eventually 
			// lead us to the Matrix that we are looking for.
			UIModelIndex parent = _index.getParent() ;
			while( parent != null )
			{
				if( isHandler( parent ) == true )
				{
					final Matrix child = rows.get( _index.getRow() ).get( _index.getColumn() ) ;
					child.removeData( _index ) ;
					return ;
				}
				parent = parent.getParent() ;
			}

			// If we do not find a parent that matches our current
			// matrix then we would never find the Matrix that represents _index.
			return ;
		}

		public Set<ItemFlags> getDataFlags( final UIModelIndex _index, final Set<ItemFlags> _flags )
		{
			if( isHandler( _index ) == true )
			{
				return null ;//getVariantFlags( _role ) ;
			}

			// Go down all of the parents until we find a parent 
			// that matches our current Matrix, we then use that 
			// index to get the child matrix that will eventually 
			// lead us to the Matrix that we are looking for.
			UIModelIndex parent = _index.getParent() ;
			while( parent != null )
			{
				if( isHandler( parent ) == true )
				{
					final Matrix child = rows.get( _index.getRow() ).get( _index.getColumn() ) ;
					return child.getDataFlags( _index, _flags ) ;
				}
				parent = parent.getParent() ;
			}

			// If we do not find a parent that matches our current
			// matrix then we would never find the Matrix that represents _index.
			return _flags ;
		}

		private boolean isHandler( final UIModelIndex _index )
		{
			return getHandler().equals( _index ) ;
		}

		private void setVariant( final IVariant _variant, final IAbstractModel.Role _role )
		{
			switch( _role )
			{
				case Display : display = _variant ; break ;
				case Edit    : edit = _variant ; break ;
				case User    : user = _variant ; break ;
			}
		}

		private IVariant getVariant( final IAbstractModel.Role _role )
		{
			switch( _role )
			{
				case Display : return display ;
				case Edit    : return edit ;
				case User    : return user ;
				default      : return null ;
			}
		}

		private Set<ItemFlags> getVariantFlags( final Set<ItemFlags> _flags )
		{
			return _flags ;
		}
		
		private void addNewRows( final int _row )
		{
			if( _row >= 0 && rows == null )
			{
				// To save some space we only construct rows 
				// if the developer has specified at least 1 row.
				// Once rows has been initialised we don't 
				// want to create it again.
				rows = MalletList.<List<Matrix>>newList() ;
			}
		
			final int rowSize = rows.size() ;
			for( int i = rowSize; i < _row; i++ )
			{
				final List<Matrix> row = MalletList.<Matrix>newList() ;
				rows.add( row ) ;

				// Before accessing the first row we will add our 
				// new row to the list, as it might be t he first row.
				final int columnSize = rows.get( 0 ).size() ;
				for( int j = 0; j < columnSize; j++ )
				{
					row.add( new Matrix( getHandler(), i, j ) ) ;
				}
			}
		}

		private void addNewColumns( final int _column )
		{
			final int columnSize = rows.get( 0 ).size() ;

			final int rowSize = rows.size() ;
			for( int i = 0; i < rowSize; i++ )
			{
				final List<Matrix> row = rows.get( i ) ;
				for( int j = columnSize; j < _column; j++ )
				{
					row.add( new Matrix( getHandler(), i, j ) ) ;
				}
			}
		}
	}
}
