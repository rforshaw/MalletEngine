package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.maths.Ratio ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.io.reader.RFReader ;
import com.linxonline.mallet.util.settings.Settings ;

/**
	SupportResolutions allows a developer to inform the Engine what resolutions & aspect-ratios
	they have decided to support.
	
	If a screen resolution is used that is not directly supported, then it will use the best 
	resolution availble. If the screen uses an aspect-ratio, not supported then it will 
	return the _default resolution. Ie. the resolution passed in.
**/
public final class SupportResolutions
{
	private final HashMap<String, ArrayList<Vector2>> resolutions = new HashMap<String, ArrayList<Vector2>>() ;

	public SupportResolutions() {}

	public boolean loadSupportFile( final String _file )
	{
		final ArrayList<Settings> file = RFReader.loadFile( _file ) ;
		if( file != null )
		{
			parseFile( file ) ;
			return true ;
		}

		System.out.println( "Failed to load: " + _file ) ;
		return false ;
	}

	public Vector2 getBestResolution( final Ratio _ratio, final Vector2 _default )
	{
		final String ratio = _ratio.toString() ;
		if( resolutions.containsKey( ratio ) == true )
		{
			return findBestResolution( resolutions.get( ratio ), _default ) ;
		}

		return _default ;
	}

	private Vector2 findBestResolution( final ArrayList<Vector2> _resolutions, final Vector2 _default )
	{
		final int length = _resolutions.size() ;
		final Vector2 diff = new Vector2() ;
		Vector2 res = null ;

		// Assumes resolutions are in order
		for( int i = 0; i < length; ++i )
		{
			res = _resolutions.get( i ) ;
			diff.x = res.x - _default.x ;
			diff.y = res.y - _default.y ;

			if( ( int )diff.x >= 0 && ( int )diff.y >= 0 )
			{
				return new Vector2( res ) ;
			}
		}

		return new Vector2( _default ) ;
	}

	private void parseFile( final ArrayList<Settings> _settings )
	{
		final String TYPE = "TYPE" ;
		final String DEFAULT = "" ;

		final int length = _settings.size() ;
		Settings setting = null ;
		String type = null ;

		for( int i = 0; i < length; ++i )
		{
			setting = _settings.get( i ) ;
			type = setting.getString( TYPE, DEFAULT ) ;
			if( type.equals( "RATIO" ) == true )
			{
				storeResolutions( setting ) ;
			}
		}
	}

	private void storeResolutions( final Settings _store )
	{
		final String[] sResolutions = _store.getString( "RESOLUTIONS", "" ).split( "," ) ;
		final ArrayList<Vector2> vResolutions = new ArrayList<Vector2>() ;

		final String X = "x" ;
		for( final String sRes : sResolutions )
		{
			final String[] s = sRes.split( X ) ;
			if( s.length == 2 )
			{
				vResolutions.add( new Vector2( Integer.parseInt( s[0] ), Integer.parseInt( s[1] ) ) ) ;
			}
		}

		final String ratio = _store.getString( "RATIO", null ) ;
		if( ratio != null )
		{
			resolutions.put( ratio, vResolutions ) ;
		}
	}
}