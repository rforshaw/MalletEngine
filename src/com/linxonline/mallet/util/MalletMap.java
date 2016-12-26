package com.linxonline.mallet.util ;

import java.util.Map ;
import java.util.HashMap ;

public class MalletMap
{
	/**
		Construct a default HashMap with an initial 
		capacity of 16 and load factor of 0.75.
		Currently uses the standard library HashMap.
		Eventually this will be replaced with an implementation 
		that doesn't generate a lot of temporary objects.
	*/
	public final static <K, V> Map<K, V> newMap()
	{
		return MalletMap.<K, V>newMap( 16 ) ;
	}

	/**
		Construct a HashMap with an initial 
		load factor of 0.75 and capacity defined by the developer.
		Currently uses the standard library HashMap.
		Eventually this will be replaced with an implementation 
		that doesn't generate a lot of temporary objects.
	*/
	public final static <K, V> Map<K, V> newMap( final int _initialCapacity )
	{
		return MalletMap.<K, V>newMap( _initialCapacity, 0.75f ) ;
	}

	/**
		Construct a HashMap with an initial 
		load factor and capacity defined by the developer.
		Currently uses the standard library HashMap.
		Eventually this will be replaced with an implementation 
		that doesn't generate a lot of temporary objects.
	*/
	public final static <K, V> Map<K, V> newMap( final int _initialCapacity, final float _loadFactor )
	{
		return new HashMap<K, V>( _initialCapacity, _loadFactor ) ;
	}
}
