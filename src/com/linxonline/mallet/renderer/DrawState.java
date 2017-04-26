package com.linxonline.mallet.renderer ;

import java.util.List ;

import com.linxonline.mallet.util.ManagedArray ;

public final class DrawState<D extends DrawData> extends ManagedArray<D>
{
	private final UploadInterface<D> UPLOAD_DEFAULT = new UploadInterface<D>()
	{
		@Override
		public void upload( final D _data ) {}
	} ;

	private UploadInterface<D> upload = UPLOAD_DEFAULT ;

	public synchronized void update( final int _diff, final int _iteration )
	{
		manageState() ;

		final int size = current.size() ;
		for( int i = 0; i < size; i++ )
		{
			final D draw = current.get( i ) ;
			draw.update( _diff, _iteration ) ;
			if( draw.toUpdate() == true )
			{
				upload.upload( draw ) ;
			}
		}
	}

	public List<D> getActiveDraws()
	{
		return current ;
	}

	@Override
	protected void addNewData( final List<D> _toAdd )
	{
		if( _toAdd.isEmpty() == false )
		{
			final int size = _toAdd.size() ;
			for( int i = 0; i < size; i++ )
			{
				final D add = _toAdd.get( i ) ;
				insertNewDrawData( add ) ;
			}
			_toAdd.clear() ;
		}
	}

	private void insertNewDrawData( final D _insert )
	{
		final int order = _insert.getOrder() ;
		final int size = current.size() ;
		if( order < size )
		{
			current.add( order, _insert ) ;
			return ;
		}

		current.add( _insert ) ;
	}

	public void sort() {}

	public void clear() {}

	public void setUploadInterface( final UploadInterface<D> _upload )
	{
		upload = ( _upload == null ) ? UPLOAD_DEFAULT : _upload ;
	}

	public interface UploadInterface<T extends Draw>
	{
		public void upload( final T _data ) ;
	}
}
