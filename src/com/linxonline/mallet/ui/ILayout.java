package com.linxonline.mallet.ui ;

public interface ILayout
{
	public enum Type
	{
		HORIZONTAL,
		VERTICAL,
		GRID,
		FORM ;

		public static Type derive( final String _type )
		{
			if( _type == null )
			{
				return HORIZONTAL ;
			}
			
			if( _type.isEmpty() == true )
			{
				return HORIZONTAL ;
			}

			return Type.valueOf( _type ) ;
		}
	}

	public void update( final float _dt, final IChildren _children ) ;
}
