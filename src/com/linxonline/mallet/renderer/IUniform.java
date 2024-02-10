package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.renderer.FloatUniform ;
import com.linxonline.mallet.renderer.BoolUniform ;
import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.MalletFont ;

public interface IUniform
{
	public Type getType() ;

	public enum Type
	{
		BOOL,
		INT32,
		UINT32,
		FLOAT32,
		FLOAT64,
		SAMPLER2D,
		FONT,
		UNKNOWN ;

		public static boolean validate( final IUniform _uniform )
		{
			switch( _uniform.getType() )
			{
				case BOOL         : return BoolUniform.class.isInstance( _uniform ) ; 
				case INT32        : return IntUniform.class.isInstance( _uniform ) ;
				case UINT32       : return UIntUniform.class.isInstance( _uniform ) ;
				case FLOAT64      : return false ;
				case FLOAT32      : return FloatUniform.class.isInstance( _uniform ) ;
				case SAMPLER2D    : return MalletTexture.class.isInstance( _uniform ) ;
				case FONT         : return MalletFont.class.isInstance( _uniform ) ;
				case UNKNOWN      : return false ;
				default           : return false ;
			}
		}

		public static Type convert( final String _uniform )
		{
			switch( _uniform )
			{
				case "BOOL"         : return Type.BOOL ;
				case "INT32"        : return Type.INT32 ;
				case "UINT32"       : return Type.UINT32 ;
				case "FLOAT32"      : return Type.FLOAT32 ;
				case "FLOAT64"      : return Type.FLOAT64 ;
				case "SAMPLER2D"    : return Type.SAMPLER2D ;
				case "FONT"         : return Type.FONT ;
				default             : return Type.UNKNOWN ;
			}
		}
	}
}
