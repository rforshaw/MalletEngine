package com.linxonline.mallet.util.factory ;

import com.linxonline.mallet.util.settings.Settings ;

public interface CreatorInterface<T, U>
{
	public String getType() ;							// Defines the type of Object that can be created
	public T create( final U _data ) ;					// Returns a created object based on settings passed
}