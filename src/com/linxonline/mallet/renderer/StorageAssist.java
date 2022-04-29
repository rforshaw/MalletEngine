package com.linxonline.mallet.renderer ;

public final class StorageAssist
{
	private static Assist assist ;

	private StorageAssist() {}

	public static void setAssist( final Assist _assist )
	{
		assist = _assist ;
	}

	public static <T extends IUpdater<Storage>> T add( final T _updater )
	{
		return assist.add( _updater ) ;
	}

	public static <T extends IUpdater<Storage>> T remove( final T _updater )
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
		public <T extends IUpdater<Storage>> T add( final T _updater ) ;
		public <T extends IUpdater<Storage>> T remove( final T _updater ) ;

		public Storage add( final Storage _storage ) ;
		public Storage update( final Storage _storage ) ;
	}
}
