package com.linxonline.mallet.ui ;

import java.util.Set ;

/**
	Interface for retrieving and setting data to be displayed to the user.
	Multiple views should be able to attach themselves to the same model.

	Extend this class when implementing a new data-set.
	This can be used in conjunction with com.linxonline.mallet.io.save.state.DataState,
	allowing for easier loading/saving of data-sets too.
*/
public interface IAbstractModel
{
	public enum ItemFlags
	{
		NoFlags,			// No properties set
		Editable,			// Is editable
		Selectable,			// Is Selectable
		Enabled				// User can interact with
	} ;

	public enum Role
	{
		Display,			// Return or Set data used for display state
		Edit,				// Return or Set data used while in an edit state
		User				// Return or Set data defined as custom user information
	} ;

	public UIModelIndex root() ;

	/**
		Return the amount of rows for the children of _parent.
	*/
	public int rowCount( final UIModelIndex _parent ) ;

	/**
		Return the amount of columns for the children of _parent.
	*/
	public int columnCount( final UIModelIndex _parent ) ;

	public boolean createData( final UIModelIndex _parent, final int _row, final int _column ) ;

	/**
		Update the data-model at _index location based on the role and 
		the value stored in the variant. 
	*/
	public void setData( final UIModelIndex _index, final IVariant _variant, final Role _role ) ;

	/**
		Return the value stored at _index location based on the _role.
	*/
	public IVariant getData( final UIModelIndex _index, final Role _role ) ;

	/**
		Remove the cell data associated at _row, _column, if _row and _column are >= 0.
		Remove the data associated at _row, if _row is >= 0 and _column is -1.
		Remove the data associted at _column, if _column is >= 0 and _row is -1.
		Remove the data associated to the parent, if parent is specified 
		but _row and _column are -1.
		
	*/
	public void removeData( final UIModelIndex _index ) ;

	/**
		Determine what can be done to an item defined by the _index.
	*/
	public Set<ItemFlags> getDataFlags( final UIModelIndex _index, final Set<ItemFlags> _flags ) ;

	/**
		Find out whether the _parent has children within the data-set.
	*/
	public boolean hasChildren( final UIModelIndex _parent ) ;
}
