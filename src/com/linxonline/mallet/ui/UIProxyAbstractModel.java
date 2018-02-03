package com.linxonline.mallet.ui ;

import java.util.Set ;

public abstract class UIProxyAbstractModel implements IAbstractModel
{
	private IAbstractModel source ;

	public UIProxyAbstractModel( final IAbstractModel _source )
	{
		source = _source ;
	}

	/**
		Reimplement to map the proxy index to sources index.
	*/
	public abstract UIModelIndex mapToSource( final UIModelIndex _proxyIndex ) ;

	/**
		Reimplement to map the source index to the proxy index.
	*/
	public abstract UIModelIndex mapToProxy( final UIModelIndex _sourceIndex ) ;

	@Override
	public int rowCount( final UIModelIndex _proxyParent )
	{
		return source.rowCount( mapToSource( _proxyParent ) ) ;
	}

	@Override
	public int columnCount( final UIModelIndex _proxyParent )
	{
		return source.columnCount( mapToSource( _proxyParent ) ) ;
	}

	@Override
	public void setData( final UIModelIndex _proxyIndex, final IVariant _variant, final Role _role )
	{
		source.setData( mapToSource( _proxyIndex ), _variant, _role ) ;
	}

	@Override
	public IVariant getData( final UIModelIndex _proxyIndex, final Role _role )
	{
		return source.getData( mapToSource( _proxyIndex ), _role ) ;
	}

	@Override
	public void removeData( final UIModelIndex _index )
	{
		source.removeData( mapToSource( _index ) ) ;
	}

	@Override
	public Set<ItemFlags> getDataFlags( final UIModelIndex _proxyIndex, final Set<ItemFlags> _flags )
	{
		_flags.add( ItemFlags.NoFlags ) ;
		return _flags ;
	}

	@Override
	public boolean hasChildren( final UIModelIndex _parent )
	{
		return source.hasChildren( mapToSource( _parent ) ) ;
	}
}
