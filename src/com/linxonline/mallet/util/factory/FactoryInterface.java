package com.linxonline.mallet.util.factory ;

import com.linxonline.mallet.util.settings.Settings ;

public interface FactoryInterface<T>
{
	public void addCreator( final CreatorInterface<T> _creator ) ;
	public boolean removeCreator( final CreatorInterface<T> _creator ) ;
	public boolean removeCreator( final String _type ) ;

	public T create( final Settings _setting ) ;		// Returns a created object based on settings passed
}
