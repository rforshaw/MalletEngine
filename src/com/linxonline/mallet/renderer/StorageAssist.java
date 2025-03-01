package com.linxonline.mallet.renderer ;

public final class StorageAssist
{
	private static Assist assist ;

	private StorageAssist() {}

	public static void setAssist( final Assist _assist )
	{
		assist = _assist ;
	}

	public static StorageUpdater add( final StorageUpdater _updater )
	{
		return assist.add( _updater ) ;
	}

	public static StorageUpdater remove( final StorageUpdater _updater )
	{
		return assist.remove( _updater ) ;
	}

	/**
		Create a Storage block in which objects 
		can be added to.
	*/
	public static Storage add( final Storage _storage )
	{
		return assist.add( _storage ) ;
	}

	public static Storage update( final Storage _storage )
	{
		return assist.update( _storage ) ;
	}

	public interface Assist
	{
		public StorageUpdater add( final StorageUpdater _updater ) ;
		public StorageUpdater remove( final StorageUpdater _updater ) ;

		public Storage add( final Storage _storage ) ;
		public Storage update( final Storage _storage ) ;
	}
}
