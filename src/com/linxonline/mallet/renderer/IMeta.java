package com.linxonline.mallet.renderer ;

/**
	Extend the meta interface when you have information
	you want to bundle along with the object.
	This meta information could be used for a multitude
	of purposes - when implementing remember the cases may
	change depending on the context it's been used within.
*/
public interface IMeta
{
	public static final EmptyMeta EMPTY_META = new EmptyMeta() ;

	/**
		It's possible the developer will define multiple
		meta classes for different draw object use-cases.
		Each class should return a unique int that can be
		used to cast the object to the correct definition.
	*/
	public int getType() ;

	public static final class EmptyMeta implements IMeta
	{
		@Override
		public int getType()
		{
			return -1 ;
		}
	}
}
