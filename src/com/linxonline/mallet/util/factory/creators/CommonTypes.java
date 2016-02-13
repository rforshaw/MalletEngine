package com.linxonline.mallet.util.factory.creators ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.system.GlobalConfig ;
import com.linxonline.mallet.maths.* ;

public class CommonTypes
{
	public static void setDimension( final Settings _dest, final Settings _src )
	{
		final Vector2 dim = Vector2.parseVector2( _src.getString( "DIM", null ) ) ;
		if( dim != null )
		{
			_dest.addObject( "DIM", dim ) ;
		}
	}

	public static void setPercentagePosition( final Settings _dest, final Settings _src )
	{
		final Vector2 position = Vector2.parseVector2( _src.getString( "POS", "0" ) ) ;
		final Vector2 percentage = Vector2.parseVector2( _src.getString( "PER", null ) ) ;

		if( percentage != null )
		{
			final int renderWidth = GlobalConfig.getInteger( "RENDERWIDTH", 800 ) ;
			final int renderHeight = GlobalConfig.getInteger( "RENDERHEIGHT", 600 ) ;

			position.x = ( renderWidth / 100.0f ) * percentage.x ;
			position.y = ( renderHeight / 100.0f ) * percentage.y ;
		}

		_dest.addObject( "POS", position ) ;
	}

	public static void setOffset( final Settings _dest, final Settings _src )
	{
		final String offset = _src.getString( "OFFSET", null ) ;
		if( offset != null )
		{
			_dest.addObject( "OFFSET", Vector3.parseVector3( offset ) ) ;
		}
	}
}