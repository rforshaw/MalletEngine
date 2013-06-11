package com.linxonline.mallet.util.factory ;

import com.linxonline.mallet.util.settings.Settings ;

public interface FactoryInterface
{
	public void addCreator( final CreatorInterface _creator ) ;
	public boolean removeCreator( final CreatorInterface _creator ) ;
	public boolean removeCreator( final String _type ) ;

	public Object create( final Settings _setting ) ;		// Returns a created object based on settings passed
}
