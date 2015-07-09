package com.linxonline.mallet.util.factory ;

import com.linxonline.mallet.util.settings.Settings ;

public interface FactoryInterface<T, U>
{
	public void addCreator( final CreatorInterface<T, U> _creator ) ;
	public boolean removeCreator( final CreatorInterface<T, U> _creator ) ;
	public boolean removeCreator( final String _type ) ;

	public T create( final String _type, final U _data ) ;		// Returns a created object based on settings passed
}
